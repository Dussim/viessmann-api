package xyz.dussim.buildlogic

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import xyz.dussim.buildlogic.internal.CommandInterfaceGenerator
import xyz.dussim.buildlogic.internal.ParameterSignature
import xyz.dussim.buildlogic.internal.YamlCommandDeclaration
import xyz.dussim.buildlogic.internal.YamlCommandParameter
import xyz.dussim.buildlogic.internal.YamlFeatureInterface
import xyz.dussim.buildlogic.internal.YamlFeatureInterfaceGenerator
import xyz.dussim.buildlogic.internal.YamlPropertyDeclaration
import xyz.dussim.buildlogic.internal.identifySharedCommands
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.LocalDate
import javax.inject.Inject

abstract class GenerateFeatureInterfacesFromYamlPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        target.run {
            val extension = extensions.create<GenerateFeatureInterfacesFromYamlExtension>("generateFeatureInterfacesFromYaml")
            val generateFeatureInterfaces =
                tasks.register(
                    "generateFeatureInterfacesFromYaml",
                    GenerateFeatureInterfacesFromYamlTask::class.java,
                ) {
                    featuresYamls = extension.featuresYamls
                    generatedSources = extension.generatedSources
                    packageName = extension.packageName
                    currentDate = extension.currentDate
                }
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets.named("commonMain") {
                        kotlin.srcDir(extension.generatedSources)
                    }
                    tasks.named { it.startsWith("ksp") && it.contains("KotlinMetadata") }.configureEach {
                        dependsOn(generateFeatureInterfaces)
                    }
                }
            }
            tasks.withType(FormatTask::class.java).configureEach {
                mustRunAfter(generateFeatureInterfaces)
            }
            tasks.withType(LintTask::class.java).configureEach {
                mustRunAfter(generateFeatureInterfaces)
            }
        }
}

abstract class GenerateFeatureInterfacesFromYamlExtension {
    abstract val featuresYamls: DirectoryProperty

    abstract val generatedSources: DirectoryProperty

    abstract val packageName: Property<String>

    abstract val currentDate: Property<LocalDate>
}

