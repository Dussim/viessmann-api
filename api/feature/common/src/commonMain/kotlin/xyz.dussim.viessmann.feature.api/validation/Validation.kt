package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import kotlin.jvm.JvmInline

@Suppress("FunctionName")
fun <E> Valid(): ValidationResult<E> = ValidationResult.Valid

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
                    !is Array<*> -> 1
                    else -> value.size
                }

        inline val isInvalid: Boolean
            get() = value != null

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
        companion object {
            val Valid = ValidationResult<Nothing>(null)

            @Suppress("ktlint:standard:function-naming", "FunctionName")
            fun <E> Invalid(value: E): ValidationResult<E> = ValidationResult(value)

            @Suppress("ktlint:standard:function-naming", "FunctionName")
            fun <E> Invalid(values: Array<E>): ValidationResult<E> = ValidationResult(values)

            fun <E> of(): ValidationResult<E> = Valid

            fun <E> of(error: E): ValidationResult<E> = Invalid(error)

            fun <E> of(result1: ValidationResult<E>): ValidationResult<E> = result1

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
            ): ValidationResult<E> {
                if (!result1.isInvalid &&
                    !result2.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
                result1.forEach { array[current++] = it }
                result2.forEach { array[current++] = it }
                return Invalid(array as Array<E>)
            }

            fun <E> of(
                result1: ValidationResult<E>,
                result2: ValidationResult<E>,
                result3: ValidationResult<E>,
            ): ValidationResult<E> {
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size +
                        result6.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size +
                        result6.size +
                        result7.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size +
                        result6.size +
                        result7.size +
                        result8.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size +
                        result6.size +
                        result7.size +
                        result8.size +
                        result9.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid && !result2.isInvalid && !result3.isInvalid && !result4.isInvalid && !result5.isInvalid && !result6.isInvalid && !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid
                ) {
                    return Valid
                }
                val capacity =
                    result1.size +
                        result2.size +
                        result3.size +
                        result4.size +
                        result5.size +
                        result6.size +
                        result7.size +
                        result8.size +
                        result9.size +
                        result10.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result11.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid &&
                    !result12.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result12.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                        result12.isInvalid -> return result12
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid &&
                    !result12.isInvalid &&
                    !result13.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result13.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                        result12.isInvalid -> return result12
                        result13.isInvalid -> return result13
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid &&
                    !result12.isInvalid &&
                    !result13.isInvalid &&
                    !result14.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result14.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                        result12.isInvalid -> return result12
                        result13.isInvalid -> return result13
                        result14.isInvalid -> return result14
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid &&
                    !result12.isInvalid &&
                    !result13.isInvalid &&
                    !result14.isInvalid &&
                    !result15.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result15.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                        result12.isInvalid -> return result12
                        result13.isInvalid -> return result13
                        result14.isInvalid -> return result14
                        result15.isInvalid -> return result15
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                if (!result1.isInvalid &&
                    !result2.isInvalid &&
                    !result3.isInvalid &&
                    !result4.isInvalid &&
                    !result5.isInvalid &&
                    !result6.isInvalid &&
                    !result7.isInvalid &&
                    !result8.isInvalid &&
                    !result9.isInvalid &&
                    !result10.isInvalid &&
                    !result11.isInvalid &&
                    !result12.isInvalid &&
                    !result13.isInvalid &&
                    !result14.isInvalid &&
                    !result15.isInvalid &&
                    !result16.isInvalid
                ) {
                    return Valid
                }
                val capacity =
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
                        result16.size
                if (capacity == 1) {
                    when {
                        result1.isInvalid -> return result1
                        result2.isInvalid -> return result2
                        result3.isInvalid -> return result3
                        result4.isInvalid -> return result4
                        result5.isInvalid -> return result5
                        result6.isInvalid -> return result6
                        result7.isInvalid -> return result7
                        result8.isInvalid -> return result8
                        result9.isInvalid -> return result9
                        result10.isInvalid -> return result10
                        result11.isInvalid -> return result11
                        result12.isInvalid -> return result12
                        result13.isInvalid -> return result13
                        result14.isInvalid -> return result14
                        result15.isInvalid -> return result15
                        result16.isInvalid -> return result16
                    }
                }
                var current = 0
                val array = arrayOfNulls<Any>(capacity)
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
                rules.fold(rule(value)) { acc, rule ->
                    val next = rule(value)
                    when {
                        acc.isInvalid ->
                            when {
                                next.isInvalid -> {
                                    var index = 0
                                    val array = arrayOfNulls<Any>(acc.size + next.size)
                                    acc.forEach { element -> array[index++] = element }
                                    next.forEach { element -> array[index++] = element }
                                    Invalid(values = array as Array<E>)
                                }

                                else -> acc
                            }

                        else -> next
                    }
                }
            }

        @Suppress("UNCHECKED_CAST")
        fun <T, E> or(
            rule: ValidationRule<T, E>,
            vararg rules: ValidationRule<T, E>,
        ): ValidationRule<T, E> =
            ValidationRule { value ->
                rules.fold(rule(value)) { acc, rule ->
                    when {
                        acc.isInvalid -> {
                            val next = rule(value)
                            if (!next.isInvalid) {
                                return@ValidationRule Valid()
                            }
                            var index = 0
                            val array = arrayOfNulls<Any>(acc.size + next.size)
                            acc.forEach { element -> array[index++] = element }
                            next.forEach { element -> array[index++] = element }
                            Invalid(values = array as Array<E>)
                        }

                        else -> return@ValidationRule Valid()
                    }
                }
            }
    }

    fun validate(value: T): ValidationResult<E>
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T, E> ValidationRule<T, E>.invoke(value: T): ValidationResult<E> = validate(value)

inline fun <T, R, E> ValidationRule<R, E>.transform(crossinline transform: (T) -> R): ValidationRule<T, E> = ValidationRule { value -> this(transform(value)) }

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T, E> ValidationRule<T, E>.validateAll(values: Iterable<T>): ValidationResult<E> =
    values.fold(Valid()) { acc, value ->
        val next = invoke(value)
        when {
            acc.isInvalid ->
                when {
                    next.isInvalid -> {
                        var index = 0
                        val array = arrayOfNulls<Any>(acc.size + next.size)
                        acc.forEach { element -> array[index++] = element }
                        next.forEach { element -> array[index++] = element }
                        Invalid(values = array as Array<E>)
                    }

                    else -> acc
                }

            else -> next
        }
    }

@Suppress("NOTHING_TO_INLINE")
inline fun <T, E> ValidationRule<T, E>.validateAll(values: Sequence<T>): ValidationResult<E> = validateAll(values.asIterable())

inline fun <E> ValidationResult<E>.onError(crossinline effect: (E) -> Unit): ValidationResult<E> =
    apply {
        forEach(effect)
    }
