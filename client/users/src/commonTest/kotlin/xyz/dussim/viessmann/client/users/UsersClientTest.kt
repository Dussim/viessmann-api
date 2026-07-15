package xyz.dussim.viessmann.client.users

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import xyz.dussim.viessmann.api.users.CheckPasswordRequest
import xyz.dussim.viessmann.api.users.CheckPasswordResponse
import xyz.dussim.viessmann.api.users.ConsumerAddress
import xyz.dussim.viessmann.api.users.CreateConsumerRequest
import xyz.dussim.viessmann.api.users.UserType
import xyz.dussim.viessmann.api.users.ValidateAddressRequest
import xyz.dussim.viessmann.client.core.ViessmannApiException
import xyz.dussim.viessmann.client.testing.jsonHeaders
import xyz.dussim.viessmann.client.testing.testViessmannClientConfig
import xyz.dussim.viessmann.client.testing.testViessmannHttpClient

val UsersClientTest by testSuite {
    test("posts password validation request") {
        lateinit var request: HttpRequestData
        val client =
            usersClient {
                request = it
                respond("""{"valid":true}""", headers = jsonHeaders())
            }

        client.checkUserPassword(CheckPasswordRequest("Test1234!", UserType.Customer)) shouldBe
            CheckPasswordResponse(valid = true)

        request.method shouldBe HttpMethod.Post
        request.url.encodedPath shouldBe "/users/v1/users/check-user-password"
    }

    test("sends forwarded header for consumer creation") {
        lateinit var request: HttpRequestData
        val client =
            usersClient {
                request = it
                respond(
                    """
                    {
                      "data": {
                        "id": "user-1",
                        "type": "users",
                        "attributes": {
                          "loginId": "john@example.com"
                        }
                      }
                    }
                    """.trimIndent(),
                    headers = jsonHeaders(),
                )
            }

        val response =
            client.createConsumer(
                request =
                    CreateConsumerRequest(
                        loginId = "john@example.com",
                        optIn = true,
                        languageCode = "en",
                        address = ConsumerAddress(countryCode = "DE"),
                    ),
                forwarded = "for=127.0.0.1",
            )

        response.data.id shouldBe "user-1"
        request.headers[HttpHeaders.Forwarded] shouldBe "for=127.0.0.1"
        request.url.encodedPath shouldBe "/users/v2/users/consumer"
    }

    test("posts address validation request") {
        lateinit var request: HttpRequestData
        val client =
            usersClient {
                request = it
                respond("""{"isValid":true}""", headers = jsonHeaders())
            }

        client
            .validateAddress(
                ValidateAddressRequest(
                    street = "Main Street",
                    houseNumber = "1",
                    postalCode = "12345",
                    city = "Berlin",
                    countryCode = "DE",
                ),
            ).isValid shouldBe true

        request.url.encodedPath shouldBe "/users/v2/validate-address"
        request.method shouldBe HttpMethod.Post
    }

    test("maps errors returned from users endpoints") {
        val client =
            usersClient {
                respond(
                    """
                    {
                      "statusCode": 422,
                      "errorType": "VALIDATION_ERROR",
                      "message": "Invalid data."
                    }
                    """.trimIndent(),
                    status = HttpStatusCode.UnprocessableEntity,
                    headers = jsonHeaders(),
                )
            }

        val exception =
            runCatching {
                client.validateAddress(
                    ValidateAddressRequest("Main Street", "1", "12345", "Berlin", "DE"),
                )
            }.exceptionOrNull().shouldBeInstanceOf<ViessmannApiException>()

        exception.error?.message shouldBe "Invalid data."
    }
}

private fun usersClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): UsersClient {
    val config = testViessmannClientConfig(accessToken = null)
    return DefaultUsersClient(testViessmannHttpClient(config, handler), config)
}
