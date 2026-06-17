package xyz.dussim.buildlogic

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
import org.gradle.kotlin.dsl.create
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import xyz.dussim.buildlogic.internal.YamlFeatureJsonGenerator
import java.time.LocalDate
import javax.inject.Inject

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
        }
}

abstract class GenerateFeatureJsonsFromYamlExtension {
    abstract val featuresYamls: DirectoryProperty

    abstract val generatedJsons: DirectoryProperty

    abstract val currentDate: Property<LocalDate>
}

@CacheableTask
abstract class GenerateFeatureJsonsFromYamlTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    abstract val featuresYamls: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedJsons: DirectoryProperty

    @get:Input
    abstract val currentDate: Property<LocalDate>

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    init {
        group = "build"
        description = "Generates valid JSON instances for each feature from YAML OpenAPI specifications"
    }

    @TaskAction
    fun generate() {
        val outputDir = generatedJsons.get().asFile
        outputDir.mkdirs()

        val inputFiles =
            featuresYamls
                .asFileTree
                .matching { include("*.yaml") }
                .files
                .sortedBy { it.name }

        logger.lifecycle("Found ${inputFiles.size} YAML feature files for JSON generation")

        val queue = workerExecutor.noIsolation()
        val now = currentDate.get()

        for (file in inputFiles) {
            queue.submit(GenerateFeatureJsonAction::class.java) {
                yamlFile.set(file)
                this.outputDir.set(outputDir)
                this.currentDate.set(now)
            }
        }

        // Wait for all per-file workers to finish before producing the combined file.
        workerExecutor.await()

        // Build the combined JSON by reading per-feature files written by workers.
        val prettyJson =
            Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }
        val allFeatures =
            outputDir
                .listFiles { f -> f.isFile && f.extension == "json" && f.name != "all_features.json" }
                ?.sortedBy { it.name }
                ?.map { prettyJson.parseToJsonElement(it.readText()) as JsonObject }
                ?: emptyList()

        val combinedJson =
            JsonObject(
                mapOf(
                    "data" to JsonArray(allFeatures),
                ),
            )
        val combinedFile = outputDir.resolve("all_features.json")
        combinedFile.writeText(prettyJson.encodeToString(JsonObject.serializer(), combinedJson))

        logger.lifecycle("Combined JSON with ${allFeatures.size} features written to: $combinedFile")
        logger.lifecycle("Output directory: $outputDir")
    }
}

internal interface GenerateFeatureJsonParameters : WorkParameters {
    val yamlFile: RegularFileProperty
    val outputDir: DirectoryProperty
    val currentDate: Property<LocalDate>
}

internal abstract class GenerateFeatureJsonAction : WorkAction<GenerateFeatureJsonParameters> {
    override fun execute() {
        val logger = Logging.getLogger(GenerateFeatureJsonAction::class.java)
        val file = parameters.yamlFile.get().asFile
        val outputDir = parameters.outputDir.get().asFile
        val now = parameters.currentDate.get()

        val generator = YamlFeatureJsonGenerator(logger)
        val result = generator.generateJsonForFile(file) ?: return

        val removalDate = result.removalDate
        if (removalDate != null && (removalDate.isBefore(now) || removalDate.isEqual(now))) {
            logger.warn(
                "Omitted generation of feature JSON for '${result.featureName}' as it is deprecated and past removal date ($removalDate)",
            )
            return
        }

        // Overwrite timestamp with a valid Instant-parseable value
        val fixedJson =
            JsonObject(
                result.json.toMutableMap().apply {
                    put("timestamp", JsonPrimitive("2020-01-01T00:00:01Z"))
                },
            )

        val prettyJson =
            Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }
        val outputFile = outputDir.resolve("${result.featureName.replace("{}", "0")}.json")
        outputFile.writeText(prettyJson.encodeToString(JsonObject.serializer(), fixedJson))
    }
}
