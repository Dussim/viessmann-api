package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.HeatingTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.HeatingTypeInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the heating type of `Gateway`.
 *
 * A gateway can have one of the following heating types:
 * * [None]
 * * [FloorHeating]
 * * [Radiators]
 * * [Both]
 * * [Undefined]
 * * [Unknown]
 * */
@Serializable(with = HeatingType.Serializer::class)
sealed interface HeatingType : ViessmannEnum {
    /**
     * Represents the strictly defined heating types.
     *
     * This sealed class encompasses all known and valid heating types,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known heating types.
     *
     * Subtypes of this class are the only valid instances of [HeatingType],
     * apart from [Unknown].
     *
     * @property name The string representation of the heating type.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : HeatingType

    data object None : Strict("None")

    data object FloorHeating : Strict("FloorHeating")

    data object Radiators : Strict("Radiators")

    data object Both : Strict("Both")

    data object Undefined : Strict("Undefined")

    /**
     * Represents an unknown [AccessLevel].
     *
     * This is a special value used to represent an access level that is not
     * known to the library. The [name] of this value is the access level provided by
     * the API.
     *
     * @param name the access level returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : HeatingType,
        UnknownEnumValue

    companion object :
        InstanceFactory<HeatingType> by HeatingTypeInstanceFactory,
        EntryHolder<Strict> by HeatingTypeEntryHolder

    object Serializer : KSerializer<HeatingType> by instanceFactorySerializer(HeatingType)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<HeatingType, _>()
}
