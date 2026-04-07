package xyz.dussim.buildlogic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
import org.gradle.kotlin.dsl.create
import org.gradle.work.DisableCachingByDefault
import xyz.dussim.buildlogic.internal.YamlFeatureJsonGenerator
import java.time.LocalDate

abstract class GenerateFeatureJsonsFromYamlPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        target.run {
            val extension = extensions.create<GenerateFeatureJsonsFromYamlExtension>("generateFeatureJsonsFromYaml")
            tasks.register(
                "generateFeatureJsonsFromYaml",
                GenerateFeatureJsonsFromYamlTask::class.java,
            ) {
                featuresYamls = extension.featuresYamls
                generatedJsons = extension.generatedJsons
                currentDate = extension.currentDate
            }
            extension.currentDate.convention(LocalDate.now())
        }
}

abstract class GenerateFeatureJsonsFromYamlExtension {
    abstract val featuresYamls: DirectoryProperty

    abstract val generatedJsons: DirectoryProperty

    abstract val currentDate: Property<LocalDate>
}

@DisableCachingByDefault
abstract class GenerateFeatureJsonsFromYamlTask : DefaultTask() {
    @get:InputDirectory
    abstract val featuresYamls: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedJsons: DirectoryProperty

    @get:Input
    abstract val currentDate: Property<LocalDate>

    init {
        group = "build"
        description = "Generates valid JSON instances for each feature from YAML OpenAPI specifications"
    }

    private val prettyJson =
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

    @TaskAction
    fun generate() {
        val inputDir = featuresYamls.get().asFile
        val outputDir = generatedJsons.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val inputFiles =
            inputDir
                .listFiles { file -> file.extension == "yaml" }
                ?.sortedBy { it.name }
                ?: emptyList()

        logger.lifecycle("Found ${inputFiles.size} YAML feature files for JSON generation")

        val generator = YamlFeatureJsonGenerator(logger)

        var success = 0
        var failed = 0
        var omitted = 0
        val now = currentDate.get()
        val allFeatures = mutableListOf<JsonObject>()

        for (file in inputFiles) {
            val result = generator.generateJsonForFile(file)
            if (result != null) {
                val removalDate = result.removalDate
                if (removalDate != null && (removalDate.isBefore(now) || removalDate.isEqual(now))) {
                    omitted++
                    logger.warn("Omitted generation of feature JSON for '${result.featureName}' as it is deprecated and past removal date ($removalDate)")
                    continue
                }

                val featureName = result.featureName
                val json = result.json

                // Overwrite timestamp with a valid Instant-parseable value
                val fixedJson =
                    JsonObject(
                        json.toMutableMap().apply {
                            put("timestamp", JsonPrimitive("2020-01-01T00:00:01Z"))
                        },
                    )
                // Write individual feature JSON
                val outputFile = outputDir.resolve("${featureName.replace("{}", "0")}.json")
                outputFile.writeText(prettyJson.encodeToString(JsonObject.serializer(), fixedJson))
                allFeatures.add(fixedJson)
                success++
            } else {
                failed++
            }
        }

        // Write combined JSON with all features wrapped in ResponseData format
        val combinedJson =
            JsonObject(
                mapOf(
                    "data" to JsonArray(allFeatures),
                ),
            )
        val combinedFile = outputDir.resolve("all_features.json")
        combinedFile.writeText(prettyJson.encodeToString(JsonObject.serializer(), combinedJson))

        logger.lifecycle("JSON generation results: $success success, $failed failed, $omitted omitted due to removal date out of ${inputFiles.size} total")
        logger.lifecycle("Combined JSON with ${allFeatures.size} features written to: $combinedFile")
        logger.lifecycle("Output directory: $outputDir")
    }
}
