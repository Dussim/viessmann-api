package xyz.dussim.viessmann.feature.api.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ValidationResultTest :
    FunSpec({
        context("Valid()") {
            test("isInvalid returns false") {
                Valid<String>().isInvalid shouldBe false
            }

            test("size returns 0") {
                Valid<String>().size shouldBe 0
            }

            test("forEach does nothing") {
                val collected = mutableListOf<String>()
                Valid<String>().forEach { collected.add(it) }
                collected.shouldBeEmpty()
            }

            test("map returns empty list") {
                Valid<String>().map { it.uppercase() }.shouldBeEmpty()
            }

            test("asIterable returns empty iterable") {
                Valid<String>().asIterable().toList().shouldBeEmpty()
            }
        }

        context("Invalid with single value") {
            test("isInvalid returns true") {
                ValidationResult.Invalid("error").isInvalid shouldBe true
            }

            test("size returns 1") {
                ValidationResult.Invalid("error").size shouldBe 1
            }

            test("forEach iterates over single value") {
                val collected = mutableListOf<String>()
                ValidationResult.Invalid("error").forEach { collected.add(it) }
                collected shouldContainExactly listOf("error")
            }

            test("map transforms single value") {
                ValidationResult.Invalid("error").map { it.uppercase() } shouldContainExactly listOf("ERROR")
            }

            test("asIterable returns single element iterable") {
                ValidationResult.Invalid("error").asIterable().toList() shouldContainExactly listOf("error")
            }
        }

        context("Invalid with array of values") {
            test("isInvalid returns true") {
                ValidationResult.Invalid(arrayOf("a", "b", "c")).isInvalid shouldBe true
            }

            test("size returns array size") {
                ValidationResult.Invalid(arrayOf("a", "b", "c")).size shouldBe 3
            }

            test("forEach iterates over all values") {
                val collected = mutableListOf<String>()
                ValidationResult.Invalid(arrayOf("a", "b", "c")).forEach { collected.add(it) }
                collected shouldContainExactly listOf("a", "b", "c")
            }

            test("map transforms all values") {
                ValidationResult.Invalid(arrayOf("a", "b", "c")).map { it.uppercase() } shouldContainExactly listOf("A", "B", "C")
            }

            test("asIterable returns all elements") {
                ValidationResult.Invalid(arrayOf("a", "b", "c")).asIterable().toList() shouldContainExactly listOf("a", "b", "c")
            }
        }

        context("ValidationResult.of() with no arguments") {
            test("returns Valid") {
                ValidationResult.of<String>().isInvalid shouldBe false
            }
        }

        context("ValidationResult.of() with single error") {
            test("returns Invalid with that error") {
                val result = ValidationResult.of("error")
                result.isInvalid shouldBe true
                result.size shouldBe 1
            }
        }

        context("ValidationResult.of() with single result") {
            test("returns same result when Valid") {
                val result = ValidationResult.of(Valid<String>())
                result.isInvalid shouldBe false
            }

            test("returns same result when Invalid") {
                val result = ValidationResult.of(ValidationResult.Invalid("error"))
                result.isInvalid shouldBe true
                result.size shouldBe 1
            }
        }

        context("ValidationResult.of() combining 2 results") {
            test("returns Valid when both are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid())
                result.isInvalid shouldBe false
            }

            test("returns first Invalid when only first is Invalid") {
                val result = ValidationResult.of(ValidationResult.Invalid("a"), Valid())
                result.isInvalid shouldBe true
                result.size shouldBe 1
                result.map { it } shouldContainExactly listOf("a")
            }

            test("returns second Invalid when only second is Invalid") {
                val result = ValidationResult.of(Valid(), ValidationResult.Invalid("b"))
                result.isInvalid shouldBe true
                result.size shouldBe 1
                result.map { it } shouldContainExactly listOf("b")
            }

            test("combines errors when both are Invalid") {
                val result = ValidationResult.of(ValidationResult.Invalid("a"), ValidationResult.Invalid("b"))
                result.isInvalid shouldBe true
                result.size shouldBe 2
                result.map { it } shouldContainExactly listOf("a", "b")
            }
        }

        context("ValidationResult.of() combining 3 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("returns single error when only one is Invalid") {
                val result = ValidationResult.of(Valid(), ValidationResult.Invalid("b"), Valid())
                result.isInvalid shouldBe true
                result.size shouldBe 1
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("a"),
                        ValidationResult.Invalid("b"),
                        ValidationResult.Invalid("c"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 3
                result.map { it } shouldContainExactly listOf("a", "b", "c")
            }
        }

        context("ValidationResult.of() combining 4 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("a"),
                        ValidationResult.Invalid("b"),
                        ValidationResult.Invalid("c"),
                        ValidationResult.Invalid("d"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 4
            }
        }

        context("ValidationResult.of() combining 5 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 5
            }
        }

        context("ValidationResult.of() combining 6 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), Valid(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 6
            }
        }

        context("ValidationResult.of() combining 7 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), Valid(), Valid(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 7
            }
        }

        context("ValidationResult.of() combining 8 results") {
            test("returns Valid when all are Valid") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), Valid(), Valid(), Valid(), Valid(), Valid())
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 8
            }
        }

        context("ValidationResult.of() combining 9 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 9
            }
        }

        context("ValidationResult.of() combining 10 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 10
            }
        }

        context("ValidationResult.of() combining 11 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 11
            }
        }

        context("ValidationResult.of() combining 12 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                        ValidationResult.Invalid("12"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 12
            }
        }

        context("ValidationResult.of() combining 13 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                        ValidationResult.Invalid("12"),
                        ValidationResult.Invalid("13"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 13
            }
        }

        context("ValidationResult.of() combining 14 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                        ValidationResult.Invalid("12"),
                        ValidationResult.Invalid("13"),
                        ValidationResult.Invalid("14"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 14
            }
        }

        context("ValidationResult.of() combining 15 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                        ValidationResult.Invalid("12"),
                        ValidationResult.Invalid("13"),
                        ValidationResult.Invalid("14"),
                        ValidationResult.Invalid("15"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 15
            }
        }

        context("ValidationResult.of() combining 16 results") {
            test("returns Valid when all are Valid") {
                val result =
                    ValidationResult.of(
                        Valid<String>(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                        Valid(),
                    )
                result.isInvalid shouldBe false
            }

            test("combines all errors when all are Invalid") {
                val result =
                    ValidationResult.of(
                        ValidationResult.Invalid("1"),
                        ValidationResult.Invalid("2"),
                        ValidationResult.Invalid("3"),
                        ValidationResult.Invalid("4"),
                        ValidationResult.Invalid("5"),
                        ValidationResult.Invalid("6"),
                        ValidationResult.Invalid("7"),
                        ValidationResult.Invalid("8"),
                        ValidationResult.Invalid("9"),
                        ValidationResult.Invalid("10"),
                        ValidationResult.Invalid("11"),
                        ValidationResult.Invalid("12"),
                        ValidationResult.Invalid("13"),
                        ValidationResult.Invalid("14"),
                        ValidationResult.Invalid("15"),
                        ValidationResult.Invalid("16"),
                    )
                result.isInvalid shouldBe true
                result.size shouldBe 16
            }
        }

        context("edge cases for of() with mixed valid/invalid") {
            test("of(2) returns first invalid when capacity is 1") {
                val result = ValidationResult.of(ValidationResult.Invalid("a"), Valid())
                result.map { it } shouldContainExactly listOf("a")
            }

            test("of(3) returns first invalid when capacity is 1") {
                val result = ValidationResult.of(Valid<String>(), ValidationResult.Invalid("b"), Valid())
                result.map { it } shouldContainExactly listOf("b")
            }

            test("of(4) returns first invalid when capacity is 1") {
                val result = ValidationResult.of(Valid<String>(), Valid(), Valid(), ValidationResult.Invalid("d"))
                result.map { it } shouldContainExactly listOf("d")
            }

            test("combines errors from array results") {
                val arrayResult = ValidationResult.Invalid(arrayOf("a", "b"))
                val singleResult = ValidationResult.Invalid("c")
                val result = ValidationResult.of(arrayResult, singleResult)
                result.size shouldBe 3
                result.map { it } shouldContainExactly listOf("a", "b", "c")
            }
        }
    })
