package xyz.dussim.viessmann.feature.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.feature.api.validation.propertyHash

class GeneratedAccessorsTest :
    FunSpec({
        fun hash(name: String): Long = propertyHash(name.hashCode(), name.length)

        test("requireProperty throws GeneratedAccessException for missing property") {
            val properties = EfficientStringKeyMap<Property>(emptyMap())
            val exception =
                shouldThrow<GeneratedAccessException> {
                    properties.requireProperty("temperature", hash("temperature"))
                }

            exception shouldBe GeneratedAccessException
        }

        test("requireCommand throws GeneratedAccessException for missing command") {
            val commands = EfficientStringKeyMap<Command>(emptyMap())
            val exception =
                shouldThrow<GeneratedAccessException> {
                    commands.requireCommand("setValue", hash("setValue"))
                }

            exception shouldBe GeneratedAccessException
        }

        test("requireParam throws GeneratedAccessException for missing parameter") {
            val params = EfficientStringKeyMap<Parameter>(emptyMap())
            val exception =
                shouldThrow<GeneratedAccessException> {
                    params.requireParam("value", hash("value"))
                }

            exception shouldBe GeneratedAccessException
        }

        test("requirePropertyValue throws GeneratedAccessException on type mismatch") {
            val properties = EfficientStringKeyMap(mapOf("temperature" to Property.of("high")))
            val exception =
                shouldThrow<GeneratedAccessException> {
                    properties.requirePropertyValue<DoubleValue>("temperature", hash("temperature"))
                }

            exception shouldBe GeneratedAccessException
        }

        test("findPropertyValueOrNull returns null for missing and mismatch") {
            val missing = EfficientStringKeyMap<Property>(emptyMap())
            missing.findPropertyValueOrNull<DoubleValue>("temperature", hash("temperature")).shouldBeNull()

            val presentWrongType = EfficientStringKeyMap(mapOf("temperature" to Property.of("high")))
            presentWrongType.findPropertyValueOrNull<DoubleValue>("temperature", hash("temperature")).shouldBeNull()
        }

        test("requirePropertyValueOrPromoteEmpty promotes ListEmptyValue and throws on mismatch or missing") {
            val default = ListStringValue.EMPTY

            val listEmptyValue = EfficientStringKeyMap(mapOf("values" to Property(ARRAY, ListEmptyValue)))
            listEmptyValue.requirePropertyValueOrPromoteEmpty("values", hash("values"), default) shouldBe default

            val wrongType = EfficientStringKeyMap(mapOf("values" to Property.of("wrong")))
            shouldThrow<GeneratedAccessException> {
                wrongType.requirePropertyValueOrPromoteEmpty("values", hash("values"), default)
            } shouldBe GeneratedAccessException

            val missing = EfficientStringKeyMap<Property>(emptyMap())
            shouldThrow<GeneratedAccessException> {
                missing.requirePropertyValueOrPromoteEmpty("values", hash("values"), default)
            } shouldBe GeneratedAccessException
        }

        test("findPropertyValueOrPromoteEmpty returns defaultValue on missing, mismatch, or ListEmptyValue") {
            val default = ListStringValue.EMPTY

            val missing = EfficientStringKeyMap<Property>(emptyMap())
            missing.findPropertyValueOrPromoteEmpty("values", hash("values"), default) shouldBe default

            val wrongType = EfficientStringKeyMap(mapOf("values" to Property.of("wrong")))
            wrongType.findPropertyValueOrPromoteEmpty("values", hash("values"), default) shouldBe default

            val listEmptyValue = EfficientStringKeyMap(mapOf("values" to Property(ARRAY, ListEmptyValue)))
            listEmptyValue.findPropertyValueOrPromoteEmpty("values", hash("values"), default) shouldBe default
        }

        test("requireConstraints throws GeneratedAccessException on mismatch") {
            val exception =
                shouldThrow<GeneratedAccessException> {
                    BooleanConstraints.requireConstraints<NumberConstraints>()
                }

            exception shouldBe GeneratedAccessException
        }

        test("requireArrayConstraintsOrPromoteEmpty promotes ArrayEmptyConstraints") {
            val promoted =
                ArrayEmptyConstraints(minLength = 1, maxLength = 3).requireArrayConstraintsOrPromoteEmpty(
                    fromEmpty = ::ArrayStringConstraints,
                )

            promoted shouldBe ArrayStringConstraints(minLength = 1, maxLength = 3, enum = null)
        }

        test("toArrayStringConstraintsOrThrow throws GeneratedAccessException on type mismatch") {
            val exception =
                shouldThrow<GeneratedAccessException> {
                    BooleanConstraints.toArrayStringConstraintsOrThrow()
                }

            exception shouldBe GeneratedAccessException
        }
    })
