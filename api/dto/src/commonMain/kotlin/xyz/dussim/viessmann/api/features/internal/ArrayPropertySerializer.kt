package xyz.dussim.viessmann.api.features.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.DeviceErrorContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.DoubleArrayContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.RoomActorContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.StringArrayContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.ZigbeeDeviceStatusContent

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class ArrayPropertySerializer : KSerializer<ArrayProperty> {
    override val descriptor =
        buildSerialDescriptor(
            "array",
            SerialKind.CONTEXTUAL,
            ListSerializer(ArrayContent.serializer(NothingSerializer())).descriptor,
        ) {
            element("type", String.serializer().descriptor)
            element("value", ListSerializer(ArrayContent.serializer(NothingSerializer())).descriptor)
            element("unit", String.serializer().descriptor, isOptional = true)
        }

    override fun deserialize(decoder: Decoder): ArrayProperty =
        decoder.decodeStructure(descriptor) {
            ArrayProperty(
                value =
                    runCatching { decodeSerializableElement(descriptor, 1, ListSerializer(StringArrayContent.serializer())) }
                        .recoverCatching { decodeSerializableElement(descriptor, 1, ListSerializer(DoubleArrayContent.serializer())) }
                        .recoverCatching { decodeSerializableElement(descriptor, 1, ListSerializer(DeviceErrorContent.serializer())) }
                        .recoverCatching { decodeSerializableElement(descriptor, 1, ListSerializer(ZigbeeDeviceStatusContent.serializer())) }
                        .recoverCatching { decodeSerializableElement(descriptor, 1, ListSerializer(RoomActorContent.serializer())) }
                        .getOrThrow(),
                unit =
                    runCatching { decodeNullableSerializableElement(descriptor, 2, String.serializer()) }
                        .getOrNull(),
            )
        }

    override fun serialize(
        encoder: Encoder,
        value: ArrayProperty,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, "array")

            when (value.value.firstOrNull()) {
                is StringArrayContent ->
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(StringArrayContent.serializer()),
                        value.value.filterIsInstance<StringArrayContent>(),
                    )

                is DoubleArrayContent ->
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(DoubleArrayContent.serializer()),
                        value.value.filterIsInstance<DoubleArrayContent>(),
                    )

                is DeviceErrorContent ->
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(DeviceErrorContent.serializer()),
                        value.value.filterIsInstance<DeviceErrorContent>(),
                    )

                is ZigbeeDeviceStatusContent ->
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(ZigbeeDeviceStatusContent.serializer()),
                        value.value.filterIsInstance<ZigbeeDeviceStatusContent>(),
                    )

                is RoomActorContent ->
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(RoomActorContent.serializer()),
                        value.value.filterIsInstance<RoomActorContent>(),
                    )

                null -> encodeSerializableElement(descriptor, 1, ListSerializer(StringArrayContent.serializer()), emptyList())
            }

            if (value.unit != null) {
                encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.unit)
            }
        }
    }
}
