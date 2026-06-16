package xyz.dussim.viessmann.feature.api.validation

import kotlin.jvm.JvmInline

@Suppress("FunctionName")
fun <E> Valid(): ValidationResult<E> = ValidationResult.Valid

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is only valid when every input ValidationResult is known to contain at most one error.",
)
annotation class SingleErrorValidationResultApi

@JvmInline
value class ValidationResult<out T>
    @PublishedApi
    internal constructor(
        @PublishedApi
        internal val value: Any?,
    ) {
        inline val size: Int
            get() =
                when (value) {
                    null -> 0
                    else -> (value as? Array<*>)?.size ?: 1
                }

        inline val isInvalid: Boolean
            get() = value != null

        @PublishedApi
        internal inline val invalidFlag: Int
            get() = if (isInvalid) 1 else 0

        @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
        internal inline fun singleValue(): T = value as T

        @Suppress("UNCHECKED_CAST")
        inline fun forEach(crossinline action: (T) -> Unit) {
            when (value) {
                null -> Unit

                !is Array<*> -> action(value as T)

                else -> for (i in 0..<value.size) {
                    action(value[i] as T)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        inline fun <R> map(crossinline transform: (T) -> R): List<R> =
            when (value) {
                null -> emptyList()
                is Array<*> -> value.map { transform(it as T) }.toList()
                else -> listOf(transform(value as T))
            }

        @Suppress("UNCHECKED_CAST")
        inline fun asIterable(): Iterable<T> =
            when (value) {
                null -> emptyList()
                is Array<*> -> value.asIterable() as Iterable<T>
                else -> listOf(value as T)
            }

        @Suppress("UNCHECKED_CAST")
        companion object {
            val Valid = ValidationResult<Nothing>(null)

            @Suppress("ktlint:standard:function-naming", "FunctionName")
            fun <E> Invalid(value: E): ValidationResult<E> = ValidationResult(value)

            @Suppress("ktlint:standard:function-naming", "FunctionName")
            fun <E> Invalid(values: Array<E>): ValidationResult<E> = ValidationResult(values)

            fun <E> of(): ValidationResult<E> = Valid

            fun <E> of(error: E): ValidationResult<E> = Invalid(error)

            fun <E> of(result1: ValidationResult<E>): ValidationResult<E> = result1

            @SingleErrorValidationResultApi
            fun <E> ofSingleErrorResults(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
            ): ValidationResult<E> =
                when (result1.invalidFlag or (result2.invalidFlag shl 1)) {
                    0 -> Valid
                    1 -> result1
                    2 -> result2
                    else -> Invalid(arrayOf<Any?>(result1.singleValue(), result2.singleValue()) as Array<E>)
                }

            @SingleErrorValidationResultApi
            fun <E> ofSingleErrorResults(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
            ): ValidationResult<E> =
                when (result1.invalidFlag or (result2.invalidFlag shl 1) or (result3.invalidFlag shl 2)) {
                    0 -> Valid
                    1 -> result1
                    2 -> result2
                    3 -> Invalid(arrayOf<Any?>(result1.singleValue(), result2.singleValue()) as Array<E>)
                    4 -> result3
                    5 -> Invalid(arrayOf<Any?>(result1.singleValue(), result3.singleValue()) as Array<E>)
                    6 -> Invalid(arrayOf<Any?>(result2.singleValue(), result3.singleValue()) as Array<E>)
                    else -> Invalid(arrayOf<Any?>(result1.singleValue(), result2.singleValue(), result3.singleValue()) as Array<E>)
                }

            // region generated-validation-result-of
            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
                result26: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24) or
                        (result26.invalidFlag shl 25)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                        25 -> return result26
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size +
                            result26.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                result26.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
                result26: ValidationResult<E>,
                result27: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24) or
                        (result26.invalidFlag shl 25) or
                        (result27.invalidFlag shl 26)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                        25 -> return result26
                        26 -> return result27
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size +
                            result26.size +
                            result27.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                result26.forEach { array[current++] = it }
                result27.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
                result26: ValidationResult<E>,
                result27: ValidationResult<E>,
                result28: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24) or
                        (result26.invalidFlag shl 25) or
                        (result27.invalidFlag shl 26) or
                        (result28.invalidFlag shl 27)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                        25 -> return result26
                        26 -> return result27
                        27 -> return result28
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size +
                            result26.size +
                            result27.size +
                            result28.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                result26.forEach { array[current++] = it }
                result27.forEach { array[current++] = it }
                result28.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
                result26: ValidationResult<E>,
                result27: ValidationResult<E>,
                result28: ValidationResult<E>,
                result29: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24) or
                        (result26.invalidFlag shl 25) or
                        (result27.invalidFlag shl 26) or
                        (result28.invalidFlag shl 27) or
                        (result29.invalidFlag shl 28)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                        25 -> return result26
                        26 -> return result27
                        27 -> return result28
                        28 -> return result29
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size +
                            result26.size +
                            result27.size +
                            result28.size +
                            result29.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                result26.forEach { array[current++] = it }
                result27.forEach { array[current++] = it }
                result28.forEach { array[current++] = it }
                result29.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
                result4: ValidationResult<E>,
                result5: ValidationResult<E>,
                result6: ValidationResult<E>,
                result7: ValidationResult<E>,
                result8: ValidationResult<E>,
                result9: ValidationResult<E>,
                result10: ValidationResult<E>,
                result11: ValidationResult<E>,
                result12: ValidationResult<E>,
                result13: ValidationResult<E>,
                result14: ValidationResult<E>,
                result15: ValidationResult<E>,
                result16: ValidationResult<E>,
                result17: ValidationResult<E>,
                result18: ValidationResult<E>,
                result19: ValidationResult<E>,
                result20: ValidationResult<E>,
                result21: ValidationResult<E>,
                result22: ValidationResult<E>,
                result23: ValidationResult<E>,
                result24: ValidationResult<E>,
                result25: ValidationResult<E>,
                result26: ValidationResult<E>,
                result27: ValidationResult<E>,
                result28: ValidationResult<E>,
                result29: ValidationResult<E>,
                result30: ValidationResult<E>,
            ): ValidationResult<E> {
                val mask =
                    result1.invalidFlag or
                        (result2.invalidFlag shl 1) or
                        (result3.invalidFlag shl 2) or
                        (result4.invalidFlag shl 3) or
                        (result5.invalidFlag shl 4) or
                        (result6.invalidFlag shl 5) or
                        (result7.invalidFlag shl 6) or
                        (result8.invalidFlag shl 7) or
                        (result9.invalidFlag shl 8) or
                        (result10.invalidFlag shl 9) or
                        (result11.invalidFlag shl 10) or
                        (result12.invalidFlag shl 11) or
                        (result13.invalidFlag shl 12) or
                        (result14.invalidFlag shl 13) or
                        (result15.invalidFlag shl 14) or
                        (result16.invalidFlag shl 15) or
                        (result17.invalidFlag shl 16) or
                        (result18.invalidFlag shl 17) or
                        (result19.invalidFlag shl 18) or
                        (result20.invalidFlag shl 19) or
                        (result21.invalidFlag shl 20) or
                        (result22.invalidFlag shl 21) or
                        (result23.invalidFlag shl 22) or
                        (result24.invalidFlag shl 23) or
                        (result25.invalidFlag shl 24) or
                        (result26.invalidFlag shl 25) or
                        (result27.invalidFlag shl 26) or
                        (result28.invalidFlag shl 27) or
                        (result29.invalidFlag shl 28) or
                        (result30.invalidFlag shl 29)

                if (mask == 0) return Valid

                if (mask.countOneBits() == 1) {
                    when (mask.countTrailingZeroBits()) {
                        0 -> return result1
                        1 -> return result2
                        2 -> return result3
                        3 -> return result4
                        4 -> return result5
                        5 -> return result6
                        6 -> return result7
                        7 -> return result8
                        8 -> return result9
                        9 -> return result10
                        10 -> return result11
                        11 -> return result12
                        12 -> return result13
                        13 -> return result14
                        14 -> return result15
                        15 -> return result16
                        16 -> return result17
                        17 -> return result18
                        18 -> return result19
                        19 -> return result20
                        20 -> return result21
                        21 -> return result22
                        22 -> return result23
                        23 -> return result24
                        24 -> return result25
                        25 -> return result26
                        26 -> return result27
                        27 -> return result28
                        28 -> return result29
                        29 -> return result30
                    }
                }

                var current = 0
                val array =
                    arrayOfNulls<Any>(
                        result1.size +
                            result2.size +
                            result3.size +
                            result4.size +
                            result5.size +
                            result6.size +
                            result7.size +
                            result8.size +
                            result9.size +
                            result10.size +
                            result11.size +
                            result12.size +
                            result13.size +
                            result14.size +
                            result15.size +
                            result16.size +
                            result17.size +
                            result18.size +
                            result19.size +
                            result20.size +
                            result21.size +
                            result22.size +
                            result23.size +
                            result24.size +
                            result25.size +
                            result26.size +
                            result27.size +
                            result28.size +
                            result29.size +
                            result30.size,
                    )
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                result3.forEach { array[current++] = it }
                result4.forEach { array[current++] = it }
                result5.forEach { array[current++] = it }
                result6.forEach { array[current++] = it }
                result7.forEach { array[current++] = it }
                result8.forEach { array[current++] = it }
                result9.forEach { array[current++] = it }
                result10.forEach { array[current++] = it }
                result11.forEach { array[current++] = it }
                result12.forEach { array[current++] = it }
                result13.forEach { array[current++] = it }
                result14.forEach { array[current++] = it }
                result15.forEach { array[current++] = it }
                result16.forEach { array[current++] = it }
                result17.forEach { array[current++] = it }
                result18.forEach { array[current++] = it }
                result19.forEach { array[current++] = it }
                result20.forEach { array[current++] = it }
                result21.forEach { array[current++] = it }
                result22.forEach { array[current++] = it }
                result23.forEach { array[current++] = it }
                result24.forEach { array[current++] = it }
                result25.forEach { array[current++] = it }
                result26.forEach { array[current++] = it }
                result27.forEach { array[current++] = it }
                result28.forEach { array[current++] = it }
                result29.forEach { array[current++] = it }
                result30.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }
            // endregion generated-validation-result-of
        }
    }

