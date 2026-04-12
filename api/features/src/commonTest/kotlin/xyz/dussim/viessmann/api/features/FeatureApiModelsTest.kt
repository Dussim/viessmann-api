package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

class FeatureApiModelsTest :
    FunSpec({
        test("encodes gateway filter request") {
            val request =
                GatewayFeatureFilterRequest(
                    regex = "heating\\\\.circuits\\\\.%5b0-2%5d.operating.programs.\\\\w+$",
                    filter =
                        listOf(
                            "heating.circuits.0.operating.programs.active",
                            "heating.circuits.N.operating.programs.active",
                        ),
                    skipDisabled = true,
                    skipNotReady = true,
                    includeDevicesFeatures = true,
                )

            val encoded = json.encodeToString(request)

            encoded.contains("includeDevicesFeatures") shouldBe true
        }

        test("decodes command execution response") {
            val payload =
                """
                {
                  "data": {
                    "success": "True",
                    "message": null,
                    "reason": null
                  }
                }
                """.trimIndent()

            json.decodeFromString<CommandExecutionResponse>(payload) shouldBe
                CommandExecutionResponse(
                    CommandResult(
                        success = "True",
                        message = null,
                        reason = null,
                    ),
                )
        }
    })
