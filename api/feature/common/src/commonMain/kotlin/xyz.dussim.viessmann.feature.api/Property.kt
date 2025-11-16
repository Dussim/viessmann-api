@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord
import kotlin.reflect.KClass
import kotlin.time.Instant

private const val BOOLEAN = "boolean"
private const val NUMBER = "number"
private const val STRING = "string"
private const val ARRAY = "array"
private const val OBJECT = "object"
private const val DEVICE_LIST = "DeviceList"
private const val SCHEDULE = "Schedule"

@JvmRecord
@ConsistentCopyVisibility
@Serializable(PropertySerializer::class)
data class Property internal constructor(
    val type: String,
    val value: PropertyValue<*>,
    val unit: String? = null,
) {
    companion object;

    override fun toString(): String = "Property(type='$type', value=${value.element}, unit=$unit)"
}

sealed interface PropertyValue<T> {
    companion object {
        @JvmField
        val stringValueClass = StringValue::class

        @JvmField
        val doubleValueClass = DoubleValue::class

        @JvmField
        val listDoubleValueClass = ListDoubleValue::class

        @JvmField
        val objectValueClass = ObjectOtherRoomConfigurationValue::class

        @JvmField
        val scheduleValueClass = ScheduleValue::class

        @JvmField
        val listEmptyValueClass = ListEmptyValue::class

        @JvmField
        val unknownValueClass = UnknownValue::class

        @JvmField
        val listDeviceErrorValueClass = ListDeviceErrorValue::class

        @JvmField
        val listZigbeeDeviceStatusValueClass = ListZigbeeDeviceStatusValue::class

        @JvmField
        val listRoomActorValueClass = ListRoomActorValue::class

        @JvmField
        val listDeviceValueClass = ListDeviceValue::class

        @JvmField
        val booleanValueClass = BooleanValue::class

        @JvmField
        val listStringValueClass = ListStringValue::class
    }

    val element: T
}

inline val PropertyValue<*>.propertyValueClass: KClass<out PropertyValue<*>>
    get() =
        when (this) {
            is BooleanValue -> PropertyValue.booleanValueClass
            is DoubleValue -> PropertyValue.doubleValueClass
            is ListDeviceErrorValue -> PropertyValue.listDeviceErrorValueClass
            is ListDeviceValue -> PropertyValue.listDeviceValueClass
            is ListDoubleValue -> PropertyValue.listDoubleValueClass
            is ListRoomActorValue -> PropertyValue.listRoomActorValueClass
            is ListStringValue -> PropertyValue.listStringValueClass
            is ListZigbeeDeviceStatusValue -> PropertyValue.listZigbeeDeviceStatusValueClass
            is ObjectOtherRoomConfigurationValue -> PropertyValue.objectValueClass
            is ScheduleValue -> PropertyValue.scheduleValueClass
            is StringValue -> PropertyValue.stringValueClass
            is UnknownValue -> PropertyValue.unknownValueClass
            ListEmptyValue -> PropertyValue.listEmptyValueClass
        }

sealed interface ListPropertyValue

@JvmInline
value class UnknownValue(
    override val element: JsonElement,
) : PropertyValue<JsonElement>

@JvmInline
value class BooleanValue(
    override val element: Boolean,
) : PropertyValue<Boolean>

@JvmInline
value class DoubleValue(
    override val element: Double,
) : PropertyValue<Double>

@JvmInline
value class StringValue(
    override val element: String,
) : PropertyValue<String>

@JvmInline
value class ListDoubleValue(
    override val element: List<Double>,
) : PropertyValue<List<Double>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListDoubleValue(emptyList())
    }
}

@JvmInline
value class ListStringValue(
    override val element: List<String>,
) : PropertyValue<List<String>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListStringValue(emptyList())
    }
}

@JvmInline
value class ListDeviceErrorValue(
    override val element: List<DeviceError>,
) : PropertyValue<List<DeviceError>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListDeviceErrorValue(emptyList())
    }
}

@JvmInline
value class ListZigbeeDeviceStatusValue(
    override val element: List<ZigbeeDeviceStatus>,
) : PropertyValue<List<ZigbeeDeviceStatus>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListZigbeeDeviceStatusValue(emptyList())
    }
}

@JvmInline
value class ListRoomActorValue(
    override val element: List<RoomActor>,
) : PropertyValue<List<RoomActor>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListRoomActorValue(emptyList())
    }
}

@JvmInline
value class ListDeviceValue(
    override val element: List<Device>,
) : PropertyValue<List<Device>>,
    ListPropertyValue {
    companion object {
        val EMPTY = ListDeviceValue(emptyList())
    }
}

