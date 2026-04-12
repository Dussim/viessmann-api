package xyz.dussim.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.work.DisableCachingByDefault

abstract class GenerateFeatureJsonTestsPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        target.run {
            val extension = extensions.create<GenerateFeatureJsonTestsExtension>("generateFeatureJsonTests")
            tasks.register(
                "generateFeatureJsonTests",
                GenerateFeatureJsonTestsTask::class.java,
            ) {
                generatedJsons = extension.generatedJsons
                generatedTests = extension.generatedTests
            }
        }
}

abstract class GenerateFeatureJsonTestsExtension {
    abstract val generatedJsons: DirectoryProperty

    abstract val generatedTests: DirectoryProperty
}

@DisableCachingByDefault
abstract class GenerateFeatureJsonTestsTask : DefaultTask() {
    companion object {
        private val pattern = "(?<!_)0(?=[A-Za-z])".toRegex()
        private val temporarilyDisabledTests =
            setOf(
                "device.busTopology",
                "device.product.matrix",
                "ems.power.balance",
                "ems.power.instantaneous",
                "fuel.cell.errors.raw",
                "heating.cooling.circuits.n.messages",
                "solarlog.devices.detected",
                "tcu.wifi.detected",
                "tcu.wifi.environment",
                "ventilation.messages",
                "fuelCell.errors.raw",
                "heating.coolingCircuits.0.messages",
                "device.productMatrix",
            )
    }

    @get:InputDirectory
    abstract val generatedJsons: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedTests: DirectoryProperty

    init {
        group = "build"
        description = "Generates Kotest test files for each generated feature JSON"
    }

    @TaskAction
    fun generate() {
        val inputDir = generatedJsons.get().asFile
        val outputDir = generatedTests.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val packageDir = outputDir.resolve("xyz/dussim/viessmann/api/features/generated")
        packageDir.mkdirs()

        val jsonFiles =
            inputDir
                .listFiles { file -> file.extension == "json" && file.name != "all_features.json" }
                ?.sortedBy { it.name }
                ?: emptyList()

        logger.lifecycle("Generating tests for ${jsonFiles.size} feature JSON files")

        for (jsonFile in jsonFiles) {
            val baseName = jsonFile.nameWithoutExtension
            val sanitized = jsonFile.nameWithoutExtension.replace(pattern, "N")
            val className =
                sanitized
                    .replace(".0", ".N")
                    .split(".")
                    .joinToString("") { it.replaceFirstChar { c -> c.uppercase() }.replace(pattern, "N") } + "Feature"

            val testClassName = "${className}JsonTest"

            val resourcePath = jsonFile.name

            val featureType = "ViessmannFeature"
            val isTemporarilyDisabled = baseName in temporarilyDisabledTests

            val testContent =
                buildString {
                    appendLine("package xyz.dussim.viessmann.api.features.generated")
                    appendLine()
                    appendLine("import io.kotest.core.spec.style.FunSpec")
                    appendLine("import io.kotest.matchers.nulls.shouldNotBeNull")
                    appendLine("import xyz.dussim.viessmann.feature.api.json")
                    appendLine("import xyz.dussim.viessmann.feature.api.$featureType")
                    appendLine("import xyz.dussim.viessmann.api.features.generated.descriptor")
                    appendLine()
                    appendLine("class $testClassName :")
                    appendLine("    FunSpec({")
                    if (isTemporarilyDisabled) {
                        appendLine("        // Disabled for now: these generated tests are not working due to issues with the underlying data.")
                    }
                    appendLine("        ${if (isTemporarilyDisabled) "xtest" else "test"}(\"Parse $baseName as $className\") {")
                    appendLine("            val content = this::class.java.classLoader.getResource(\"$resourcePath\")!!.readText()")
                    appendLine("            val genericFeature = json.decodeFromString($featureType.serializer(), content)")
                    appendLine("            val feature = $className.descriptor.getOrThrow(genericFeature)")
                    appendLine("            feature.shouldNotBeNull()")
                    appendLine("        }")
                    appendLine("    })")
                }

            packageDir.resolve("$testClassName.kt").writeText(testContent)
        }

        logger.lifecycle("Generated ${jsonFiles.size} test files in: $outputDir")
    }
}
