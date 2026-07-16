package xyz.dussim.viessmann.api.errors

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

private val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

val ErrorModelsTest by testSuite {
    test("decodes validation error payload") {
        val payload =
            """
            {
              "viErrorId": "req-d00b7d06c1be47549f5f455a832b519e",
              "statusCode": 422,
              "errorType": "VALIDATION_ERROR",
              "message": "Invalid data provided. Refer to validationErrors for more details",
              "extendedPayload": { "debug": "value" },
              "validationErrors": [
                {
                  "message": "\"days\" must be a number",
                  "path": ["days"],
                  "type": "number.base",
                  "context": {
                    "key": "days",
                    "label": "days",
                    "value": "abc"
                  }
                }
              ]
            }
            """.trimIndent()

        val decoded = json.decodeFromString<ViessmannApiError>(payload)

        decoded.errorType shouldBe ErrorType.ValidationError
        decoded.validationErrors
            ?.single()
            ?.context
            ?.get("key")
            ?.jsonPrimitive
            ?.content shouldBe "days"
    }

    test("round-trips unknown error types") {
        val encoded = json.encodeToString<ErrorType>(ErrorType.Unknown("SOMETHING_NEW"))

        encoded shouldBe "\"SOMETHING_NEW\""
        json.decodeFromString<ErrorType>(encoded) shouldBe ErrorType.Unknown("SOMETHING_NEW")
    }
}
