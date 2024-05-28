package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.TargetRealm.Serializer
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.TargetRealmEntryHolder
import xyz.dussim.viessmann.api.utils.factories.TargetRealmInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the target realm of a `Gateway`.
 *
 * A gateway can have one of the following target realms:
 * * [Dc]
 * * [Genesis]
 * * [Unknown]
 * */
@Serializable(with = Serializer::class)
sealed interface TargetRealm : ViessmannEnum {
    /**
     * Represents the strictly defined target realms.
     *
     * This sealed class encompasses all known and valid target realms,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known target realms.
     *
     * Subtypes of this class are the only valid instances of [TargetRealm],
     * apart from [Unknown].
     *
     * @property name The string representation of the target realm.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : TargetRealm

    data object Dc : Strict("DC")

    data object Genesis : Strict("Genesis")

    /**
     * Represents an unknown [HeatingType].
     *
     * This is a special value used to represent a heating type that is not
     * known to the library. The [name] of this value is the heating type provided by
     * the API.
     *
     * @param name the heating type returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : TargetRealm,
        UnknownEnumValue

    companion object :
        InstanceFactory<TargetRealm> by TargetRealmInstanceFactory,
        EntryHolder<Strict> by TargetRealmEntryHolder

    object Serializer : KSerializer<TargetRealm> by instanceFactorySerializer(TargetRealm)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<TargetRealm, _>()
}