@JvmInline
value class ObjectOtherRoomConfigurationValue(
    override val element: OtherRoomConfiguration,
) : PropertyValue<OtherRoomConfiguration>

@JvmInline
value class ScheduleValue(
    override val element: Map<String, List<Schedule>>,
) : PropertyValue<Map<String, List<Schedule>>>

data object ListEmptyValue : PropertyValue<List<Nothing>>, ListPropertyValue {
    override val element = emptyList<Nothing>()
}

@OptIn(kotlin.time.ExperimentalTime::class)
@JvmRecord
@Serializable
data class DeviceError(
    val errorCode: String,
    val timestamp: Instant,
    val accessLevel: String,
    val priority: String,
    val audiences: List<String>,
)

@JvmRecord
@Serializable
data class ZigbeeDeviceStatus(
    val device: String,
    val value: String,
)

@JvmRecord
@Serializable
data class RoomActor(
    val deviceId: String,
    val heatingCircuit: Int,
)

@JvmRecord
@Serializable
data class OtherRoomConfiguration(
    val hydraulicBalance: Boolean,
    val heatupSpeed: String,
    val trvAlgoActive: Boolean,
    val openPointDetection: Boolean,
    val virtualClimateSensor: Boolean,
    val etrvSync: Boolean,
    val useTrvOpenWindow: Boolean,
    val heatOnTime: Boolean,
)

@JvmRecord
@Serializable
data class Device(
    val id: String,
    val fingerprint: String,
    val modelId: String,
    val modelVersion: String,
    val type: String,
    val name: String,
    val roles: List<String>,
    val status: String,
)

@JvmRecord
@Serializable
data class Schedule(
    val start: String,
    val end: String,
    val mode: String,
    val position: Int,
    val active: Boolean = true,
)

@JvmName("ofBoolean")
fun Property.Companion.ofBoolean(boolean: Boolean): Property = Property(BOOLEAN, BooleanValue(boolean))

@JvmName("ofDouble")
fun Property.Companion.ofDouble(
    double: Double,
    unit: String? = null,
): Property = Property(NUMBER, DoubleValue(double))

@JvmName("ofString")
fun Property.Companion.of(string: String): Property = Property(STRING, StringValue(string))

@JvmName("ofStrings")
fun Property.Companion.of(strings: List<String>): Property = Property(ARRAY, ListStringValue(strings))

@JvmName("ofStrings")
fun Property.Companion.of(vararg string: String): Property = Property(ARRAY, ListStringValue(string.toList()))

@JvmName("ofDoubles")
fun Property.Companion.of(list: List<Double>): Property = Property(ARRAY, ListDoubleValue(list))

@JvmName("ofDoubles")
fun Property.Companion.of(vararg double: Double): Property = Property(ARRAY, ListDoubleValue(double.toList()))

@JvmName("ofDeviceErrors")
fun Property.Companion.of(list: List<DeviceError>): Property = Property(ARRAY, ListDeviceErrorValue(list))

@JvmName("ofDeviceErrors")
fun Property.Companion.of(vararg deviceError: DeviceError): Property = Property(ARRAY, ListDeviceErrorValue(deviceError.toList()))

@JvmName("ofZigbeeDeviceStatuses")
fun Property.Companion.of(list: List<ZigbeeDeviceStatus>): Property = Property(ARRAY, ListZigbeeDeviceStatusValue(list))

@JvmName("ofRoomActors")
fun Property.Companion.of(list: List<RoomActor>): Property = Property(ARRAY, ListRoomActorValue(list))

@JvmName("ofRoomActors")
fun Property.Companion.of(vararg roomActor: RoomActor): Property = Property(ARRAY, ListRoomActorValue(roomActor.toList()))

@JvmName("ofDevices")
fun Property.Companion.of(list: List<Device>): Property = Property(DEVICE_LIST, ListDeviceValue(list))

@JvmName("ofDevices")
fun Property.Companion.of(vararg device: Device): Property = Property(DEVICE_LIST, ListDeviceValue(device.toList()))

@JvmName("ofOtherRoomConfiguration")
fun Property.Companion.of(otherRoomConfiguration: OtherRoomConfiguration): Property = Property(OBJECT, ObjectOtherRoomConfigurationValue(otherRoomConfiguration))

@JvmName("ofSchedules")
fun Property.Companion.of(scheduleMap: Map<String, List<Schedule>>): Property = Property(SCHEDULE, ScheduleValue(scheduleMap))

@JvmName("ofSchedules")
fun Property.Companion.of(vararg schedule: Pair<String, List<Schedule>>): Property = Property(SCHEDULE, ScheduleValue(schedule.toMap()))

