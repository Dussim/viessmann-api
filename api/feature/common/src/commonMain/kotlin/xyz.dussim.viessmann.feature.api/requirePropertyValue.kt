package xyz.dussim.viessmann.feature.api

/**
 * Missing key throws. Type mismatch throws [GeneratedAccessException].
 */
@ViessmannApiInternalExceptionUsage
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValue2(
    name: String,
    hash: Long,
): T {
    return  this[name, hash]?.value as? T ?: throw GeneratedAccessException
}

/**
 * Missing key throws. Type mismatch throws [GeneratedAccessException].
 */
@ViessmannApiInternalExceptionUsage
inline fun <reified T : PropertyValue<*>> EfficientStringKeyMap<Property>.requirePropertyValue3(
    name: String,
    hash: Long,
): T {
    return this[name, hash]?.value as? T ?: throw GeneratedAccessException
}