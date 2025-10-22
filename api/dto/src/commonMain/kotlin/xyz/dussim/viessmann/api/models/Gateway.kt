package xyz.dussim.viessmann.api.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.GatewayType
import xyz.dussim.viessmann.api.enums.TargetRealm
import kotlin.time.Instant

@Serializable(with = GatewaySerializer::class)
interface Gateway {
    val serial: String
    val version: String
    val firmwareUpdateFailureCounter: Int
    val autoUpdate: Boolean
    val createdAt: Instant
    val producedAt: Instant
    val lastStatusChangedAt: String
    val aggregatedStatus: AggregatedStatus
    val targetRealm: TargetRealm
    val gatewayType: GatewayType
    val installationId: Long?
    val registeredAt: Instant?
    val description: String?
    val otaOngoing: Boolean

    @Serializable
    data class Impl(
        override val serial: String,
        override val version: String,
        override val firmwareUpdateFailureCounter: Int,
        override val autoUpdate: Boolean,
        override val createdAt: Instant,
        override val producedAt: Instant,
        override val lastStatusChangedAt: String,
        override val aggregatedStatus: AggregatedStatus,
        override val targetRealm: TargetRealm,
        override val gatewayType: GatewayType,
        override val installationId: Long?,
        override val registeredAt: Instant?,
        override val description: String?,
        override val otaOngoing: Boolean,
    ) : Gateway

    @Serializable
    data class Strict(
        override val serial: String,
        override val version: String,
        override val firmwareUpdateFailureCounter: Int,
        override val autoUpdate: Boolean,
        override val createdAt: Instant,
        override val producedAt: Instant,
        override val lastStatusChangedAt: String,
        override val aggregatedStatus: AggregatedStatus.Strict,
        override val targetRealm: TargetRealm.Strict,
        override val gatewayType: GatewayType.Strict,
        override val installationId: Long?,
        override val registeredAt: Instant?,
        override val description: String?,
        override val otaOngoing: Boolean,
    ) : Gateway
}

object GatewaySerializer : KSerializer<Gateway> {
    override val descriptor: SerialDescriptor = Gateway.Impl.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Gateway,
    ) {
        when (value) {
            is Gateway.Impl -> Gateway.Impl.serializer().serialize(encoder, value)
            is Gateway.Strict -> Gateway.Strict.serializer().serialize(encoder, value)
            else -> throw IllegalArgumentException("Unknown Gateway type")
        }
    }

    override fun deserialize(decoder: Decoder): Gateway = Gateway.Impl.serializer().deserialize(decoder)
}
