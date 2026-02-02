package xyz.dussim.viessmann.feature.api.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ValidationRuleTest :
    FunSpec({
        context("ValidationRule.validate") {
            test("returns Valid for passing validation") {
                val rule = ValidationRule<Int, String> { Valid() }
                rule.validate(42).isInvalid shouldBe false
            }

            test("returns Invalid for failing validation") {
                val rule = ValidationRule<Int, String> { ValidationResult.Invalid("error") }
                rule.validate(42).isInvalid shouldBe true
            }
        }

        context("ValidationRule invoke operator") {
            test("delegates to validate") {
                val rule =
                    ValidationRule<Int, String> { value ->
                        if (value > 0) Valid() else ValidationResult.Invalid("must be positive")
                    }
                rule(5).isInvalid shouldBe false
                rule(-1).isInvalid shouldBe true
            }
        }

        context("ValidationRule.and") {
            val isPositive =
                ValidationRule<Int, String> { value ->
                    if (value > 0) Valid() else ValidationResult.Invalid("must be positive")
                }
            val isEven =
                ValidationRule<Int, String> { value ->
                    if (value % 2 == 0) Valid() else ValidationResult.Invalid("must be even")
                }
            val isLessThan100 =
                ValidationRule<Int, String> { value ->
                    if (value < 100) Valid() else ValidationResult.Invalid("must be less than 100")
                }

            test("returns Valid when all rules pass") {
                val combined = ValidationRule.and(isPositive, isEven)
                combined(4).isInvalid shouldBe false
            }

            test("returns Invalid when first rule fails") {
                val combined = ValidationRule.and(isPositive, isEven)
                val result = combined(-2)
                result.isInvalid shouldBe true
                result.map { it } shouldContainExactly listOf("must be positive")
            }

            test("returns Invalid when second rule fails") {
                val combined = ValidationRule.and(isPositive, isEven)
                val result = combined(3)
                result.isInvalid shouldBe true
                result.map { it } shouldContainExactly listOf("must be even")
            }

            test("accumulates all errors when multiple rules fail") {
                val combined = ValidationRule.and(isPositive, isEven)
                val result = combined(-3)
                result.isInvalid shouldBe true
                result.size shouldBe 2
                result.map { it } shouldContainExactly listOf("must be positive", "must be even")
            }

            test("combines multiple rules with vararg") {
                val combined = ValidationRule.and(isPositive, isEven, isLessThan100)
                combined(4).isInvalid shouldBe false
                combined(201).size shouldBe 2 // not even (201 % 2 = 1), not less than 100
            }
        }

        context("ValidationRule.or") {
            val isPositive =
                ValidationRule<Int, String> { value ->
                    if (value > 0) Valid() else ValidationResult.Invalid("must be positive")
                }
            val isEven =
                ValidationRule<Int, String> { value ->
                    if (value % 2 == 0) Valid() else ValidationResult.Invalid("must be even")
                }
            val isZero =
                ValidationRule<Int, String> { value ->
                    if (value == 0) Valid() else ValidationResult.Invalid("must be zero")
                }

            test("returns Valid when first rule passes") {
                val combined = ValidationRule.or(isPositive, isEven)
                combined(5).isInvalid shouldBe false
            }

            test("returns Valid when second rule passes") {
                val combined = ValidationRule.or(isPositive, isEven)
                combined(-2).isInvalid shouldBe false
            }

            test("returns Valid when both rules pass") {
                val combined = ValidationRule.or(isPositive, isEven)
                combined(4).isInvalid shouldBe false
            }

            test("returns Invalid with all errors when all rules fail") {
                val combined = ValidationRule.or(isPositive, isEven)
                val result = combined(-3)
                result.isInvalid shouldBe true
                result.size shouldBe 2
            }

            test("short-circuits on first Valid result") {
                var secondRuleCalled = false
                val firstRule = ValidationRule<Int, String> { Valid() }
                val secondRule =
                    ValidationRule<Int, String> {
                        secondRuleCalled = true
                        Valid()
                    }
                val combined = ValidationRule.or(firstRule, secondRule)
                combined(1)
                secondRuleCalled shouldBe false
            }

            test("combines multiple rules with vararg") {
                val combined = ValidationRule.or(isPositive, isEven, isZero)
                combined(0).isInvalid shouldBe false
                combined(-3).isInvalid shouldBe true
            }
        }

        context("transform extension") {
            val isNotEmpty =
                ValidationRule<String, String> { value ->
                    if (value.isNotEmpty()) Valid() else ValidationResult.Invalid("must not be empty")
                }

            test("applies transformation before validation") {
                val rule = isNotEmpty.transform<Int, String, String> { it.toString() }
                rule(42).isInvalid shouldBe false
            }

            test("validation fails after transformation") {
                val isLongEnough =
                    ValidationRule<String, String> { value ->
                        if (value.length >= 3) Valid() else ValidationResult.Invalid("too short")
                    }
                val rule = isLongEnough.transform<Int, String, String> { it.toString() }
                rule(5).isInvalid shouldBe true // "5" has length 1
                rule(123).isInvalid shouldBe false // "123" has length 3
            }
        }

        context("validateAll extension for Iterable") {
            val isPositive =
                ValidationRule<Int, String> { value ->
                    if (value > 0) Valid() else ValidationResult.Invalid("$value must be positive")
                }

            test("returns Valid when all items pass") {
                val result = isPositive.validateAll(listOf(1, 2, 3))
                result.isInvalid shouldBe false
            }

            test("returns Invalid when one item fails") {
                val result = isPositive.validateAll(listOf(1, -2, 3))
                result.isInvalid shouldBe true
                result.size shouldBe 1
            }

            test("accumulates errors from all failing items") {
                val result = isPositive.validateAll(listOf(-1, -2, -3))
                result.isInvalid shouldBe true
                result.size shouldBe 3
            }

            test("returns Valid for empty iterable") {
                val result = isPositive.validateAll(emptyList())
                result.isInvalid shouldBe false
            }
        }

        context("validateAll extension for Sequence") {
            val isPositive =
                ValidationRule<Int, String> { value ->
                    if (value > 0) Valid() else ValidationResult.Invalid("$value must be positive")
                }

            test("returns Valid when all items pass") {
                val result = isPositive.validateAll(sequenceOf(1, 2, 3))
                result.isInvalid shouldBe false
            }

            test("accumulates errors from all failing items") {
                val result = isPositive.validateAll(sequenceOf(-1, -2, -3))
                result.isInvalid shouldBe true
                result.size shouldBe 3
            }

            test("returns Valid for empty sequence") {
                val result = isPositive.validateAll(emptySequence())
                result.isInvalid shouldBe false
            }
        }

        context("onError extension") {
            test("executes effect for each error") {
                val collected = mutableListOf<String>()
                val result = ValidationResult.Invalid(arrayOf("a", "b", "c"))
                result.onError { collected.add(it) }
                collected shouldContainExactly listOf("a", "b", "c")
            }

            test("does not execute effect for Valid") {
                val collected = mutableListOf<String>()
                Valid<String>().onError { collected.add(it) }
                collected.shouldBeEmpty()
            }

            test("returns the original result") {
                val original = ValidationResult.Invalid("error")
                val returned = original.onError { }
                returned shouldBe original
            }

            test("executes effect for single error") {
                val collected = mutableListOf<String>()
                ValidationResult.Invalid("single").onError { collected.add(it) }
                collected shouldContainExactly listOf("single")
            }
        }
    })
