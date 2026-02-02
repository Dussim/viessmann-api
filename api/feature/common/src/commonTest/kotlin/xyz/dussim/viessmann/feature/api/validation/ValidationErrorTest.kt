package xyz.dussim.viessmann.feature.api.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ValidationErrorTest :
    FunSpec({
        context("MissingComponent") {
            test("stores name and expectedActualClass") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX)
                val error = ValidationError.MissingComponent("testProperty", eac)
                error.name shouldBe "testProperty"
                error.expectedActualClass shouldBe eac
            }

            test("toString includes component name") {
                val error =
                    ValidationError.MissingComponent(
                        "myProperty",
                        ExpectedActualClass.of(BOOLEAN_VALUE_CLASS_INDEX),
                    )
                error.toString() shouldContain "myProperty"
                error.toString() shouldContain "Missing component"
            }

            test("equals and hashCode work correctly") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX)
                val error1 = ValidationError.MissingComponent("prop", eac)
                val error2 = ValidationError.MissingComponent("prop", eac)
                val error3 = ValidationError.MissingComponent("other", eac)

                error1 shouldBe error2
                error1.hashCode() shouldBe error2.hashCode()
                (error1 == error3) shouldBe false
            }
        }

        context("ComponentTypeMismatch") {
            test("stores name and expectedActualClass") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, BOOLEAN_VALUE_CLASS_INDEX)
                val error = ValidationError.ComponentTypeMismatch("testProperty", eac)
                error.name shouldBe "testProperty"
                error.expectedActualClass shouldBe eac
            }

            test("toString includes component name and mismatch info") {
                val error =
                    ValidationError.ComponentTypeMismatch(
                        "myProperty",
                        ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, BOOLEAN_VALUE_CLASS_INDEX),
                    )
                error.toString() shouldContain "myProperty"
                error.toString() shouldContain "type mismatch"
            }

            test("equals and hashCode work correctly") {
                val eac = ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, DOUBLE_VALUE_CLASS_INDEX)
                val error1 = ValidationError.ComponentTypeMismatch("prop", eac)
                val error2 = ValidationError.ComponentTypeMismatch("prop", eac)
                val error3 = ValidationError.ComponentTypeMismatch("other", eac)

                error1 shouldBe error2
                error1.hashCode() shouldBe error2.hashCode()
                (error1 == error3) shouldBe false
            }
        }

        context("NumberOfParametersMismatch") {
            test("stores expected and actual values") {
                val expected = ValidationError.NumberOfParametersMismatch.Expected("setTemperature", 2)
                val error = ValidationError.NumberOfParametersMismatch(expected, 3)
                error.expected shouldBe expected
                error.actual shouldBe 3
            }

            test("Expected stores name and count") {
                val expected = ValidationError.NumberOfParametersMismatch.Expected("setMode", 1)
                expected.name shouldBe "setMode"
                expected.expected shouldBe 1
            }

            test("toString includes command name and counts") {
                val expected = ValidationError.NumberOfParametersMismatch.Expected("setTemperature", 2)
                val error = ValidationError.NumberOfParametersMismatch(expected, 5)
                error.toString() shouldContain "setTemperature"
                error.toString() shouldContain "parameters"
            }

            test("equals and hashCode work correctly for Expected") {
                val expected1 = ValidationError.NumberOfParametersMismatch.Expected("cmd", 2)
                val expected2 = ValidationError.NumberOfParametersMismatch.Expected("cmd", 2)
                val expected3 = ValidationError.NumberOfParametersMismatch.Expected("cmd", 3)

                expected1 shouldBe expected2
                expected1.hashCode() shouldBe expected2.hashCode()
                (expected1 == expected3) shouldBe false
            }

            test("equals and hashCode work correctly for NumberOfParametersMismatch") {
                val expected = ValidationError.NumberOfParametersMismatch.Expected("cmd", 2)
                val error1 = ValidationError.NumberOfParametersMismatch(expected, 5)
                val error2 = ValidationError.NumberOfParametersMismatch(expected, 5)
                val error3 = ValidationError.NumberOfParametersMismatch(expected, 6)

                error1 shouldBe error2
                error1.hashCode() shouldBe error2.hashCode()
                (error1 == error3) shouldBe false
            }
        }

        context("ValidationError as sealed interface") {
            test("all error types can be used polymorphically") {
                val errors: List<ValidationError> =
                    listOf(
                        ValidationError.MissingComponent("a", ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX)),
                        ValidationError.ComponentTypeMismatch("b", ExpectedActualClass.of(STRING_VALUE_CLASS_INDEX, DOUBLE_VALUE_CLASS_INDEX)),
                        ValidationError.NumberOfParametersMismatch(
                            ValidationError.NumberOfParametersMismatch.Expected("c", 1),
                            2,
                        ),
                    )
                errors.size shouldBe 3
            }
        }
    })