@CacheableTask
abstract class GenerateFeatureInterfacesFromYamlTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    abstract val featuresYamls: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedSources: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val currentDate: Property<LocalDate>

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    init {
        group = "build"
        description = "Generates feature interfaces from YAML OpenAPI specifications"
    }

    @TaskAction
    fun generate() {
        val outputDir = generatedSources.get().asFile
        outputDir.mkdirs()

        val inputFiles =
            featuresYamls
                .asFileTree
                .matching { include("*.yaml") }
                .files
                .sortedBy { it.name }

        logger.lifecycle("Found ${inputFiles.size} YAML feature files")

        val pkg = packageName.get()
        val generator = YamlFeatureInterfaceGenerator(pkg, logger)
        val queue = workerExecutor.noIsolation()

        // First pass: parse all features in parallel. Workers write parsed feature DTOs
        // to the task temporary directory because Gradle workers do not return values.
        logger.lifecycle("Pass 1: Parsing YAML features...")
        val parsedFeaturesDir = temporaryDir.resolve("parsed-features")
        parsedFeaturesDir.deleteRecursively()
        parsedFeaturesDir.mkdirs()
        val parsedFeatureFiles =
            inputFiles.mapIndexed { index, file ->
                parsedFeaturesDir.resolve("${index.toString().padStart(5, '0')}-${file.nameWithoutExtension}.bin")
            }

        inputFiles.zip(parsedFeatureFiles).forEach { (file, parsedFeatureFile) ->
            queue.submit(ParseYamlFeatureAction::class.java) {
                yamlFile.set(file)
                outputFile.set(parsedFeatureFile)
                packageName.set(pkg)
            }
        }
        queue.await()

        val parsedFeatures =
            parsedFeatureFiles
                .mapNotNull { file ->
                    if (file.isFile) {
                        ObjectInputStream(file.inputStream()).use { input ->
                            (input.readObject() as SerializableYamlFeatureInterface).toFeature()
                        }
                    } else {
                        null
                    }
                }

        // Collect all command signatures and identify shared ones (must remain sequential)
        val commandSignatures =
            parsedFeatures.flatMap { feature ->
                feature.commands.map { command -> generator.getCommandSignature(command) }
            }

        val sharedCommandsMap = identifySharedCommands(commandSignatures, pkg)
        logger.lifecycle("Found ${sharedCommandsMap.size} shared command signatures")

        generator.useSharedCommands(sharedCommandsMap)

        // Generate shared command interfaces in parallel via the Worker API
        sharedCommandsMap.forEach { (sig, className) ->
            queue.submit(WriteSharedCommandAction::class.java) {
                this.outputDir.set(outputDir)
                packageName.set(className.packageName)
                simpleName.set(className.simpleName)
                commandName.set(sig.name)
                parameterNames.set(sig.parameters.map { it.name })
                parameterTypes.set(sig.parameters.map { it.type })
            }
        }

        // Second pass: generate feature files in parallel
        logger.lifecycle("Pass 2: Generating feature files...")
        val now = currentDate.get()
        val existing = mutableSetOf<String>()
        var omitted = 0
        var duplicate = 0
        var submitted = 0

        for (feature in parsedFeatures) {
            val removalDate = feature.removalDate
            if (removalDate != null && (removalDate.isBefore(now) || removalDate.isEqual(now))) {
                omitted++
                logger.warn("Omitted generation of feature '${feature.featureName}' as it is deprecated and past removal date ($removalDate)")
                continue
            }
            if (!existing.add(feature.className)) {
                duplicate++
                logger.warn("Duplicate feature class: ${feature.className}")
                continue
            }
            val fileSpec = generator.generate(feature)
            queue.submit(WriteFileSpecAction::class.java) {
                this.outputDir.set(outputDir)
                packageName.set(fileSpec.packageName)
                fileName.set(fileSpec.name)
                fileContents.set(fileSpec.toString())
            }
            submitted++
        }

        queue.await()
        logger.lifecycle(
            "Results: $submitted success, $duplicate duplicate, $omitted omitted due to removal date out of ${inputFiles.size} total",
        )
        logger.lifecycle("Shared commands: ${sharedCommandsMap.size}")
        logger.lifecycle("Output directory: $outputDir")
    }
}

internal interface ParseYamlFeatureParameters : WorkParameters {
    val yamlFile: RegularFileProperty
    val outputFile: RegularFileProperty
    val packageName: Property<String>
}

internal abstract class ParseYamlFeatureAction : WorkAction<ParseYamlFeatureParameters> {
    override fun execute() {
        val logger = Logging.getLogger(ParseYamlFeatureAction::class.java)
        val file = parameters.yamlFile.get().asFile
        val generator = YamlFeatureInterfaceGenerator(parameters.packageName.get(), logger)
        val feature = generator.parseYamlFile(file) ?: return

        val outputFile = parameters.outputFile.get().asFile
        outputFile.parentFile.mkdirs()
        ObjectOutputStream(outputFile.outputStream()).use { output ->
            output.writeObject(SerializableYamlFeatureInterface.from(feature))
        }
    }
}

private data class SerializableYamlFeatureInterface(
    val featureName: String,
    val className: String,
    val properties: List<SerializableYamlPropertyDeclaration>,
    val commands: List<SerializableYamlCommandDeclaration>,
    val isDeprecated: Boolean,
    val deprecationMessage: String?,
    val removalDate: LocalDate?,
) : Serializable {
    fun toFeature(): YamlFeatureInterface =
        YamlFeatureInterface(
            featureName = featureName,
            className = className,
            properties = properties.map { it.toProperty() },
            commands = commands.map { it.toCommand() },
            isDeprecated = isDeprecated,
            deprecationMessage = deprecationMessage,
            removalDate = removalDate,
        )

    companion object {
        fun from(feature: YamlFeatureInterface): SerializableYamlFeatureInterface =
            SerializableYamlFeatureInterface(
                featureName = feature.featureName,
                className = feature.className,
                properties = feature.properties.map { SerializableYamlPropertyDeclaration.from(it) },
                commands = feature.commands.map { SerializableYamlCommandDeclaration.from(it) },
                isDeprecated = feature.isDeprecated,
                deprecationMessage = feature.deprecationMessage,
                removalDate = feature.removalDate,
            )
    }
}

