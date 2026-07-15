package xyz.dussim.buildlogic.internal

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.media.Schema

val OpenApiSchemaResolverTest by testSuite {
        test("resolves composed object properties") {
            val schema =
                Schema<Any>().apply {
                    allOf =
                        listOf(
                            Schema<Any>().apply {
                                type = "object"
                                properties =
                                    mapOf(
                                        "mode" to
                                            Schema<Any>().apply {
                                                type = "string"
                                            },
                                    )
                            },
                            Schema<Any>().apply {
                                properties =
                                    mapOf(
                                        "enabled" to
                                            Schema<Any>().apply {
                                                type = "boolean"
                                            },
                                    )
                            },
                        )
                }

            val resolved = OpenApiSchemaResolver.resolve(schema)

            resolved.type shouldBe "object"
            resolved.properties?.get("mode")?.type shouldBe "string"
            resolved.properties?.get("enabled")?.type shouldBe "boolean"
        }

        test("merges duplicate property schemas") {
            val schema =
                Schema<Any>().apply {
                    allOf =
                        listOf(
                            Schema<Any>().apply {
                                properties =
                                    mapOf(
                                        "value" to
                                            Schema<Any>().apply {
                                                type = "string"
                                                enum = mutableListOf("on")
                                            },
                                    )
                            },
                            Schema<Any>().apply {
                                properties =
                                    mapOf(
                                        "value" to
                                            Schema<Any>().apply {
                                                nullable = true
                                                enum = mutableListOf("off")
                                            },
                                    )
                            },
                        )
                }

            val value = OpenApiSchemaResolver.resolve(schema).properties?.get("value")

            value?.type shouldBe "string"
            value?.nullable shouldBe true
            value?.enum shouldBe mutableListOf("on", "off")
        }

        test("documents current required field intersection behavior") {
            val schema =
                Schema<Any>().apply {
                    allOf =
                        listOf(
                            Schema<Any>().apply {
                                required = listOf("left", "shared")
                            },
                            Schema<Any>().apply {
                                required = listOf("right", "shared")
                            },
                        )
                }

            OpenApiSchemaResolver.resolve(schema).required shouldBe listOf("shared")
        }

        test("resolves array items and additional property schemas") {
            val schema =
                Schema<Any>().apply {
                    properties =
                        mapOf(
                            "values" to
                                Schema<Any>().apply {
                                    type = "array"
                                    items =
                                        Schema<Any>().apply {
                                            oneOf =
                                                listOf(
                                                    Schema<Any>().apply {
                                                        type = "string"
                                                    },
                                                    Schema<Any>().apply {
                                                        enum = mutableListOf("auto")
                                                    },
                                                )
                                        }
                                },
                            "attributes" to
                                Schema<Any>().apply {
                                    additionalProperties =
                                        Schema<Any>().apply {
                                            type = "number"
                                        }
                                },
                        )
                }

            val resolved = OpenApiSchemaResolver.resolve(schema)

            val values =
                resolved.properties
                    ?.get("values")
                    ?.items
            val attributes =
                resolved.properties
                    ?.get("attributes")
                    ?.additionalProperties as Schema<*>

            values?.type shouldBe "string"
            values?.enum shouldBe mutableListOf("auto")
            attributes.type shouldBe "number"
        }
}
