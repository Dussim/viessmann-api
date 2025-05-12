package xyz.dussim.viessmann.api.utils

import xyz.dussim.viessmann.api.enums.UnknownEnumValue
import xyz.dussim.viessmann.api.enums.ViessmannEnum

/**
 * A functional interface for creating instances of [ViessmannEnum] subtypes based on string values.
 *
 * @param T The type of [ViessmannEnum] that this factory creates instances of.
 */
fun interface InstanceFactory<T : ViessmannEnum> {
    class UnknownEnumValueException(
        override val message: String,
    ) : Exception()

    /**
     * Creates an instance of [T] based on the provided string value or if it is unknown it returns an intersection type of [UnknownEnumValue]  & [T]
     *
     * @param value The string value to convert into an instance of [T].
     * @return An instance of [T] corresponding to the provided value.
     */
    fun valueOf(value: String): T

    /**
     * Attempts to create an instance of [T] based on the provided string value, returning `null` if the value is unknown.
     *
     * @param value The string value to convert into an instance of [T].
     * @return An instance of [T] corresponding to the provided value, or `null` if the value is unknown.
     */
    fun valueOfOrNull(value: String): T? =
        when (val instance = valueOf(value)) {
            is UnknownEnumValue -> null
            else -> instance
        }

    /**
     * Attempts to create an instance of [T] based on the provided string value, throwing an exception if the value is unknown.
     *
     * @param value The string value to convert into an instance of [T].
     * @return An instance of [T] corresponding to the provided value.
     * @throws UnknownEnumValueException if the value is unknown.
     */
    fun valueOfOrThrow(value: String): T =
        when (val instance = valueOf(value)) {
            is UnknownEnumValue -> throw UnknownEnumValueException("No enum constant found for value: $value")
            else -> instance
        }

    /**
     * Attempts to create an instance of [T] based on the provided string value, throwing an exception produced by the provided lambda if the value is unknown.
     *
     * @param value The string value to convert into an instance of [T].
     * @param exceptionProducer Lambda that produces the exception to throw if the value is unknown.
     * @return An instance of [T] corresponding to the provided value.
     * @throws Throwable if the value is unknown and the lambda produces an exception.
     */
    fun valueOfOrThrow(
        value: String,
        exceptionProducer: (String) -> Throwable,
    ): T =
        when (val instance = valueOf(value)) {
            is UnknownEnumValue -> throw exceptionProducer(value)
            else -> instance
        }

    /**
     * Attempts to create an instance of [T] based on the provided string value, returning a default instance if the value is unknown.
     *
     * @param value The string value to convert into an instance of [T].
     * @param default The default instance to return if the value is unknown.
     * @return An instance of [T] corresponding to the provided value, or the default instance if the value is unknown.
     */
    fun valueOfOrDefault(
        value: String,
        default: T,
    ): T = valueOfOrNull(value) ?: default

    /**
     * Attempts to create an instance of [T] based on the provided string value, returning the result of the provided lambda if the value is unknown.
     *
     * @param value The string value to convert into an instance of [T].
     * @param defaultValue Lambda providing the default instance to return if the value is unknown.
     * @return An instance of [T] corresponding to the provided value, or the result of the lambda if the value is unknown.
     */
    fun valueOfOrDefault(
        value: String,
        defaultValue: () -> T,
    ): T = valueOfOrNull(value) ?: defaultValue()
}
