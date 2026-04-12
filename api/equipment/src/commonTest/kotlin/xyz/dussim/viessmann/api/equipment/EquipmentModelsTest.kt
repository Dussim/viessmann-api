package xyz.dussim.viessmann.api.equipment

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.GatewayState
import xyz.dussim.viessmann.api.models.Cursor
import xyz.dussim.viessmann.api.models.ResponseData

private val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

class EquipmentModelsTest :
    FunSpec({
        test("decodes paged response data") {
            val payload =
                """
                {
                  "cursor": { "next": "MTIzNA==" },
                  "data": []
                }
                """.trimIndent()

            json.decodeFromString<ResponseData<String>>(payload) shouldBe
                ResponseData(
                    cursor = Cursor("MTIzNA=="),
                    data = emptyList(),
                )
        }

        test("decodes gateway and installation status responses") {
            val gatewayPayload =
                """
                {
                  "aggregatedStatus": "Error",
                  "lastChangedAt": "2026-03-13T10:53:41.7550347+00:00",
                  "gatewayStatus": {
                    "value": "Registered",
                    "lastChangedAt": "2026-03-13T10:53:41.6111710+00:00",
                    "isOnline": true,
                    "onlineChangedAt": "2026-03-13T10:53:41.6111710+00:00",
                    "isUpdating": false,
                    "hasError": false
                  },
                  "bmuStatuses": [
                    {
                      "deviceId": "0",
                      "value": "Connected",
                      "lastChangedAt": "2026-03-13T10:53:41.6111710+00:00"
                    }
                  ],
                  "boilerStatuses": []
                }
                """.trimIndent()

            val installationPayload =
                """
                {
                  "aggregatedStatus": "Error",
                  "lastChangedAt": "2026-03-13T10:53:42.4986781+00:00",
                  "gatewaysStatuses": [$gatewayPayload]
                }
                """.trimIndent()

            val gateway = json.decodeFromString<GatewayStatus>(gatewayPayload)
            val installation = json.decodeFromString<InstallationStatus>(installationPayload)

            gateway.aggregatedStatus shouldBe AggregatedStatus.Error
            gateway.gatewayStatus?.value shouldBe GatewayState.Registered
            installation.gatewaysStatuses.single() shouldBe gateway
        }
    })
