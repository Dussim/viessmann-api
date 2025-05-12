package xyz.dussim.viessmann.api.enums

import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.ViessmannEnumEntryHolder
import xyz.dussim.viessmann.api.utils.factories.ViessmannEnumInstanceFactory

/**
 * Base interface for all DTO enums of viessmann api.
 * */
sealed interface ViessmannEnum {
    /**
     * @property name is the value of literal string used as enum value in viessmann apis
     * */
    val name: String

    /**
     * Represents an unknown enum value.
     *
     * This class serves as a generic implementation for handling unknown values
     * in enums where the value is not recognized by the library when one of [InstanceFactory] methods
     * of [ViessmannEnum.Companion] is used.
     * It will never be a return value of any other specialized [InstanceFactory].
     * It implements the [UnknownEnumValue] interface to mark it as a placeholder for unrecognized
     * enum values.
     *
     * @property name The string value of the unknown enum returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : UnknownEnumValue

    /**
     * Companion object for the [ViessmannEnum] interface.
     *
     * This object serves as both an entry holder and a factory for instances of [ViessmannEnum].
     *
     * - It implements [EntryHolder] to provide access to a set of all well-defined enum values.
     * - It implements [InstanceFactory] to create instances of [ViessmannEnum] or its subtypes from string representations.
     *
     * The factory methods can be used to deserialize string values into appropriate enum instances
     * while handling unknown or unexpected values gracefully.
     */
    @ViessmannEnumUnstableApi
    companion object :
        EntryHolder<ViessmannEnum> by ViessmannEnumEntryHolder,
        InstanceFactory<ViessmannEnum> by ViessmannEnumInstanceFactory
}
