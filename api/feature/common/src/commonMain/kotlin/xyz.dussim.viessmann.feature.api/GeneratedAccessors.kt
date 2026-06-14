package xyz.dussim.viessmann.feature.api

// @Suppress("NOTHING_TO_INLINE") is required on all three below: inlining is necessary so the call site can
// access GeneratedAccessException, which is @PublishedApi internal. Without inline the compiler rejects the
// reference at non-inline call sites in generated (non-library) code.

/** Missing key throws [GeneratedAccessException]. */
@Suppress("NOTHING_TO_INLINE")
inline fun EfficientStringKeyMap<Property>.requireProperty(
    name: String,
    hash: Long,
): Property = this[name, hash] ?: throw GeneratedAccessException

/** Missing key throws [GeneratedAccessException]. */
@Suppress("NOTHING_TO_INLINE")
inline fun EfficientStringKeyMap<Command>.requireCommand(
    name: String,
    hash: Long,
): Command = this[name, hash] ?: throw GeneratedAccessException

/** Missing key throws [GeneratedAccessException]. */
@Suppress("NOTHING_TO_INLINE")
inline fun EfficientStringKeyMap<Parameter>.requireParam(
    name: String,
    hash: Long,
): Parameter = this[name, hash] ?: throw GeneratedAccessException

/**
 * Missing key throws. Type mismatch throws [GeneratedAccessException].
 */
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValue(
    name: String,
    hash: Long,
): T {
    val value = requireProperty(name, hash).value
    return value as? T ?: throw GeneratedAccessException
}

/**
 * Missing key returns null. Type mismatch throws [GeneratedAccessException].
 */
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValueOrNullIfMissing(
    name: String,
    hash: Long,
): T? {
    val value = this[name, hash]?.value ?: return null
    return value as? T ?: throw GeneratedAccessException
}

/**
 * Missing key returns null. Type mismatch returns null.
 */
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.findPropertyValueOrNull(
    name: String,
    hash: Long,
): T? {
    val value = this[name, hash]?.value ?: return null
    return value as? T
}

fun EfficientStringKeyMap<Property>.requireNullableBooleanPropertyValue(
    name: String,
    hash: Long,
): NullableBooleanValue =
    when (val value = requireProperty(name, hash).value) {
        is NullableBooleanValue -> value
        is BooleanValue -> NullableBooleanValue(value.element)
        else -> throw GeneratedAccessException
    }

fun EfficientStringKeyMap<Property>.requireNullableDoublePropertyValue(
    name: String,
    hash: Long,
): NullableDoubleValue =
    when (val value = requireProperty(name, hash).value) {
        is NullableDoubleValue -> value
        is DoubleValue -> NullableDoubleValue(value.element)
        else -> throw GeneratedAccessException
    }

fun EfficientStringKeyMap<Property>.requireNullableStringPropertyValue(
    name: String,
    hash: Long,
): NullableStringValue =
    when (val value = requireProperty(name, hash).value) {
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
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValueOrPromoteEmpty(
    name: String,
    hash: Long,
    defaultValue: T,
): T =
    when (val value = requireProperty(name, hash).value) {
        is T -> value
        ListEmptyValue -> defaultValue
        else -> throw GeneratedAccessException
    }

/**
 * Missing key returns `defaultValue`. Type mismatch and `ListEmptyValue` return `defaultValue`.
 */
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.findPropertyValueOrPromoteEmpty(
    name: String,
    hash: Long,
    defaultValue: T,
): T {
    val value = this[name, hash]?.value ?: return defaultValue
    return when (value) {
        is T -> value
        ListEmptyValue -> defaultValue
        else -> defaultValue
    }
}

/**
 * Type mismatch throws [GeneratedAccessException].
 */
inline fun <reified T : Constraints<*>> Constraints<*>.requireConstraints(): T = this as? T ?: throw GeneratedAccessException

/**
 * Type mismatch throws, except `ArrayEmptyConstraints` is promoted via [fromEmpty].
 */
inline fun <reified T : ArrayConstraints> Constraints<*>.requireArrayConstraintsOrPromoteEmpty(fromEmpty: (minLength: Int?, maxLength: Int?) -> T): T =
    when (this) {
        is T -> this
        is ArrayEmptyConstraints -> fromEmpty(minLength, maxLength)
        else -> throw GeneratedAccessException
    }
