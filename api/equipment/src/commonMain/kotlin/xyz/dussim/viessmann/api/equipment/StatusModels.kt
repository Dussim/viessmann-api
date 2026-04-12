package xyz.dussim.viessmann.api.equipment

import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.GatewayState
import kotlin.time.Instant

@Serializable
data class GatewayStatus(
    val aggregatedStatus: AggregatedStatus,
    val lastChangedAt: Instant,
    val gatewayStatus: GatewayStatusDetail? = null,
    val bmuStatuses: List<DeviceStatus> = emptyList(),
    val boilerStatuses: List<DeviceStatus> = emptyList(),
)

@Serializable
data class GatewayStatusDetail(
    val value: GatewayState,
    val lastChangedAt: Instant,
    val isOnline: Boolean? = null,
    val onlineChangedAt: Instant? = null,
    val isUpdating: Boolean? = null,
    val updateChangedAt: Instant? = null,
    val isDiagnosticModeEnabled: Boolean? = null,
    val diagnosticModeChangedAt: Instant? = null,
    val hasError: Boolean? = null,
    val errorChangedAt: Instant? = null,
)

@Serializable
data class DeviceStatus(
    val deviceId: String? = null,
    val value: String,
    val lastChangedAt: Instant,
)

@Serializable
data class InstallationStatus(
    val aggregatedStatus: AggregatedStatus,
    val lastChangedAt: Instant,
    val gatewaysStatuses: List<GatewayStatus> = emptyList(),
)
