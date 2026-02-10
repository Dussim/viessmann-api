package xyz.dussim.buildlogic

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import xyz.dussim.buildlogic.internal.CommandInterfaceGenerator
import xyz.dussim.buildlogic.internal.YamlFeatureInterfaceGenerator
import xyz.dussim.buildlogic.internal.identifySharedCommands

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
        }
}

abstract class GenerateFeatureInterfacesFromYamlExtension {
    abstract val featuresYamls: DirectoryProperty

    abstract val generatedSources: DirectoryProperty

    abstract val packageName: Property<String>
}

@DisableCachingByDefault
abstract class GenerateFeatureInterfacesFromYamlTask : DefaultTask() {
    @get:InputDirectory
    abstract val featuresYamls: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedSources: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    init {
        group = "build"
        description = "Generates feature interfaces from YAML OpenAPI specifications"
    }

    @TaskAction
    fun generate() {
        val inputDir = featuresYamls.get().asFile
        val outputDir = generatedSources.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val inputFiles =
            inputDir
                .listFiles { file -> file.extension == "yaml" }
                ?.sortedBy { it.name }
                ?: emptyList()

        logger.lifecycle("Found ${inputFiles.size} YAML feature files")

        val generator = YamlFeatureInterfaceGenerator(packageName.get(), logger)

        // First pass: parse all features
        logger.lifecycle("Pass 1: Parsing YAML features...")
        val parsedFeatures = inputFiles.mapNotNull { file -> generator.parseYamlFile(file) }

        // Collect all command signatures
        val commandSignatures =
            parsedFeatures.flatMap { feature ->
                feature.commands.map { command -> generator.getCommandSignature(command) }
            }

        val sharedCommandsMap = identifySharedCommands(commandSignatures, packageName.get())
        logger.lifecycle("Found ${sharedCommandsMap.size} shared command signatures")

        generator.useSharedCommands(sharedCommandsMap)

        // Generate shared command interfaces
        sharedCommandsMap.forEach { (sig, className) ->
            val fileSpec =
                FileSpec
                    .builder(className.packageName, className.simpleName)
                    .addType(CommandInterfaceGenerator.generateCommandInterface(className.simpleName, sig.name, sig.parameters))
                    .build()
            fileSpec.writeTo(outputDir)
        }

        // Second pass: generate feature files
        logger.lifecycle("Pass 2: Generating feature files...")
        var success = 0
        var duplicate = 0

        for (feature in parsedFeatures) {
            val outputFile = outputDir.resolve("${feature.className}.kt")
            if (outputFile.exists()) {
                duplicate++
                logger.warn("File already exists: $outputFile")
            } else {
                val fileSpec = generator.generate(feature)
                fileSpec.writeTo(outputDir)
                success++
            }
        }

        logger.lifecycle(
            "Results: $success success, $duplicate duplicate out of ${inputFiles.size} total",
        )
        logger.lifecycle("Shared commands: ${sharedCommandsMap.size}")
        logger.lifecycle("Output directory: $outputDir")
    }
}
