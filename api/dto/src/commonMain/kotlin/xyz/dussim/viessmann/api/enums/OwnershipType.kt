package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.OwnershipTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.OwnershipTypeInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the ownership type of `Installation`.
 *
 * A gateway can have one of the following ownership types:
 * * [ResidentialEndUser]
 * * [PreCommissioning]
 * * [Oma]
 * * [CommercialV1]
 * * [CommercialV2]
 * * [None]
 * * [Unknown]
 */
@Serializable(with = OwnershipType.Serializer::class)
sealed interface OwnershipType : ViessmannEnum {
    /**
     * Represents the strictly defined ownership types.
     *
     * This sealed class encompasses all known and valid ownership types,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known ownership types.
     *
     * Subtypes of this class are the only valid instances of [OwnershipType],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : OwnershipType

    data object ResidentialEndUser : Strict("ResidentialEndUser")

    data object PreCommissioning : Strict("PreCommissioning")

    data object Oma : Strict("Oma")

    data object CommercialV1 : Strict("CommercialV1")

    data object CommercialV2 : Strict("CommercialV2")

    data object None : Strict("None")

    /**
     * Represents an unknown [OwnershipType].
     *
     * This is a special value used to represent an ownership type that is not
     * known to the library. The [name] of this value is the ownership type provided by
     * the API.
     *
     * @param name the heating type returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : OwnershipType,
        UnknownEnumValue

    companion object :
        InstanceFactory<OwnershipType> by OwnershipTypeInstanceFactory,
        EntryHolder<Strict> by OwnershipTypeEntryHolder

    object Serializer : KSerializer<OwnershipType> by instanceFactorySerializer(OwnershipType)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<OwnershipType, _>()
}
