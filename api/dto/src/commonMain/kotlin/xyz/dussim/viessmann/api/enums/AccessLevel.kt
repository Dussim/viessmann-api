package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.AccessLevel.Serializer
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.AccessLevelEntryHolder
import xyz.dussim.viessmann.api.utils.factories.AccessLevelInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the access level of a user.
 *
 * A user can have one of the following access levels:
 * * [Owner]
 * * [FamilyMember]
 * * [Maintainer]
 * * [Support]
 * * [Installer]
 * * [ServiceContractor]
 * * [Operator]
 * * [BuildingManager]
 * * [CommercialOwner]
 * * [Partner]
 * * [Consumer]
 * * [Unknown]
 * */
@Serializable(with = Serializer::class)
sealed interface AccessLevel : ViessmannEnum {
    /**
     * Represents the strictly defined access levels.
     *
     * This sealed class encompasses all known and valid access levels,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known access levels.
     *
     * Subtypes of this class are the only valid instances of [AccessLevel],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : AccessLevel

    data object Owner : Strict("Owner")

    data object FamilyMember : Strict("FamilyMember")

    data object Maintainer : Strict("Maintainer")

    data object Support : Strict("Support")

    data object Installer : Strict("Installer")

    data object ServiceContractor : Strict("ServiceContractor")

    data object Operator : Strict("Operator")

    data object BuildingManager : Strict("BuildingManager")

    data object CommercialOwner : Strict("CommercialOwner")

    data object Partner : Strict("Partner")

    data object Consumer : Strict("Consumer")

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
    ) : AccessLevel,
        UnknownEnumValue

    companion object :
        InstanceFactory<AccessLevel> by AccessLevelInstanceFactory,
        EntryHolder<Strict> by AccessLevelEntryHolder

    object Serializer : KSerializer<AccessLevel> by instanceFactorySerializer(AccessLevel)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<AccessLevel, _>()
}
