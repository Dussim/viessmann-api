package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

internal object LenientInstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("xyz.dussim.viessmann.feature.api.LenientInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val value = decoder.decodeString()
        return parseInstantOrAssumeUtc(value)
    }

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(value.toString())
    }

    private fun parseInstantOrAssumeUtc(value: String): Instant = Instant.parseOrNull(value) ?: Instant.parse("${value}Z")
}