@OptIn(ExperimentalSerializationApi::class)
internal data object PropertySerializer : KSerializer<Property> {
    override val descriptor =
        buildClassSerialDescriptor("xyz.dussim.viessmann.feature.api.Property") {
            element<String>("type")
            element<JsonElement>("value")
            element<String?>("unit")
        }

    override fun serialize(
        encoder: Encoder,
        value: Property,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.type)
            when (val propertyValue = value.value) {
                is BooleanValue -> encodeBooleanElement(descriptor, 1, propertyValue.element)
                is DoubleValue -> encodeDoubleElement(descriptor, 1, propertyValue.element)
                is StringValue -> encodeStringElement(descriptor, 1, propertyValue.element)
                is ListDoubleValue -> encodeSerializableElement(descriptor, 1, ListSerializer(Double.serializer()), propertyValue.element)
                is ListStringValue -> encodeSerializableElement(descriptor, 1, ListSerializer(String.serializer()), propertyValue.element)
                is UnknownValue -> encodeSerializableElement(descriptor, 1, JsonElement.serializer(), propertyValue.element)
                is ListDeviceErrorValue -> encodeSerializableElement(descriptor, 1, ListSerializer(DeviceError.serializer()), propertyValue.element)
                is ListRoomActorValue -> encodeSerializableElement(descriptor, 1, ListSerializer(RoomActor.serializer()), propertyValue.element)
                is ListZigbeeDeviceStatusValue -> encodeSerializableElement(descriptor, 1, ListSerializer(ZigbeeDeviceStatus.serializer()), propertyValue.element)
                is ObjectOtherRoomConfigurationValue -> encodeSerializableElement(descriptor, 1, OtherRoomConfiguration.serializer(), propertyValue.element)
                is ListDeviceValue -> encodeSerializableElement(descriptor, 1, ListSerializer(Device.serializer()), propertyValue.element)
                is ScheduleValue -> encodeSerializableElement(descriptor, 1, MapSerializer(String.serializer(), ListSerializer(Schedule.serializer())), propertyValue.element)
                ListEmptyValue -> encodeSerializableElement(descriptor, 1, ListSerializer(NothingSerializer()), emptyList())
            }
            encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.unit)
        }
    }

    override fun deserialize(decoder: Decoder): Property {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement().jsonObject
        val type = element.getValue("type").jsonPrimitive.content
        val value = element.getValue("value")
        val unit = element["unit"]?.jsonPrimitive?.content

        return Property(
            type = type,
            value =
                when (type) {
                    BOOLEAN -> {
                        BooleanValue(value.jsonPrimitive.boolean)
                    }

                    NUMBER -> {
                        DoubleValue(value.jsonPrimitive.double)
                    }

                    STRING -> {
                        StringValue(value.jsonPrimitive.content)
                    }

                    ARRAY -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(array, String.serializer(), ::ListStringValue)
                            ?: decoder.decodeOrNull(array, Double.serializer(), ::ListDoubleValue)
                            ?: decoder.decodeOrNull(array, DeviceError.serializer(), ::ListDeviceErrorValue)
                            ?: decoder.decodeOrNull(array, ZigbeeDeviceStatus.serializer(), ::ListZigbeeDeviceStatusValue)
                            ?: decoder.decodeOrNull(array, RoomActor.serializer(), ::ListRoomActorValue)
                            ?: UnknownValue(value)
                    }

                    OBJECT -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(obj, OtherRoomConfiguration.serializer(), ::ObjectOtherRoomConfigurationValue)
                            ?: UnknownValue(value)
                    }

                    DEVICE_LIST -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(array, Device.serializer(), ::ListDeviceValue)
                            ?: UnknownValue(value)
                    }

                    SCHEDULE -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(obj, MapSerializer(String.serializer(), ListSerializer(Schedule.serializer())), ::ScheduleValue)
                            ?: UnknownValue(value)
                    }

                    else -> {
                        UnknownValue(value)
                    }
                },
            unit = unit,
        )
    }

    private fun <T> JsonDecoder.decodeOrNull(
        array: JsonArray?,
        serializer: KSerializer<T>,
        provider: (List<T>) -> PropertyValue<*>,
    ): PropertyValue<*>? {
        if (array == null) {
            return null
        }

        if (array.isEmpty()) {
            return ListEmptyValue
        }

        return try {
            provider(json.decodeFromJsonElement(ListSerializer(serializer), array))
        } catch (_: Exception) {
            return null
        }
    }

    private fun <T> JsonDecoder.decodeOrNull(
        obj: JsonObject?,
        serializer: KSerializer<T>,
        provider: (T) -> PropertyValue<*>,
    ): PropertyValue<*>? {
        if (obj == null) {
            return null
        }

        return try {
            provider(json.decodeFromJsonElement(serializer, obj))
        } catch (_: Exception) {
            return null
        }
    }
}
