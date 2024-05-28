package xyz.dussim.viessmann.api.utils

import xyz.dussim.viessmann.api.enums.ViessmannEnum

/**
 * Interface for holding a set of well-defined enum values.
 *
 * @param T The type of [ViessmannEnum] that this holder manages.
 */
fun interface EntryHolder<T : ViessmannEnum> {
    /**
     * Retrieves a set of all well-defined enum values of type [T].
     *
     * @return A set of all well-defined enum values.
     */
    fun getAllEntries(): Set<T>
}
