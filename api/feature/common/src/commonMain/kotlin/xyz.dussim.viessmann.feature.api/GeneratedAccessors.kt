@file:OptIn(ViessmannApiInternalExceptionUsage::class)

package xyz.dussim.viessmann.feature.api

import kotlin.reflect.KClass

private fun <T : Any> KClass<T>.castOrNull(value: Any?): T? {
    if (!isInstance(value)) return null

    @Suppress("UNCHECKED_CAST")
    return value as T
}

/** Missing key throws [GeneratedAccessException]. */
fun EfficientStringKeyMap<Property>.requireProperty(
    name: String,
    hash: Long,
): Property = this[name, hash] ?: throw GeneratedAccessException

/** Missing key throws [GeneratedAccessException]. */
fun EfficientStringKeyMap<Command>.requireCommand(
    name: String,
    hash: Long,
): Command = this[name, hash] ?: throw GeneratedAccessException

/** Missing key throws [GeneratedAccessException]. */
fun EfficientStringKeyMap<Parameter>.requireParam(
    name: String,
    hash: Long,
): Parameter = this[name, hash] ?: throw GeneratedAccessException

/**
 * Missing key throws. Type mismatch throws [GeneratedAccessException].
 */
fun <T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValue(
    name: String,
    hash: Long,
    expectedType: KClass<T>,
): T = expectedType.castOrNull(this[name, hash]?.value) ?: throw GeneratedAccessException

/**
 * Missing key returns null. Type mismatch throws [GeneratedAccessException].
 */
fun <T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValueOrNullIfMissing(
    name: String,
    hash: Long,
    expectedType: KClass<T>,
): T? {
    val value = this[name, hash]?.value ?: return null
    return expectedType.castOrNull(value) ?: throw GeneratedAccessException
}

/**
 * Missing key returns null. Type mismatch returns null.
 */
fun <T : PropertyValue<*>> EfficientStringKeyMap<Property>.findPropertyValueOrNull(
    name: String,
    hash: Long,
    expectedType: KClass<T>,
): T? {
    val value = this[name, hash]?.value ?: return null
    return expectedType.castOrNull(value)
}

fun EfficientStringKeyMap<Property>.requireNullableBooleanPropertyValue(
    name: String,
    hash: Long,
): NullableBooleanValue =
    when (val value = this[name, hash]?.value) {
        is NullableBooleanValue -> value
        is BooleanValue -> NullableBooleanValue(value.element)
        else -> throw GeneratedAccessException
    }

fun EfficientStringKeyMap<Property>.requireNullableDoublePropertyValue(
    name: String,
    hash: Long,
): NullableDoubleValue =
    when (val value = this[name, hash]?.value) {
        is NullableDoubleValue -> value
        is DoubleValue -> NullableDoubleValue(value.element)
        else -> throw GeneratedAccessException
    }

fun EfficientStringKeyMap<Property>.requireNullableStringPropertyValue(
    name: String,
    hash: Long,
): NullableStringValue =
    when (val value = this[name, hash]?.value) {
        is NullableStringValue -> value
        is StringValue -> NullableStringValue(value.element)
        else -> throw GeneratedAccessException
    }

fun EfficientStringKeyMap<Property>.findNullableBooleanPropertyValueOrNull(
    name: String,
    hash: Long,
): NullableBooleanValue? =
    when (val value = this[name, hash]?.value ?: return null) {
        is NullableBooleanValue -> value
        is BooleanValue -> NullableBooleanValue(value.element)
        else -> null
    }

fun EfficientStringKeyMap<Property>.findNullableDoublePropertyValueOrNull(
    name: String,
    hash: Long,
): NullableDoubleValue? =
    when (val value = this[name, hash]?.value ?: return null) {
        is NullableDoubleValue -> value
        is DoubleValue -> NullableDoubleValue(value.element)
        else -> null
    }

fun EfficientStringKeyMap<Property>.findNullableStringPropertyValueOrNull(
    name: String,
    hash: Long,
): NullableStringValue? =
    when (val value = this[name, hash]?.value ?: return null) {
        is NullableStringValue -> value
        is StringValue -> NullableStringValue(value.element)
        else -> null
    }

/**
 * Missing key throws. Type mismatch and `ListEmptyValue` return `defaultValue`.
 */
fun <T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValueOrPromoteEmpty(
    name: String,
    hash: Long,
    expectedType: KClass<T>,
    defaultValue: T,
): T =
    when (val value = this[name, hash]?.value) {
        ListEmptyValue -> defaultValue
        else -> expectedType.castOrNull(value) ?: throw GeneratedAccessException
    }

/**
 * Missing key returns `defaultValue`. Type mismatch and `ListEmptyValue` return `defaultValue`.
 */
fun <T : PropertyValue<*>> EfficientStringKeyMap<Property>.findPropertyValueOrPromoteEmpty(
    name: String,
    hash: Long,
    expectedType: KClass<T>,
    defaultValue: T,
): T {
    val value = this[name, hash]?.value ?: return defaultValue
    return when (value) {
        ListEmptyValue -> defaultValue
        else -> expectedType.castOrNull(value) ?: defaultValue
    }
}

/**
 * Type mismatch throws [GeneratedAccessException].
 */
fun <T : Constraints<*>> Constraints<*>.requireConstraints(expectedType: KClass<T>): T = expectedType.castOrNull(this) ?: throw GeneratedAccessException

/**
 * Type mismatch throws, except `ArrayEmptyConstraints` is promoted via [fromEmpty].
 */
fun <T : ArrayConstraints> Constraints<*>.requireArrayConstraintsOrPromoteEmpty(
    expectedType: KClass<T>,
    fromEmpty: (minLength: Int?, maxLength: Int?) -> T,
): T =
    when (this) {
        is ArrayEmptyConstraints -> fromEmpty(minLength, maxLength)
        else -> expectedType.castOrNull(this) ?: throw GeneratedAccessException
    }
