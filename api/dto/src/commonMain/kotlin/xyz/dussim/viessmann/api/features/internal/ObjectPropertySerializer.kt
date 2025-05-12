package xyz.dussim.viessmann.api.features.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ObjectProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ObjectProperty.ObjectContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ObjectProperty.ObjectContent.OtherRoomConfigurationContent

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class ObjectPropertySerializer : KSerializer<ObjectProperty> {
    override val descriptor =
        buildSerialDescriptor(
            "object",
            SerialKind.CONTEXTUAL,
            ObjectContent.serializer(NothingSerializer()).descriptor,
        ) {
            element("type", String.serializer().descriptor)
            element("value", ObjectContent.serializer(NothingSerializer()).descriptor)
            element("unit", String.serializer().descriptor, isOptional = true)
        }

    override fun deserialize(decoder: Decoder): ObjectProperty =
        decoder.decodeStructure(descriptor) {
            ObjectProperty(
                value =
                    runCatching { decodeSerializableElement(descriptor, 1, OtherRoomConfigurationContent.serializer()) }
                        .getOrThrow(),
                unit = null,
            )
        }

    override fun serialize(
        encoder: Encoder,
        value: ObjectProperty,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, "object")

            when (value.value) {
                is OtherRoomConfigurationContent ->
                    encodeSerializableElement(descriptor, 1, OtherRoomConfigurationContent.serializer(), value.value)
            }

            if (value.unit != null) {
                encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.unit)
            }
        }
    }
}
