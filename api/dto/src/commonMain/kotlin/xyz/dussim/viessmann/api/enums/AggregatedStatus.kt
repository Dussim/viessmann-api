package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.AggregatedStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.AggregatedStatusInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the aggregated status of a `Gateway`.
 *
 * A gateway can have one of the following aggregated statuses:
 * * [Error]
 * * [Offline]
 * * [Maintenance]
 * * [WorksProperly]
 * * [RemoteDiagnosticSession]
 * * [NbIotConnected]
 * * [Unknown]
 * */
@Serializable(with = AggregatedStatus.Serializer::class)
sealed interface AggregatedStatus : ViessmannEnum {
    /**
     * Represents the strictly defined aggregated statuses.
     *
     * This sealed class encompasses all known and valid aggregated statuses,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known aggregated statuses.
     *
     * Subtypes of this class are the only valid instances of [AggregatedStatus],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : AggregatedStatus

    data object Error : Strict("Error")

    data object Offline : Strict("Offline")

    data object Maintenance : Strict("Maintenance")

    data object WorksProperly : Strict("WorksProperly")

    data object RemoteDiagnosticSession : Strict("RemoteDiagnosticSession")

    data object NbIotConnected : Strict("NbIotConnected")

    /**
     * Represents an unknown [Gender].
     *
     * This is a special value used to represent a gender that is not
     * known to the library. The [name] of this value is the gender provided by
     * the API.
     *
     * @param name the gender returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : AggregatedStatus,
        UnknownEnumValue

    companion object :
        InstanceFactory<AggregatedStatus> by AggregatedStatusInstanceFactory,
        EntryHolder<Strict> by AggregatedStatusEntryHolder

    object Serializer : KSerializer<AggregatedStatus> by instanceFactorySerializer(AggregatedStatus)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<AggregatedStatus, _>()
}
