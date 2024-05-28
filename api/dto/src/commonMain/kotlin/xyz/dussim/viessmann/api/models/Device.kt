package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.NetworkStatus
import xyz.dussim.viessmann.api.enums.SerialEditor

@Serializable
data class Device(
    val gatewaySerial: String,
    val id: String,
    val boilerSerial: String,
    val boilerSerialEditor: SerialEditor,
    val bmuSerial: String,
    val bmuSerialEditor: SerialEditor,
    val createdAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val editedAt: String,
    val modelId: String,
    val status: NetworkStatus,
    val deviceType: String,
    val roles: List<String>,
    val isBoilerSerialEditable: Boolean,
)

@Serializable
data class DeviceStrict(
    val gatewaySerial: String,
    val id: String,
    val boilerSerial: String,
    val boilerSerialEditor: SerialEditor.Strict,
    val bmuSerial: String,
    val bmuSerialEditor: SerialEditor.Strict,
    val createdAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val editedAt: String,
    val modelId: String,
    val status: NetworkStatus.Strict,
    val deviceType: String,
    val roles: List<String>,
    val isBoilerSerialEditable: Boolean,
)
