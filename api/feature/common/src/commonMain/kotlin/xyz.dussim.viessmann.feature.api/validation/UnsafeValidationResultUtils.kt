package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

@RequiresOptIn("Unsafe validation 'of' methods only works when partial results have at most one error inside, in other case they will lead to class cast exceptions")
annotation class UnsafeValidationResultOf

@Suppress("UNCHECKED_CAST")
object UnsafeValidationResultUtils {
    @UnsafeValidationResultOf
    fun <E> of(result: ValidationResult<E>): ValidationResult<E> = result

    @UnsafeValidationResultOf
    fun <E> of(
        result1: ValidationResult<E>,
        result2: ValidationResult<E>,
    ): ValidationResult<E> {
        val mask =
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
            when (mask.countTrailingZeroBits()) {
                0 -> return result1
                1 -> return result2
            }
        }
        val array = arrayOfNulls<Any>(capacity)

        twoBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
        )

        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
    fun <E> of(
        result1: ValidationResult<E>,
        result2: ValidationResult<E>,
        result3: ValidationResult<E>,
    ): ValidationResult<E> {
        val mask =
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
            when (mask.countTrailingZeroBits()) {
                0 -> return result1
                1 -> return result2
                2 -> return result3
            }
        }
        val array = arrayOfNulls<Any>(capacity)
        threeBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
        )

        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
    fun <E> of(
        result1: ValidationResult<E>,
        result2: ValidationResult<E>,
        result3: ValidationResult<E>,
        result4: ValidationResult<E>,
    ): ValidationResult<E> {
        val mask =
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
            when (mask.countTrailingZeroBits()) {
                0 -> return result1
                1 -> return result2
                2 -> return result3
                3 -> return result4
            }
        }
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
    fun <E> of(
        result1: ValidationResult<E>,
        result2: ValidationResult<E>,
        result3: ValidationResult<E>,
        result4: ValidationResult<E>,
        result5: ValidationResult<E>,
    ): ValidationResult<E> {
        val mask =
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
            when (mask.countTrailingZeroBits()) {
                0 -> return result1
                1 -> return result2
                2 -> return result3
                3 -> return result4
                4 -> return result5
            }
        }
        val array = arrayOfNulls<Any>(capacity)
        threeBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
        )
        twoBitsChunk(
            array = array,
            startIndex = (mask and 0b0111).countOneBits(),
            mask = mask shr 3,
            result1 = result4,
            result2 = result5,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
    fun <E> of(
        result1: ValidationResult<E>,
        result2: ValidationResult<E>,
        result3: ValidationResult<E>,
        result4: ValidationResult<E>,
        result5: ValidationResult<E>,
        result6: ValidationResult<E>,
    ): ValidationResult<E> {
        val mask =
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
            when (mask.countTrailingZeroBits()) {
                0 -> return result1
                1 -> return result2
                2 -> return result3
                3 -> return result4
                4 -> return result5
                5 -> return result6
            }
        }
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        twoBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        threeBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        threeBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
        )
        twoBitsChunk(
            array,
            startIndex = (mask and 0b111_1111).countOneBits(),
            mask = mask shr 7,
            result1 = result8,
            result2 = result9,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        twoBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        threeBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10) or
                (result12.isInvalidBit shl 11)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
            result4 = result12,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10) or
                (result12.isInvalidBit shl 11) or
                (result13.isInvalidBit shl 12)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        threeBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
        )
        twoBitsChunk(
            array,
            startIndex = (mask and 0b111_1111_1111).countOneBits(),
            mask = mask shr 11,
            result1 = result12,
            result2 = result13,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10) or
                (result12.isInvalidBit shl 11) or
                (result13.isInvalidBit shl 12) or
                (result14.isInvalidBit shl 13)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array = array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        fourBitsChunk(
            array = array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
            result4 = result12,
        )
        twoBitsChunk(
            array = array,
            startIndex = (mask and 0b1111_1111_1111).countOneBits(),
            mask = mask shr 12,
            result1 = result13,
            result2 = result14,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10) or
                (result12.isInvalidBit shl 11) or
                (result13.isInvalidBit shl 12) or
                (result14.isInvalidBit shl 13) or
                (result15.isInvalidBit shl 14)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array = array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array = array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        fourBitsChunk(
            array = array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
            result4 = result12,
        )
        threeBitsChunk(
            array = array,
            startIndex = (mask and 0b1111_1111_1111).countOneBits(),
            mask = mask shr 12,
            result1 = result13,
            result2 = result14,
            result3 = result15,
        )
        return Invalid(array as Array<E>)
    }

    @UnsafeValidationResultOf
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
            result1.isInvalidBit or
                (result2.isInvalidBit shl 1) or
                (result3.isInvalidBit shl 2) or
                (result4.isInvalidBit shl 3) or
                (result5.isInvalidBit shl 4) or
                (result6.isInvalidBit shl 5) or
                (result7.isInvalidBit shl 6) or
                (result8.isInvalidBit shl 7) or
                (result9.isInvalidBit shl 8) or
                (result10.isInvalidBit shl 9) or
                (result11.isInvalidBit shl 10) or
                (result12.isInvalidBit shl 11) or
                (result13.isInvalidBit shl 12) or
                (result14.isInvalidBit shl 13) or
                (result15.isInvalidBit shl 14) or
                (result16.isInvalidBit shl 15)
        if (mask == 0) return ValidationResult.Valid
        val capacity = mask.countOneBits()
        if (capacity == 1) {
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
        val array = arrayOfNulls<Any>(capacity)
        fourBitsChunk(
            array,
            startIndex = 0,
            mask = mask,
            result1 = result1,
            result2 = result2,
            result3 = result3,
            result4 = result4,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111).countOneBits(),
            mask = mask shr 4,
            result1 = result5,
            result2 = result6,
            result3 = result7,
            result4 = result8,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111).countOneBits(),
            mask = mask shr 8,
            result1 = result9,
            result2 = result10,
            result3 = result11,
            result4 = result12,
        )
        fourBitsChunk(
            array,
            startIndex = (mask and 0b1111_1111_1111).countOneBits(),
            mask = mask shr 12,
            result1 = result13,
            result2 = result14,
            result3 = result15,
            result4 = result16,
        )
        return Invalid(array as Array<E>)
    }
}
