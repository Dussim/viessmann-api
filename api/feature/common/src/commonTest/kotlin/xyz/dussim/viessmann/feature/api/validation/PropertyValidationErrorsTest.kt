package xyz.dussim.viessmann.feature.api.validation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PropertyValidationErrorsTest :
    FunSpec({
        context("getMismatchProperties") {
            test("returns Invalid result with ComponentTypeMismatch error") {
                val getter = PropertyValidationErrors.getMismatchProperties("testPropUnique1", STRING_VALUE_CLASS_INDEX)
                val result = getter(BOOLEAN_VALUE_CLASS_INDEX)

                result.isInvalid shouldBe true
                val error = result.asIterable().first()
                error.shouldBeInstanceOf<ValidationError.ComponentTypeMismatch>()
                error.name shouldBe "testPropUnique1"
            }

            test("caches results - second call returns same result for same indices") {
                // Use a unique expected index that won't be used elsewhere
                val getter = PropertyValidationErrors.getMismatchProperties("cachedPropUnique", LIST_ROOM_ACTOR_VALUE_CLASS_INDEX)

                // First call creates the cached entry
                val result1 = getter(OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX)
                // Second call should return cached entry
                val result2 = getter(OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX)

                // Both should be invalid with same content
                result1.isInvalid shouldBe true
                result2.isInvalid shouldBe true
                result1.asIterable().first().name shouldBe "cachedPropUnique"
                result2.asIterable().first().name shouldBe "cachedPropUnique"
            }

            test("returns different results for different actual indices") {
                val getter = PropertyValidationErrors.getMismatchProperties("propUnique2", STRING_VALUE_CLASS_INDEX)

                val result1 = getter(BOOLEAN_VALUE_CLASS_INDEX)
                val result2 = getter(DOUBLE_VALUE_CLASS_INDEX)

                result1.asIterable().first().shouldBeInstanceOf<ValidationError.ComponentTypeMismatch>()
                result2.asIterable().first().shouldBeInstanceOf<ValidationError.ComponentTypeMismatch>()
                (result1 == result2) shouldBe false
            }

            test("getter returns error with correct property name") {
                val getter1 = PropertyValidationErrors.getMismatchProperties("uniquePropA", LIST_DEVICE_VALUE_CLASS_INDEX)
                val getter2 = PropertyValidationErrors.getMismatchProperties("uniquePropB", LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX)

                val result1 = getter1(SCHEDULE_VALUE_CLASS_INDEX)
                val result2 = getter2(SCHEDULE_VALUE_CLASS_INDEX)

                result1.asIterable().first().name shouldBe "uniquePropA"
                result2.asIterable().first().name shouldBe "uniquePropB"
            }

            test("works for all property value type indices") {
                val indices =
                    listOf(
                        STRING_VALUE_CLASS_INDEX,
                        BOOLEAN_VALUE_CLASS_INDEX,
                        DOUBLE_VALUE_CLASS_INDEX,
                        LIST_DOUBLE_VALUE_CLASS_INDEX,
                        LIST_STRING_VALUE_CLASS_INDEX,
                        LIST_DEVICE_ERROR_VALUE_CLASS_INDEX,
                        LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX,
                        LIST_ROOM_ACTOR_VALUE_CLASS_INDEX,
                        LIST_DEVICE_VALUE_CLASS_INDEX,
                        OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX,
                        SCHEDULE_VALUE_CLASS_INDEX,
                        LIST_EMPTY_VALUE_CLASS_INDEX,
                        UNKNOWN_VALUE_CLASS_INDEX,
                    )

                for (expectedIndex in indices) {
                    val getter = PropertyValidationErrors.getMismatchProperties("testProp", expectedIndex)
                    for (actualIndex in indices) {
                        if (expectedIndex != actualIndex) {
                            val result = getter(actualIndex)
                            result.isInvalid shouldBe true
                        }
                    }
                }
            }

            test("works for constraint type indices") {
                val constraintIndices =
                    listOf(
                        BOOLEAN_CONSTRAINTS_CLASS_INDEX,
                        NUMBER_CONSTRAINTS_CLASS_INDEX,
                        STRING_CONSTRAINTS_CLASS_INDEX,
                        SCHEDULE_CONSTRAINTS_CLASS_INDEX,
                        UNKNOWN_CONSTRAINTS_CLASS_INDEX,
                    )

                for (expectedIndex in constraintIndices) {
                    val getter = PropertyValidationErrors.getMismatchProperties("param", expectedIndex)
                    for (actualIndex in constraintIndices) {
                        if (expectedIndex != actualIndex) {
                            val result = getter(actualIndex)
                            result.isInvalid shouldBe true
                        }
                    }
                }
            }
        }

        context("MismatchPropertiesGetter") {
            test("invoke operator returns validation result") {
                val getter = PropertyValidationErrors.getMismatchProperties("prop", STRING_VALUE_CLASS_INDEX)
                val result = getter(BOOLEAN_VALUE_CLASS_INDEX)
                result.isInvalid shouldBe true
            }
        }
    })
