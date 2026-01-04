@file:Suppress("NOTHING_TO_INLINE")

package xyz.dussim.viessmann.feature.api.validation

internal inline fun <E> twoBitsChunk(
    array: Array<Any?>,
    startIndex: Int,
    mask: Int,
    result1: ValidationResult<E>,
    result2: ValidationResult<E>,
) {
    when (mask and 0b11) {
        0b0000 -> { // 0
        }

        0b0001 -> { // 1
            array[startIndex] = result1.value
        }

        0b0010 -> { // 2
            array[startIndex] = result2.value
        }

        0b0011 -> { // 3
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
        }
    }
}

internal inline fun <E> threeBitsChunk(
    array: Array<Any?>,
    startIndex: Int,
    mask: Int,
    result1: ValidationResult<E>,
    result2: ValidationResult<E>,
    result3: ValidationResult<E>,
) {
    when (mask and 0b111) {
        0b0000 -> { // 0
        }

        0b0001 -> { // 1
            array[startIndex] = result1.value
        }

        0b0010 -> { // 2
            array[startIndex] = result2.value
        }

        0b0011 -> { // 3
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
        }

        0b0100 -> { // 4
            array[startIndex] = result3.value
        }

        0b0101 -> { // 5
            array[startIndex] = result1.value
            array[startIndex + 1] = result3.value
        }

        0b0110 -> { // 6
            array[startIndex] = result2.value
            array[startIndex + 1] = result3.value
        }

        0b0111 -> { // 7
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
            array[startIndex + 2] = result3.value
        }
    }
}

internal inline fun <E> fourBitsChunk(
    array: Array<Any?>,
    startIndex: Int,
    mask: Int,
    result1: ValidationResult<E>,
    result2: ValidationResult<E>,
    result3: ValidationResult<E>,
    result4: ValidationResult<E>,
) {
    when (mask and 0b1111) {
        0b0000 -> { // 0
        }

        0b0001 -> { // 1
            array[startIndex] = result1.value
        }

        0b0010 -> { // 2
            array[startIndex] = result2.value
        }

        0b0011 -> { // 3
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
        }

        0b0100 -> { // 4
            array[startIndex] = result3.value
        }

        0b0101 -> { // 5
            array[startIndex] = result1.value
            array[startIndex + 1] = result3.value
        }

        0b0110 -> { // 6
            array[startIndex] = result2.value
            array[startIndex + 1] = result3.value
        }

        0b0111 -> { // 7
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
            array[startIndex + 2] = result3.value
        }

        0b1000 -> { // 8
            array[startIndex] = result4.value
        }

        0b1001 -> { // 9
            array[startIndex] = result1.value
            array[startIndex + 1] = result4.value
        }

        0b1010 -> { // 10
            array[startIndex] = result2.value
            array[startIndex + 1] = result4.value
        }

        0b1011 -> { // 11
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
            array[startIndex + 2] = result4.value
        }

        0b1100 -> { // 12
            array[startIndex] = result3.value
            array[startIndex + 1] = result4.value
        }

        0b1101 -> { // 13
            array[startIndex] = result1.value
            array[startIndex + 1] = result3.value
            array[startIndex + 2] = result4.value
        }

        0b1110 -> { // 14
            array[startIndex] = result2.value
            array[startIndex + 1] = result3.value
            array[startIndex + 2] = result4.value
        }

        0b1111 -> { // 15
            array[startIndex] = result1.value
            array[startIndex + 1] = result2.value
            array[startIndex + 2] = result3.value
            array[startIndex + 3] = result4.value
        }
    }
}
