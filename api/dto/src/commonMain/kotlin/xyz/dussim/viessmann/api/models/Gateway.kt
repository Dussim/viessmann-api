package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.GatewayType
import xyz.dussim.viessmann.api.enums.TargetRealm

@Serializable
data class Gateway(
    val serial: String,
    val version: String,
    val firmwareUpdateFailureCounter: Int,
    val autoUpdate: Boolean,
    val createdAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val producedAt: String,
    val lastStatusChangedAt: String,
    val aggregatedStatus: AggregatedStatus,
    val targetRealm: TargetRealm,
    val gatewayType: GatewayType,
    val installationId: Long,
    val registeredAt: String,
    val description: String,
    val otaOngoing: Boolean,
)

@Serializable
data class GatewayStrict(
    val serial: String,
    val version: String,
    val firmwareUpdateFailureCounter: Int,
    val autoUpdate: Boolean,
    val createdAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val producedAt: String,
    val lastStatusChangedAt: String,
    val aggregatedStatus: AggregatedStatus.Strict,
    val targetRealm: TargetRealm.Strict,
    val gatewayType: GatewayType.Strict,
    val installationId: Long,
    val registeredAt: String,
    val description: String,
    val otaOngoing: Boolean,
)
