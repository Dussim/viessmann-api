package xyz.dussim.viessmann.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.api.enums.ViessmannEnum

internal class InstanceFactorySerializer<T : ViessmannEnum>(
    private val factory: InstanceFactory<T>,
    serialName: String,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T = factory.valueOf(decoder.decodeString())

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        encoder.encodeString(value.name)
    }
}

internal inline fun <reified T : ViessmannEnum> instanceFactorySerializer(factory: InstanceFactory<T>): KSerializer<T> =
    InstanceFactorySerializer(factory, serialName = T::class.simpleName.toString())
