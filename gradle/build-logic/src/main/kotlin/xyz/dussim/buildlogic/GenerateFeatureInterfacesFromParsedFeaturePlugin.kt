package xyz.dussim.buildlogic

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import xyz.dussim.buildlogic.internal.CommandSignature
import xyz.dussim.buildlogic.internal.FeatureInterfaceGenerator
import xyz.dussim.buildlogic.internal.FeatureMerger
import java.io.File

abstract class GenerateFeatureInterfacesFromParsedFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        target.run {
            val extension = extensions.create<GenerateFeatureInterfacesFromParsedFeaturesExtension>("generateFeatureInterfaces")
            val generateFeatureInterfaces =
                tasks.register("generateFeatureInterfaces", GenerateFeatureInterfacesFromParsedFeaturesTask::class.java) {
                    featuresJsons = extension.featuresJsons
                    generatedSources = extension.generatedSources
                    packageName = extension.packageName
                    ignoredFeatures = extension.ignoredFeatures.orElse(emptySet())
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

abstract class GenerateFeatureInterfacesFromParsedFeaturesExtension {
    abstract val featuresJsons: DirectoryProperty

    abstract val generatedSources: DirectoryProperty

    abstract val packageName: Property<String>

    abstract val ignoredFeatures: SetProperty<String>
}

@DisableCachingByDefault
abstract class GenerateFeatureInterfacesFromParsedFeaturesTask : DefaultTask() {
    @get:InputDirectory
    abstract val featuresJsons: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedSources: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val ignoredFeatures: SetProperty<String>

    init {
        group = "build"
        description = "Generates feature interfaces from parsed features"
    }

    @TaskAction
    fun generate() {
        val ignoredFeatures = ignoredFeatures.get()
        val json =
            Json {
                explicitNulls = false
                encodeDefaults = false
                useAlternativeNames = false

                ignoreUnknownKeys = true
            }

        val inputFiles =
            featuresJsons
                .get()
                .asFileTree.files
                .sortedBy { it.absolutePath }

        val outputDir = generatedSources.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val allFeatures =
            inputFiles
                .flatMap(readFeaturesFromFile(json))
                .filterIsInstance<JsonObject>()

        val mergedFeatures =
            FeatureMerger
                .mergeAll(allFeatures)
                .filter { (it["feature"]?.jsonPrimitive?.content ?: "") !in ignoredFeatures }

        val generator = FeatureInterfaceGenerator(packageName.get(), logger)

        // Collect all command signatures
        val commandSignatures =
            mergedFeatures.flatMap { feature ->
                feature["commands"]?.jsonObject?.map { (name, command) ->
                    generator.getCommandSignature(name, command.jsonObject)
                } ?: emptyList()
            }

        // Identify shareable commands
        val shareableSignatures =
            commandSignatures
                .groupBy { it }
                .filter { it.value.size > 1 }
                .map { it.key }

        val allUniqueSignatures = commandSignatures.distinct()
        val signaturesByName = allUniqueSignatures.groupBy { it.name }

        val sharedCommandsPackage = "${packageName.get()}.commands"
        val sharedCommandsMap: Map<CommandSignature, ClassName> =
            shareableSignatures.associateWith { sig ->
                val useShortName = (signaturesByName[sig.name]?.size ?: 0) == 1
                val interfaceName = if (useShortName) sig.capitalizedName else sig.interfaceName
                ClassName(sharedCommandsPackage, interfaceName)
            }

        generator.useSharedCommands(sharedCommandsMap)

        // Generate shared command interfaces
        sharedCommandsMap.forEach { (sig, className) ->
            val fileSpec =
                FileSpec
                    .builder(className.packageName, className.simpleName)
                    .addType(generator.generateCommandInterface(className.simpleName, sig.name, sig.parameters))
                    .build()
            fileSpec.writeTo(outputDir)
        }

        mergedFeatures.forEach { feature ->
            val fileSpec = generator.generate(feature)
            fileSpec.writeTo(outputDir)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun readFeaturesFromFile(json: Json) =
    { file: File ->
        file.inputStream().use {
            json
                .decodeFromStream(
                    deserializer = JsonObject.serializer(),
                    stream = it,
                ).getValue("data")
                .jsonArray
        }
    }
