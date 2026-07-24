package xyz.dussim.buildlogic.internal

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.gradle.api.logging.Logging

val FeatureInterfaceGeneratorTest by testSuite {
    test("removes opposite commands when a full Boolean setter replacement exists") {
        val replacements =
            listOf(
                Triple("activate", "deactivate", "setActive"),
                Triple("enable", "disable", "setEnabled"),
                Triple("grant", "revoke", "setUseApproved"),
            )

        replacements.forEach { (positive, negative, setter) ->
            val generator = FeatureInterfaceGenerator("test.generated", Logging.getLogger("FeatureInterfaceGeneratorTest"))
            val feature = generator.parseFeature(featureJson(positive, negative, setter))

            generator.getCommandSignatures(feature).map(CommandSignature::name) shouldContainExactly listOf(setter)

            val source = generator.generate(feature).toString()
            source shouldContain "public val $setter"
            source shouldNotContain "public val $positive"
            source shouldNotContain "public val $negative"
            source shouldNotContain "CommandName(\"$positive\")"
            source shouldNotContain "CommandName(\"$negative\")"
        }
    }

    test("retains opposite commands when the replacement triplet is incomplete") {
        val generator = FeatureInterfaceGenerator("test.generated", Logging.getLogger("FeatureInterfaceGeneratorTest"))
        val feature = generator.parseFeature(featureJson("activate", "setActive"))

        generator.getCommandSignatures(feature).map(CommandSignature::name) shouldContainExactly listOf("activate", "setActive")

        val source = generator.generate(feature).toString()
        source shouldContain "public val activate"
        source shouldContain "public val setActive"
    }
}

private fun featureJson(vararg commandNames: String): JsonObject {
    val json =
        Json.parseToJsonElement(
            buildString {
                append("""{"feature":"test.feature","_metadata":{"properties":[{"name":"active","type":"boolean"}],"commands":[""")
                append(
                    commandNames.joinToString(",") { commandName ->
                        val parameter =
                            when (commandName) {
                                "setActive" -> "active"
                                "setEnabled" -> "enabled"
                                "setUseApproved" -> "useApproved"
                                else -> null
                            }
                        val parameters =
                            if (parameter == null) {
                                "[]"
                            } else {
                                "[{\"name\":\"$parameter\",\"type\":\"boolean\"}]"
                            }
                        "{\"name\":\"$commandName\",\"parameters\":$parameters}"
                    },
                )
                append("]}}}")
            },
        )
    return json.jsonObject
}
