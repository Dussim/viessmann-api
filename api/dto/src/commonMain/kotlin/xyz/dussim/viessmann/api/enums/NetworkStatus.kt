package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.NetworkStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.NetworkStatusInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the network status of a `Gateway`.
 *
 * A gateway can have one of the following network statuses:
 * * [Online]
 * * [Offline]
 * * [Unknown]
 */
@Serializable(with = NetworkStatus.Serializer::class)
sealed interface NetworkStatus : ViessmannEnum {
    /**
     * Represents the strictly defined network statuses.
     *
     * This sealed class encompasses all known and valid network statuses,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known network statuses.
     *
     * Subtypes of this class are the only valid instances of [NetworkStatus],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : NetworkStatus

    data object Online : Strict("Online")

    data object Offline : Strict("Offline")

    /**
     * Represents an unknown [NetworkStatus].
     *
     * This is a special value used to represent a network status that is not
     * known to the library. The [name] of this value is the network status returned by
     * the API.
     *
     * @param name the network status returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : NetworkStatus,
        UnknownEnumValue

    companion object :
        InstanceFactory<NetworkStatus> by NetworkStatusInstanceFactory,
        EntryHolder<Strict> by NetworkStatusEntryHolder

    object Serializer : KSerializer<NetworkStatus> by instanceFactorySerializer(NetworkStatus)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<NetworkStatus, _>()
}
