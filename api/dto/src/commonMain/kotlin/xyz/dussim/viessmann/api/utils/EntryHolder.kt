package xyz.dussim.viessmann.api.utils

import xyz.dussim.viessmann.api.enums.ViessmannEnum

/**
 * Interface for holding a set of well-defined enum values.
 *
 * @param T The type of [ViessmannEnum] that this holder manages.
 */
interface EntryHolder<T : ViessmannEnum> {
    /**
     * Retrieves a set of all well-defined enum values of type [T].
     *
     * @return A set of all well-defined enum values.
     */
    val entries: Set<T>

    /**
     * Checks if a [T] enum named [value] is present within the entries.
     *
     * @param value The string value to check for presence in the entries.
     * @return `true` if an entry with the specified name exists, otherwise `false`.
     */
    operator fun contains(value: String): Boolean = entries.any { it.name == value }

    /**
     * Checks if a [ViessmannEnum] enum value [value] is present within the entries.
     *
     * @param value The [ViessmannEnum] value to check for presence in the entries.
     * @return `true` if a specified entry exists, otherwise `false`.
     */
    operator fun contains(value: ViessmannEnum): Boolean = entries.contains(value)
}
