package xyz.dussim.viessmann.client.equipment

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import xyz.dussim.viessmann.client.core.ViessmannApiException
import xyz.dussim.viessmann.client.testing.jsonHeaders
import xyz.dussim.viessmann.client.testing.testViessmannClientConfig
import xyz.dussim.viessmann.client.testing.testViessmannHttpClient

val EquipmentClientTest by testSuite {
    test("builds installations query parameters") {
        lateinit var request: HttpRequestData
        val client =
            equipmentClient {
                request = it
                respond(
                    """
                    {
                      "cursor": { "next": "cursor-2" },
                      "data": []
                    }
                    """.trimIndent(),
                    headers = jsonHeaders(),
                )
            }

        client
            .getInstallations(
                InstallationsQuery(
                    cursor = "cursor-1",
                    includeGateways = true,
                    limit = 10,
                ),
            ).cursor
            ?.next shouldBe "cursor-2"

        request.method shouldBe HttpMethod.Get
        request.url.encodedPath shouldBe "/iot/v2/equipment/installations"
        request.url.parameters["cursor"] shouldBe "cursor-1"
        request.url.parameters["includeGateways"] shouldBe "true"
        request.url.parameters["limit"] shouldBe "10"
    }

    test("decodes installation status envelope") {
        val client =
            equipmentClient {
                respond(
                    """
                    {
                      "data": {
                        "aggregatedStatus": "Error",
                        "lastChangedAt": "2026-03-13T10:53:42.4986781Z",
                        "gatewaysStatuses": []
                      }
                    }
                    """.trimIndent(),
                    headers = jsonHeaders(),
                )
            }

        client.getInstallationStatus("42").aggregatedStatus.toString() shouldBe "Error"
    }

    test("loads nested installation gateway devices") {
        lateinit var request: HttpRequestData
        val client =
            equipmentClient {
                request = it
                respond("""{"data":[]}""", headers = jsonHeaders())
            }

        client.getInstallationGatewayDevices("100", "serial-1").data.size shouldBe 0
        request.url.encodedPath shouldBe "/iot/v2/equipment/installations/100/gateways/serial-1/devices"
    }

    test("maps equipment api errors") {
        val client =
            equipmentClient {
                respond(
                    """
                    {
                      "statusCode": 404,
                      "errorType": "NOT_FOUND",
                      "message": "Gateway not found."
                    }
                    """.trimIndent(),
                    status = HttpStatusCode.NotFound,
                    headers = jsonHeaders(),
                )
            }

        val exception =
            runCatching {
                client.getGateway("serial-1")
            }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

        exception.error?.message shouldBe "Gateway not found."
    }
}

private fun equipmentClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): EquipmentClient {
    val config = testViessmannClientConfig()
    return DefaultEquipmentClient(testViessmannHttpClient(config, handler), config)
}
