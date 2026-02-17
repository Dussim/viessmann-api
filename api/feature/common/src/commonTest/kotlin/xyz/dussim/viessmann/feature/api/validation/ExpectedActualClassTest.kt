package xyz.dussim.viessmann.feature.api.validation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.StringValue

class ExpectedActualClassTest :
    FunSpec({
        context("ExpectedActualClass.of(expected, actual)") {
            test("creates instance with correct expected and actual classes") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, BOOLEAN_VALUE_CLASS_INDEX)
                eac.expectedClass shouldBe StringValue::class
                eac.actualClass shouldBe BooleanValue::class
            }

            test("handles same expected and actual") {
                val eac = ExpectedActualClass.of(DOUBLE_VALUE_CLASS_INDEX, DOUBLE_VALUE_CLASS_INDEX)
                eac.expectedClass shouldBe DoubleValue::class
                eac.actualClass shouldBe DoubleValue::class
            }

            test("handles feature types") {
                val eac = ExpectedActualClass.of(FEATURE_DEVICE_CLASS_INDEX, FEATURE_GATEWAY_CLASS_INDEX)
                eac.expectedClass shouldBe Feature.Device::class
                eac.actualClass shouldBe Feature.Gateway::class
            }

            test("handles command type") {
                val eac = ExpectedActualClass.of(COMMAND_CLASS_INDEX, STRING_VALUE_CLASS_INDEX)
                eac.expectedClass shouldBe Command::class
                eac.actualClass shouldBe StringValue::class
            }
        }

        context("ExpectedActualClass.of(expected) for missing components") {
            test("creates instance with Nothing as actual class") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX)
                eac.expectedClass shouldBe StringValue::class
                eac.actualClass shouldBe Nothing::class
            }

            test("works for all property value types") {
                ExpectedActualClass.of(BOOLEAN_VALUE_CLASS_INDEX).expectedClass shouldBe BooleanValue::class
                ExpectedActualClass.of(DOUBLE_VALUE_CLASS_INDEX).expectedClass shouldBe DoubleValue::class
            }
        }

        context("toString") {
            test("formats message for type mismatch") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, BOOLEAN_VALUE_CLASS_INDEX)
                val str = eac.toString()
                str shouldContain "expected"
                str shouldContain "actual"
            }

            test("formats message for missing component") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX)
                val str = eac.toString()
                str shouldContain "expected"
                str shouldContain "got nothing"
            }
        }

        context("validation") {
            test("throws for negative index") {
                shouldThrow<IllegalArgumentException> {
                    ExpectedActualClass(-1)
                }
            }

            test("throws for index exceeding maximum") {
                shouldThrow<IllegalArgumentException> {
                    ExpectedActualClass(40 * 40) // SIZE * SIZE
                }
            }

            test("accepts valid index at boundary") {
                // Should not throw - index 0 is valid
                ExpectedActualClass(0)
                // Index just below max should work
                ExpectedActualClass(40 * 40 - 1)
            }
        }

        context("all class indices") {
            test("STRING_VALUE_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX).expectedClass shouldBe StringValue::class
            }

            test("BOOLEAN_VALUE_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(BOOLEAN_VALUE_CLASS_INDEX).expectedClass shouldBe BooleanValue::class
            }

            test("DOUBLE_VALUE_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(DOUBLE_VALUE_CLASS_INDEX).expectedClass shouldBe DoubleValue::class
            }

            test("FEATURE_DEVICE_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(FEATURE_DEVICE_CLASS_INDEX).expectedClass shouldBe Feature.Device::class
            }

            test("FEATURE_GATEWAY_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(FEATURE_GATEWAY_CLASS_INDEX).expectedClass shouldBe Feature.Gateway::class
            }

            test("FEATURE_GEOFENCING_CLASS_INDEX resolves correctly") {
                ExpectedActualClass.of(FEATURE_GEOFENCING_CLASS_INDEX).expectedClass shouldBe Feature.Geofencing::class
            }
        }
    })
