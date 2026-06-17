package xyz.dussim.buildlogic

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
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
import xyz.dussim.buildlogic.internal.YamlFeatureInterfaceGenerator
import xyz.dussim.buildlogic.internal.identifySharedCommands
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

        // First pass: parse all features in parallel. Each parseYamlFile call constructs its own
        // OpenAPIV3Parser instance, so parallel invocation is safe and CPU-bound.
        logger.lifecycle("Pass 1: Parsing YAML features...")
        val parsedFeatures =
            inputFiles
                .parallelStream()
                .map { file -> generator.parseYamlFile(file) }
                .filter { it != null }
                .map { it!! }
                .toList()

        // Collect all command signatures and identify shared ones (must remain sequential)
        val commandSignatures =
            parsedFeatures.flatMap { feature ->
                feature.commands.map { command -> generator.getCommandSignature(command) }
            }

        val sharedCommandsMap = identifySharedCommands(commandSignatures, pkg)
        logger.lifecycle("Found ${sharedCommandsMap.size} shared command signatures")

        generator.useSharedCommands(sharedCommandsMap)

        // Generate shared command interfaces in parallel via the Worker API
        val queue = workerExecutor.noIsolation()
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

        // workerExecutor.await() is implicit at the end of the @TaskAction
        logger.lifecycle(
            "Results: $submitted success, $duplicate duplicate, $omitted omitted due to removal date out of ${inputFiles.size} total",
        )
        logger.lifecycle("Shared commands: ${sharedCommandsMap.size}")
        logger.lifecycle("Output directory: $outputDir")
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
                xyz.dussim.buildlogic.internal.ParameterSignature(n, t)
            }

        val fileSpec =
            FileSpec
                .builder(pkg, simple)
                .addType(CommandInterfaceGenerator.generateCommandInterface(simple, cmdName, parameters))
                .build()
        fileSpec.writeTo(this.parameters.outputDir.get().asFile)
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
