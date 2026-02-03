package xyz.dussim.feature.benchmark.validationresult

import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

/**
 * Old implementation - uses isInvalid checks first, then capacity.
 */
@Suppress("UNCHECKED_CAST")
fun <E> ofOldFour(
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

/**
 * Current implementation - uses bitmask approach.
 */
@Suppress("UNCHECKED_CAST")
fun <E> ofCurrentFour(
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

    if (mask == 0) {
        return Valid
    }

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
            result1.size + result2.size + result3.size + result4.size,
        )
    result1.forEach { array[current++] = it }
    result2.forEach { array[current++] = it }
    result3.forEach { array[current++] = it }
    result4.forEach { array[current++] = it }
    return Invalid(array as Array<E>)
}
