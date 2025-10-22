package xyz.dussim.viessmann.api.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.api.enums.NetworkStatus
import xyz.dussim.viessmann.api.enums.SerialEditor
import kotlin.time.Instant

@Serializable(with = DeviceSerializer::class)
interface Device {
    val gatewaySerial: String
    val id: String
    val boilerSerial: String? // FIXME documentation says that this can NOT be null but it is for some devices :/
    val boilerSerialEditor: SerialEditor? // FIXME documentation says that this can NOT be null but it is for some devices :/
    val bmuSerial: String? // FIXME documentation says that this can NOT be null but it is for some devices :/
    val bmuSerialEditor: SerialEditor? // FIXME documentation says that this can NOT be null but it is for some devices :/
    val createdAt: Instant
    val editedAt: String
    val modelId: String
    val status: NetworkStatus
    val deviceType: String
    val roles: List<String>
    val isBoilerSerialEditable: Boolean

    @Serializable
    data class Impl(
        override val gatewaySerial: String,
        override val id: String,
        override val boilerSerial: String?,
        override val boilerSerialEditor: SerialEditor?,
        override val bmuSerial: String?,
        override val bmuSerialEditor: SerialEditor?,
        override val createdAt: Instant,
        override val editedAt: String,
        override val modelId: String,
        override val status: NetworkStatus,
        override val deviceType: String,
        override val roles: List<String>,
        override val isBoilerSerialEditable: Boolean,
    ) : Device

    @Serializable
    data class Strict(
        override val gatewaySerial: String,
        override val id: String,
        override val boilerSerial: String?,
        override val boilerSerialEditor: SerialEditor.Strict?,
        override val bmuSerial: String?,
        override val bmuSerialEditor: SerialEditor.Strict?,
        override val createdAt: Instant,
        override val editedAt: String,
        override val modelId: String,
        override val status: NetworkStatus.Strict,
        override val deviceType: String,
        override val roles: List<String>,
        override val isBoilerSerialEditable: Boolean,
    ) : Device
}

object DeviceSerializer : KSerializer<Device> {
    override val descriptor: SerialDescriptor = Device.Impl.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Device,
    ) {
        when (value) {
            is Device.Impl -> Device.Impl.serializer().serialize(encoder, value)
            is Device.Strict -> Device.Strict.serializer().serialize(encoder, value)
            else -> throw IllegalArgumentException("Unknown Device type")
        }
    }

    override fun deserialize(decoder: Decoder): Device = Device.Impl.serializer().deserialize(decoder)
}
