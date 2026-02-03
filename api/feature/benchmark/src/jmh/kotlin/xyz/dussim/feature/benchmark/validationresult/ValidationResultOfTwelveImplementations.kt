package xyz.dussim.feature.benchmark.validationresult

import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

@Suppress("UNCHECKED_CAST")
fun <E> ofOldTwelve(
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
    if (!result1.isInvalid && !result2.isInvalid && !result3.isInvalid && !result4.isInvalid &&
        !result5.isInvalid && !result6.isInvalid && !result7.isInvalid && !result8.isInvalid &&
        !result9.isInvalid && !result10.isInvalid && !result11.isInvalid && !result12.isInvalid
    ) {
        return Valid
    }
    val capacity =
        result1.size + result2.size + result3.size + result4.size + result5.size + result6.size +
            result7.size + result8.size + result9.size + result10.size + result11.size + result12.size
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

@Suppress("UNCHECKED_CAST")
fun <E> ofCurrentTwelve(
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
        result1.invalidFlag or (result2.invalidFlag shl 1) or (result3.invalidFlag shl 2) or
            (result4.invalidFlag shl 3) or (result5.invalidFlag shl 4) or (result6.invalidFlag shl 5) or
            (result7.invalidFlag shl 6) or (result8.invalidFlag shl 7) or (result9.invalidFlag shl 8) or
            (result10.invalidFlag shl 9) or (result11.invalidFlag shl 10) or (result12.invalidFlag shl 11)

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
            result1.size + result2.size + result3.size + result4.size + result5.size + result6.size +
                result7.size + result8.size + result9.size + result10.size + result11.size + result12.size,
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
