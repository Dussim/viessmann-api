package xyz.dussim.viessmann.client.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
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
import kotlinx.serialization.Serializable

@Serializable
private data class TestPayload(
    val value: String,
)

class ViessmannHttpClientTest :
    FunSpec({
        test("injects bearer token for authenticated requests") {
            lateinit var request: HttpRequestData
            val client =
                testClient("token-123") {
                    request = it
                    respond("""{"data":{"value":"ok"}}""", headers = jsonHeaders())
                }

            client
                .viessmannRequestData<TestPayload>(
                    config = testConfig("token-123"),
                    service = ViessmannService.Api,
                    method = HttpMethod.Get,
                    path = "/test",
                ).value shouldBe "ok"

            request.headers[HttpHeaders.Authorization] shouldBe "Bearer token-123"
        }

        test("decodes typed JSON response") {
            val client =
                testClient("token-123") {
                    respond("""{"value":"ok"}""", headers = jsonHeaders())
                }

            client
                .viessmannRequest<TestPayload>(
                    config = testConfig("token-123"),
                    service = ViessmannService.Api,
                    method = HttpMethod.Get,
                    path = "/test",
                ).value shouldBe "ok"
        }

        test("maps Viessmann JSON error responses") {
            val client =
                testClient("token-123") {
                    respond(
                        content =
                            """
                            {
                              "statusCode": 403,
                              "errorType": "NOT_ALLOWED",
                              "message": "App is not allowed."
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.Forbidden,
                        headers = jsonHeaders(),
                    )
                }

            val exception =
                runCatching {
                    client.viessmannRequest<TestPayload>(
                        config = testConfig("token-123"),
                        service = ViessmannService.Api,
                        method = HttpMethod.Get,
                        path = "/test",
                    )
                }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

            exception.statusCode shouldBe HttpStatusCode.Forbidden
            exception.error?.message shouldBe "App is not allowed."
        }

        test("falls back to raw body for non-json errors") {
            val client =
                testClient("token-123") {
                    respond(
                        content = "upstream failed",
                        status = HttpStatusCode.BadGateway,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
                    )
                }

            val exception =
                runCatching {
                    client.viessmannRequest<TestPayload>(
                        config = testConfig("token-123"),
                        service = ViessmannService.Api,
                        method = HttpMethod.Get,
                        path = "/test",
                    )
                }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

            exception.responseBody shouldBe "upstream failed"
            exception.error shouldBe null
        }

        test("fails fast when token is missing") {
            val client =
                testClient(null) {
                    respond("""{"value":"ok"}""", headers = jsonHeaders())
                }

            val exception =
                runCatching {
                    client.viessmannRequest<TestPayload>(
                        config = testConfig(null),
                        service = ViessmannService.Api,
                        method = HttpMethod.Get,
                        path = "/test",
                    )
                }.exceptionOrNull().shouldBeInstanceOf<MissingAccessTokenException>()

            exception.shouldHaveMessage("No Viessmann access token is available for an authenticated request")
        }
    })

private fun testClient(
    token: String?,
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient =
    HttpClient(MockEngine(handler)) {
        installViessmannClient(testConfig(token))
    }

private fun testConfig(token: String?) =
    ViessmannClientConfig(
        apiBaseUrl = "https://example.com",
        accessTokenProvider = { token },
    )

private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
