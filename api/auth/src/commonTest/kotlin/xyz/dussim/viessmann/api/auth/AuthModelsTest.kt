package xyz.dussim.viessmann.api.auth

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

private val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

val AuthModelsTest by testSuite {
    test("decodes validate response") {
        val payload =
            """
            {
              "x-uuid": "ef023110-12c9-4105-8c0b-e278438e787f",
              "roles": "Vitoguide.CustomerMain,Vitoguide.Default,Limas.Default"
            }
            """.trimIndent()

        json.decodeFromString<ValidateResponse>(payload) shouldBe
            ValidateResponse(
                xUuid = "ef023110-12c9-4105-8c0b-e278438e787f",
                roles = "Vitoguide.CustomerMain,Vitoguide.Default,Limas.Default",
            )
    }

    test("decodes csrf and saml payloads") {
        val jwksPayload =
            """
            {
              "keys": [
                {
                  "e": "AQAB",
                  "kty": "RSA",
                  "alg": "RS256",
                  "n": "abc123",
                  "use": "sig",
                  "kid": "key-1"
                }
              ]
            }
            """.trimIndent()
        val tokenPayload = """{"token":"jwt-token"}"""
        val validationPayload =
            """
            {
              "statusCode": 422,
              "errorType": "VALIDATION_ERROR",
              "message": "Invalid data provided. Refer to validationErrors for more details",
              "validationErrors": [
                {
                  "path": "x-original-host",
                  "type": "any.required",
                  "message": "\"x-original-host\" is required"
                }
              ]
            }
            """.trimIndent()
        val forbiddenPayload =
            """
            {
              "statusCode": 403,
              "errorType": "NOT_ALLOWED",
              "message": "App https://myapp.com is not allowed."
            }
            """.trimIndent()

        json
            .decodeFromString<CsrfJwksResponse>(jwksPayload)
            .keys
            .single()
            .kid shouldBe "key-1"
        json.decodeFromString<CsrfTokenResponse>(tokenPayload).token shouldBe "jwt-token"
        json
            .decodeFromString<SamlValidationError>(validationPayload)
            .validationErrors
            .single()
            .path shouldBe "x-original-host"
        json.decodeFromString<ForbiddenError>(forbiddenPayload).errorType shouldBe "NOT_ALLOWED"
    }
}
