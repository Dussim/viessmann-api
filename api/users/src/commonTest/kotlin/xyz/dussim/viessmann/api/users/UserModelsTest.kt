package xyz.dussim.viessmann.api.users

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import xyz.dussim.viessmann.api.enums.Gender

private val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

val UserModelsTest by testSuite {
    test("encodes password check request and decodes response") {
        val request =
            CheckPasswordRequest(
                password = "Test1234!",
                type = UserType.Customer,
            )

        json.encodeToString(request) shouldBe """{"password":"Test1234!","type":"customer"}"""

        val response = """{"valid":true,"errors":[{"code":"OK"}]}"""
        json.decodeFromString<CheckPasswordResponse>(response).valid shouldBe true
    }

    test("round-trips create consumer request and decodes response") {
        val request =
            CreateConsumerRequest(
                loginId = "max@mustermann.com",
                documents = listOf(DocumentAcceptance("123e4567-e89b-12d3-a456-426614174000", true)),
                optIn = true,
                appId = "429872e8-9933-469f-b372-7f2df436076d",
                languageCode = "de",
                gender = Gender.Male,
                birthDate = "1999-01-01+0100",
                name = ConsumerName("Dr", "Max", "Mustermann"),
                address =
                    ConsumerAddress(
                        countryCode = "de",
                        city = "Frankfurt",
                        street = "Teststrasse",
                        postalCode = "12345",
                        houseNumber = "5a",
                        region = "123",
                    ),
                contacts = ConsumerContacts(telephone = "+49 0611 1234567"),
                password = "!Pommes123!",
            )

        json.decodeFromString<CreateConsumerRequest>(json.encodeToString(request)) shouldBe request

        val response =
            """
            {
              "data": {
                "id": "9039e4d7-cf60-4831-b220-adc26ae9a011",
                "type": "users",
                "attributes": {
                  "loginId": "max@mustermann.com",
                  "languageCode": "de",
                  "userState": "ACTIVE",
                  "name": {
                    "title": "Dr",
                    "firstName": "Max",
                    "familyName": "Mustermann"
                  },
                  "_id": "internal-id",
                  "roles": ["consumer"]
                }
              }
            }
            """.trimIndent()

        json
            .decodeFromString<CreateConsumerResponse>(response)
            .data.attributes.internalId shouldBe "internal-id"
    }

    test("decodes address validation response") {
        val payload =
            """
            {
              "isValid": true,
              "alternatives": [
                {
                  "street": "Main Street",
                  "houseNumber": "123",
                  "postalCode": "12345",
                  "city": "Berlin",
                  "countryCode": "DE"
                }
              ]
            }
            """.trimIndent()

        json
            .decodeFromString<ValidateAddressResponse>(payload)
            .alternatives
            ?.single()
            ?.city shouldBe "Berlin"
    }
}
