package xyz.dussim.viessmann.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import xyz.dussim.viessmann.api.enums.ViessmannEnum
import kotlin.reflect.KClass
import kotlin.reflect.cast

internal class StrictInstanceFactorySerializer<Base : ViessmannEnum, Strict : Base>(
    private val baseSerializer: KSerializer<Base>,
    private val strictKClass: KClass<Strict>,
) : KSerializer<Strict> {
    override val descriptor: SerialDescriptor = baseSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Strict,
    ) {
        baseSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Strict {
        val value = baseSerializer.deserialize(decoder)
        return try {
            strictKClass.cast(value)
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Unknown value of '$value' cannot be deserialized as ${strictKClass.simpleName}", e)
        }
    }
}

internal inline fun <reified Base : ViessmannEnum, reified Strict : Base> strictInstanceFactorySerializer(): KSerializer<Strict> =
    StrictInstanceFactorySerializer(serializer<Base>(), Strict::class)
