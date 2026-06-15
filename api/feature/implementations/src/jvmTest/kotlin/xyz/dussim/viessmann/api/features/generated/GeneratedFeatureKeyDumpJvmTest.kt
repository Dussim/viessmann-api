package xyz.dussim.viessmann.api.features.generated

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.feature.api.CommandKeyReport
import xyz.dussim.viessmann.feature.api.FeatureKeyReport
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.json
import xyz.dussim.viessmann.feature.api.toKeyLogString
import xyz.dussim.viessmann.feature.api.toKeyReports
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class GeneratedFeatureKeyDumpJvmTest :
    FunSpec({
        test("writes generated feature key dump and similarity report") {
            val reports = loadGeneratedFeatureExamples().toKeyReports()

            reports.isNotEmpty() shouldBe true
            reports.any { it.propertyKeys.isNotEmpty() } shouldBe true
            reports.any { it.commands.isNotEmpty() } shouldBe true

            val reportDirectory = Path("build/reports/feature-key-dump")
            reportDirectory.createDirectories()
            reportDirectory.resolve("feature-key-dump.txt").writeText(reports.distinctKeyReports().toKeyLogString())
            reportDirectory.resolve("feature-key-similarity.txt").writeText(reports.toSimilarityReport())

            println("Feature key dump written to ${reportDirectory.resolve("feature-key-dump.txt")}")
            println("Feature key similarity report written to ${reportDirectory.resolve("feature-key-similarity.txt")}")
        }
    })

@Serializable
private data class GeneratedFeatureExamples(
    val data: List<ViessmannFeature>,
)

private fun loadGeneratedFeatureExamples(): List<ViessmannFeature> {
    val content =
        GeneratedFeatureKeyDumpJvmTest::class.java.classLoader
            .getResource("all_features.json")
            ?.readText()
            ?: error("Generated all_features.json resource was not found")
    return json.decodeFromString<GeneratedFeatureExamples>(content).data
}

private fun List<FeatureKeyReport>.toSimilarityReport(): String {
    val reports = this
    return buildString {
        appendLine("Generated feature key similarity report")
        appendLine("features: ${reports.size}")
        appendLine("wildcard features: ${reports.map { it.wildcardFeature }.distinct().size}")
        appendLine("unique wildcard key shapes: ${reports.distinctKeyReports().size}")
        appendLine("features with commands: ${reports.count { it.commands.isNotEmpty() }}")
        appendLine()

        appendSignatureGroups(
            title = "Shared property key sets",
            groups = reports.groupedBySignature { it.propertySignature() },
            signatureName = "properties",
        )
        appendLine()

        appendSignatureGroups(
            title = "Shared command key sets",
            groups = reports.groupedBySignature { it.commandKeySignature() },
            signatureName = "commands",
            includeEmptySignature = false,
        )
        appendLine()

        appendSignatureGroups(
            title = "Shared command parameter and constraint signatures",
            groups = reports.groupedBySignature { it.commandStructureSignature() },
            signatureName = "command signature",
            includeEmptySignature = false,
        )
        appendLine()

        appendCommandSignatureGroups(
            "Reusable command signatures",
            reports
                .flatMap { feature ->
                    feature.commands.map { command ->
                        CommandOccurrence(
                            feature = feature,
                            signature = command.structureSignature(),
                        )
                    }
                }.groupBy { it.signature }
                .values
                .toList(),
        )
        appendLine()

        appendCommandSignatureGroups(
            "Reusable parameter constraint shapes",
            reports
                .flatMap { feature ->
                    feature.commands.flatMap { command ->
                        command.parameters.map { parameter ->
                            CommandOccurrence(
                                feature = feature,
                                signature = "${command.name}.${parameter.name}[${parameter.constraintKeys.joinSignatureKeys()}]",
                            )
                        }
                    }
                }.groupBy { it.signature }
                .values
                .toList(),
        )
    }.trimEnd()
}

private data class CommandOccurrence(
    val feature: FeatureKeyReport,
    val signature: String,
)

private fun List<FeatureKeyReport>.groupedBySignature(signature: (FeatureKeyReport) -> String): List<List<FeatureKeyReport>> = groupBy(signature).values.toList()

private fun List<FeatureKeyReport>.distinctKeyReports(): List<FeatureKeyReport> =
    distinctBy { report ->
        "${report.wildcardFeature}|${report.propertySignature()}|${report.commandStructureSignature()}"
    }

private fun StringBuilder.appendSignatureGroups(
    title: String,
    groups: List<List<FeatureKeyReport>>,
    signatureName: String,
    includeEmptySignature: Boolean = true,
) {
    appendLine(title)
    groups
        .filter { group ->
            val signature = group.first().signature(signatureName)
            group.distinctWildcardFeatures().size > 1 &&
                (includeEmptySignature || signature.isNotEmpty())
        }.sortedWith(compareByDescending<List<FeatureKeyReport>> { it.distinctWildcardFeatures().size }.thenByDescending { it.size })
        .take(20)
        .forEach { group ->
            val wildcards = group.distinctWildcardFeatures()
            appendLine(
                "- ${wildcards.size} wildcard features, ${group.size} feature occurrences, " +
                    "$signatureName: ${group.first().signature(signatureName).displaySignature()}",
            )
            appendLine("  examples: ${wildcards.take(8).joinToString()}")
        }
}

private fun StringBuilder.appendCommandSignatureGroups(
    title: String,
    groups: List<List<CommandOccurrence>>,
) {
    appendLine(title)
    groups
        .filter { group -> group.distinctOccurrenceWildcardFeatures().size > 1 }
        .sortedWith(
            compareByDescending<List<CommandOccurrence>> { it.distinctOccurrenceWildcardFeatures().size }
                .thenByDescending { it.size },
        ).take(30)
        .forEach { group ->
            val wildcards = group.distinctOccurrenceWildcardFeatures()
            appendLine(
                "- ${wildcards.size} wildcard features, ${group.size} command occurrences, " +
                    "signature: ${group.first().signature}",
            )
            appendLine("  examples: ${wildcards.take(8).joinToString()}")
        }
}

private fun List<FeatureKeyReport>.distinctWildcardFeatures(): List<String> = map { it.wildcardFeature }.distinct().sorted()

private fun List<CommandOccurrence>.distinctOccurrenceWildcardFeatures(): List<String> = map { it.feature.wildcardFeature }.distinct().sorted()

private fun FeatureKeyReport.signature(signatureName: String): String =
    when (signatureName) {
        "properties" -> propertySignature()
        "commands" -> commandKeySignature()
        else -> commandStructureSignature()
    }

private fun String.displaySignature(): String = ifEmpty { "<none>" }

private fun FeatureKeyReport.propertySignature(): String = propertyKeys.joinSignatureKeys()

private fun FeatureKeyReport.commandKeySignature(): String = commandKeys.joinSignatureKeys()

private fun FeatureKeyReport.commandStructureSignature(): String = commands.joinToString(separator = " | ") { it.structureSignature() }

private fun CommandKeyReport.structureSignature(): String =
    "$name(${parameters.joinToString { parameter -> "${parameter.name}[${parameter.constraintKeys.joinSignatureKeys()}]" }})"

private fun List<String>.joinSignatureKeys(): String = joinToString(separator = "+")
