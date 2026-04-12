package xyz.dussim.viessmann.client.auth

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
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import xyz.dussim.viessmann.client.core.ViessmannApiException
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.installViessmannClient

class AuthClientTest :
    FunSpec({
        test("validates session with bearer token") {
            lateinit var request: HttpRequestData
            val client =
                authClient {
                    request = it
                    respond("""{"x-uuid":"user-1","roles":"role-1"}""", headers = jsonHeaders())
                }

            client.validateSession().xUuid shouldBe "user-1"
            request.headers[HttpHeaders.Authorization] shouldBe "Bearer token-123"
            request.url.encodedPath shouldBe "/auth/v1/validate-session"
        }

        test("sends route policy forwarding headers") {
            lateinit var request: HttpRequestData
            val client =
                authClient {
                    request = it
                    respond("""{"x-uuid":"user-1","roles":"role-1"}""", headers = jsonHeaders())
                }

            client
                .validateWithRouteBasedPolicies(
                    RoutePolicyValidationRequest(
                        forwardedHost = "app.example.com",
                        forwardedMethod = "GET",
                        forwardedProto = "https",
                        forwardedUri = "/dashboard",
                    ),
                ).roles shouldBe "role-1"

            request.headers["x-forwarded-host"] shouldBe "app.example.com"
            request.headers["x-forwarded-uri"] shouldBe "/dashboard"
        }

        test("returns raw saml response wrapper") {
            lateinit var request: HttpRequestData
            val client =
                authClient {
                    request = it
                    respond(
                        "<html>redirect</html>",
                        status = HttpStatusCode.MovedPermanently,
                        headers = headersOf(HttpHeaders.Location, "https://app.example.com"),
                    )
                }

            val response =
                client.getSamlSsoRequest(
                    SamlSsoRequest(
                        originalHost = "app.example.com",
                        appId = "vitoguide",
                        redirectUrl = "https://app.example.com",
                    ),
                )

            response.statusCode shouldBe 301
            response.body shouldBe "<html>redirect</html>"
            request.url.parameters["appId"] shouldBe "vitoguide"
            request.headers["x-original-host"] shouldBe "app.example.com"
        }

        test("maps auth json errors") {
            val client =
                authClient {
                    respond(
                        """
                        {
                          "statusCode": 403,
                          "errorType": "NOT_ALLOWED",
                          "message": "App https://myapp.com is not allowed."
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.Forbidden,
                        headers = jsonHeaders(),
                    )
                }

            val exception =
                runCatching {
                    client.getCsrfJwks()
                }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

            exception.error?.message shouldBe "App https://myapp.com is not allowed."
        }
    })

private fun authClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): AuthClient {
    val config =
        ViessmannClientConfig(
            apiBaseUrl = "https://example.com",
            accessTokenProvider = { "token-123" },
        )
    val httpClient =
        HttpClient(MockEngine(handler)) {
            installViessmannClient(config)
        }

    return DefaultAuthClient(httpClient, config)
}

private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
