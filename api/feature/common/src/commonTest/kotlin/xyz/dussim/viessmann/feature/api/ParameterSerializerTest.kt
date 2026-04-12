package xyz.dussim.viessmann.feature.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class ParameterSerializerTest :
    FunSpec({
        val json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }

        test("deserializes array parameter constraints without enum as ArrayEmptyConstraints") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "array",
                      "required": true,
                      "constraints": {
                        "minLength": 0,
                        "maxLength": 576
                      }
                    }
                    """.trimIndent(),
                )

            val constraints = parameter.constraints.shouldBeInstanceOf<ArrayEmptyConstraints>()
            constraints.minLength shouldBe 0
            constraints.maxLength shouldBe 576
        }

        test("deserializes array constraints with empty enum as ArrayUnknownConstraints") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "array",
                      "required": true,
                      "constraints": {
                        "minLength": 0,
                        "maxLength": 10,
                        "enum": []
                      }
                    }
                    """.trimIndent(),
                )

            val constraints = parameter.constraints.shouldBeInstanceOf<ArrayUnknownConstraints>()
            constraints.minLength shouldBe 0
            constraints.maxLength shouldBe 10
            constraints.enum shouldBe emptyList()
        }

        test("deserializes array constraints enum of numbers as ArrayNumberConstraints") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "array",
                      "required": true,
                      "constraints": {
                        "minLength": 1,
                        "maxLength": 3,
                        "enum": [1.0, 2.5, 3.0]
                      }
                    }
                    """.trimIndent(),
                )

            val constraints = parameter.constraints.shouldBeInstanceOf<ArrayNumberConstraints>()
            constraints.enum shouldBe listOf(1.0, 2.5, 3.0)
        }

        test("deserializes object parameter constraints as ObjectConstraints") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "object",
                      "required": true,
                      "constraints": {
                        "minProperties": 0,
                        "maxProperties": 2,
                        "required": ["logs"]
                      }
                    }
                    """.trimIndent(),
                )

            val constraints = parameter.constraints.shouldBeInstanceOf<ObjectConstraints>()
            constraints.minProperties shouldBe 0
            constraints.maxProperties shouldBe 2
            constraints.required shouldBe listOf("logs")
        }

        test("deserializes energy matrix parameter constraints as EnergyMatrixConstraints") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "EnergyMatrix",
                      "required": true,
                      "constraints": {}
                    }
                    """.trimIndent(),
                )

            parameter.constraints.shouldBeInstanceOf<EnergyMatrixConstraints>()
        }

        test("falls back to UnknownConstraints for unsupported parameter type") {
            val parameter =
                json.decodeFromString(
                    Parameter.serializer(),
                    """
                    {
                      "type": "customType",
                      "required": true,
                      "constraints": {
                        "someField": "someValue"
                      }
                    }
                    """.trimIndent(),
                )

            parameter.constraints.shouldBeInstanceOf<UnknownConstraints>()
        }
    })