fun interface ValidationRule<T, E> {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T, E> and(
            rule: ValidationRule<T, E>,
            vararg rules: ValidationRule<T, E>,
        ): ValidationRule<T, E> =
            ValidationRule { value ->
                rules.fold(rule(value)) { acc, currentRule ->
                    ValidationResult.of(acc, currentRule(value))
                }
            }

        @Suppress("UNCHECKED_CAST")
        fun <T, E> or(
            rule: ValidationRule<T, E>,
            vararg rules: ValidationRule<T, E>,
        ): ValidationRule<T, E> =
            ValidationRule { value ->
                rules.fold(rule(value)) { acc, currentRule ->
                    if (!acc.isInvalid) {
                        return@ValidationRule acc
                    }
                    val next = currentRule(value)
                    if (!next.isInvalid) {
                        return@ValidationRule next
                    }
                    ValidationResult.of(acc, next)
                }
            }
    }

    fun validate(value: T): ValidationResult<E>
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T, E> ValidationRule<T, E>.invoke(value: T): ValidationResult<E> = validate(value)

/**
 * Transforms a validation rule to work on a different input type by applying a transformation function.
 */
inline fun <From, To, E> ValidationRule<To, E>.transform(crossinline transform: (From) -> To): ValidationRule<From, E> = ValidationRule { value -> this(transform(value)) }

/**
 * Validates all values in the iterable, accumulating all errors into a single result.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T, E> ValidationRule<T, E>.validateAll(values: Iterable<T>): ValidationResult<E> =
    values.fold(Valid()) { acc, value ->
        ValidationResult.of(acc, invoke(value))
    }

/**
 * Validates all values in the sequence, accumulating all errors into a single result.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T, E> ValidationRule<T, E>.validateAll(values: Sequence<T>): ValidationResult<E> = validateAll(values.asIterable())

/**
 * Executes the given effect for each error in an invalid result, returning the original result.
 */
inline fun <E> ValidationResult<E>.onError(crossinline effect: (E) -> Unit): ValidationResult<E> = apply { forEach(effect) }
