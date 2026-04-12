package xyz.dussim.viessmann.client.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import xyz.dussim.viessmann.api.features.FeatureFilterRequest
import xyz.dussim.viessmann.api.features.GatewayFeatureFilterRequest
import xyz.dussim.viessmann.client.core.ViessmannApiException
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.installViessmannClient

class FeaturesClientTest :
    FunSpec({
        test("builds installation feature filters as query params") {
            lateinit var request: HttpRequestData
            val client =
                featuresClient {
                    request = it
                    respond(featureListResponse(), headers = jsonHeaders())
                }

            client
                .getInstallationFeatures(
                    installationId = "100",
                    query =
                        FeatureQuery(
                            filter = listOf("heating.circuits.*", "heating.dhw.*"),
                            skipDisabled = true,
                        ),
                ).single()
                .feature shouldBe "heating.dhw.temperature.main"

            request.method shouldBe HttpMethod.Get
            request.url.encodedPath shouldBe "/iot/v2/features/installations/100/features"
            request.url.parameters.getAll("filter") shouldBe listOf("heating.circuits.*", "heating.dhw.*")
            request.url.parameters["skipDisabled"] shouldBe "true"
        }

        test("posts gateway filter body") {
            lateinit var request: HttpRequestData
            val client =
                featuresClient {
                    request = it
                    respond(featureListResponse(), headers = jsonHeaders())
                }

            client
                .filterGatewayFeatures(
                    installationId = "100",
                    gatewaySerial = "serial-1",
                    request = GatewayFeatureFilterRequest(includeDevicesFeatures = true),
                ).size shouldBe 1

            request.method shouldBe HttpMethod.Post
            request.url.encodedPath shouldBe "/iot/v2/features/installations/100/gateways/serial-1/features/filter"
        }

        test("posts device command payload") {
            lateinit var request: HttpRequestData
            val client =
                featuresClient {
                    request = it
                    respond(
                        """{"data":{"success":"True","message":null,"reason":null}}""",
                        headers = jsonHeaders(),
                    )
                }

            val response =
                client.executeDeviceCommand(
                    installationId = "100",
                    gatewaySerial = "serial-1",
                    deviceId = "device-1",
                    featureName = "heating.dhw.temperature.main",
                    commandName = "setTargetTemperature",
                    body =
                        buildJsonObject {
                            put("temperature", 55)
                        },
                )

            response.data.success shouldBe "True"
            request.url.encodedPath shouldBe
                "/iot/v2/features/installations/100/gateways/serial-1/devices/device-1/" +
                "features/heating.dhw.temperature.main/commands/setTargetTemperature"
        }

        test("maps feature endpoint errors") {
            val client =
                featuresClient {
                    respond(
                        """
                        {
                          "statusCode": 403,
                          "errorType": "NOT_ALLOWED",
                          "message": "Write access is required."
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.Forbidden,
                        headers = jsonHeaders(),
                    )
                }

            val exception =
                runCatching {
                    client.filterInstallationFeatures("100", FeatureFilterRequest())
                }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

            exception.error?.message shouldBe "Write access is required."
        }
    })

private fun featuresClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): FeaturesClient {
    val config =
        ViessmannClientConfig(
            apiBaseUrl = "https://example.com",
            accessTokenProvider = { "token-123" },
        )
    val httpClient =
        HttpClient(MockEngine(handler)) {
            installViessmannClient(config)
        }

    return DefaultFeaturesClient(httpClient, config)
}

private fun featureListResponse() =
    """
    {
      "data": [
        {
          "feature": "heating.dhw.temperature.main",
          "isEnabled": true,
          "isReady": true,
          "apiVersion": 1,
          "timestamp": "2026-03-13T10:53:42.4986781Z",
          "uri": "https://example.com/iot/v2/features/installations/100/features/heating.dhw.temperature.main",
          "properties": {},
          "commands": {}
        }
      ]
    }
    """.trimIndent()

private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