private data class SerializableYamlPropertyDeclaration(
    val name: String,
    val type: String,
    val isRequired: Boolean,
) : Serializable {
    fun toProperty(): YamlPropertyDeclaration =
        YamlPropertyDeclaration(
            name = name,
            type = type,
            isRequired = isRequired,
        )

    companion object {
        fun from(property: YamlPropertyDeclaration): SerializableYamlPropertyDeclaration =
            SerializableYamlPropertyDeclaration(
                name = property.name,
                type = property.type,
                isRequired = property.isRequired,
            )
    }
}

private data class SerializableYamlCommandDeclaration(
    val propertyName: String,
    val commandName: String,
    val interfaceName: String,
    val parameters: List<SerializableYamlCommandParameter>,
    val isRequired: Boolean,
) : Serializable {
    fun toCommand(): YamlCommandDeclaration =
        YamlCommandDeclaration(
            propertyName = propertyName,
            commandName = commandName,
            interfaceName = interfaceName,
            parameters = parameters.map { it.toCommandParameter() },
            isRequired = isRequired,
        )

    companion object {
        fun from(command: YamlCommandDeclaration): SerializableYamlCommandDeclaration =
            SerializableYamlCommandDeclaration(
                propertyName = command.propertyName,
                commandName = command.commandName,
                interfaceName = command.interfaceName,
                parameters = command.parameters.map { SerializableYamlCommandParameter.from(it) },
                isRequired = command.isRequired,
            )
    }
}

private data class SerializableYamlCommandParameter(
    val name: String,
    val type: String,
    val constraintType: String,
) : Serializable {
    fun toCommandParameter(): YamlCommandParameter =
        YamlCommandParameter(
            name = name,
            type = type,
            constraintType = constraintType,
        )

    companion object {
        fun from(parameter: YamlCommandParameter): SerializableYamlCommandParameter =
            SerializableYamlCommandParameter(
                name = parameter.name,
                type = parameter.type,
                constraintType = parameter.constraintType,
            )
    }
}

internal interface WriteSharedCommandParameters : WorkParameters {
    val outputDir: DirectoryProperty
    val packageName: Property<String>
    val simpleName: Property<String>
    val commandName: Property<String>
    val parameterNames: org.gradle.api.provider.ListProperty<String>
    val parameterTypes: org.gradle.api.provider.ListProperty<String>
}

internal abstract class WriteSharedCommandAction : WorkAction<WriteSharedCommandParameters> {
    override fun execute() {
        val pkg = parameters.packageName.get()
        val simple = parameters.simpleName.get()
        val cmdName = parameters.commandName.get()
        val names = parameters.parameterNames.get()
        val types = parameters.parameterTypes.get()

        val parameters =
            names.zip(types).map { (n, t) ->
                ParameterSignature(n, t)
            }

        val fileSpec =
            FileSpec
                .builder(pkg, simple)
                .addType(CommandInterfaceGenerator.generateCommandInterface(simple, cmdName, parameters))
                .build()
        fileSpec.writeTo(
            this.parameters.outputDir
                .get()
                .asFile,
        )
    }
}

internal interface WriteFileSpecParameters : WorkParameters {
    val outputDir: DirectoryProperty
    val packageName: Property<String>
    val fileName: Property<String>
    val fileContents: Property<String>
}

internal abstract class WriteFileSpecAction : WorkAction<WriteFileSpecParameters> {
    override fun execute() {
        val outDir = parameters.outputDir.get().asFile
        val pkg = parameters.packageName.get()
        val fileName = parameters.fileName.get()
        val contents = parameters.fileContents.get()

        val pkgDir =
            if (pkg.isEmpty()) outDir else outDir.resolve(pkg.replace('.', java.io.File.separatorChar))
        pkgDir.mkdirs()
        val outFile = pkgDir.resolve("$fileName.kt")
        outFile.writeText(contents)
    }
}
