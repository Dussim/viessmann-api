@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import xyz.dussim.viessmann.feature.api.validation.BOOLEAN_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.DOUBLE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DEVICE_ERROR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DOUBLE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EMPTY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ROOM_ACTOR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.SCHEDULE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.UNKNOWN_VALUE_CLASS_INDEX
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord
import kotlin.time.Instant

internal const val BOOLEAN = "boolean"
internal const val NUMBER = "number"
internal const val STRING = "string"
internal const val ARRAY = "array"
internal const val OBJECT = "object"
internal const val DEVICE_LIST = "DeviceList"
internal const val SCHEDULE = "Schedule"

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
    val element: T

    val propertyValueClassIndex: Int
}

sealed interface ListPropertyValue

@JvmRecord
data class UnknownValue(
    override val element: JsonElement,
) : PropertyValue<JsonElement> {
    override val propertyValueClassIndex get() = UNKNOWN_VALUE_CLASS_INDEX
}

@JvmRecord
data class BooleanValue(
    override val element: Boolean,
) : PropertyValue<Boolean> {
    override val propertyValueClassIndex get() = BOOLEAN_VALUE_CLASS_INDEX
}

@JvmRecord
data class DoubleValue(
    override val element: Double,
) : PropertyValue<Double> {
    override val propertyValueClassIndex get() = DOUBLE_VALUE_CLASS_INDEX
}

@JvmRecord
data class StringValue(
    override val element: String,
) : PropertyValue<String> {
    override val propertyValueClassIndex get() = STRING_VALUE_CLASS_INDEX
}

@JvmRecord
data class ListDoubleValue(
    override val element: List<Double>,
) : PropertyValue<List<Double>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_DOUBLE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListDoubleValue(emptyList())
    }
}

@JvmRecord
data class ListStringValue(
    override val element: List<String>,
) : PropertyValue<List<String>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_STRING_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListStringValue(emptyList())
    }
}

@JvmRecord
data class ListDeviceErrorValue(
    override val element: List<DeviceError>,
) : PropertyValue<List<DeviceError>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_DEVICE_ERROR_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListDeviceErrorValue(emptyList())
    }
}

@JvmRecord
data class ListZigbeeDeviceStatusValue(
    override val element: List<ZigbeeDeviceStatus>,
) : PropertyValue<List<ZigbeeDeviceStatus>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListZigbeeDeviceStatusValue(emptyList())
    }
}

@JvmRecord
data class ListRoomActorValue(
    override val element: List<RoomActor>,
) : PropertyValue<List<RoomActor>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_ROOM_ACTOR_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListRoomActorValue(emptyList())
    }
}

@JvmRecord
data class ListDeviceValue(
    override val element: List<Device>,
) : PropertyValue<List<Device>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_DEVICE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListDeviceValue(emptyList())
    }
}

@JvmRecord
data class ObjectOtherRoomConfigurationValue(
    override val element: OtherRoomConfiguration,
) : PropertyValue<OtherRoomConfiguration> {
    override val propertyValueClassIndex get() = OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX
}

@JvmRecord
data class ScheduleValue(
    override val element: Map<String, List<Schedule>>,
) : PropertyValue<Map<String, List<Schedule>>> {
    override val propertyValueClassIndex get() = SCHEDULE_VALUE_CLASS_INDEX
}

data object ListEmptyValue : PropertyValue<List<Nothing>>, ListPropertyValue {
    override val element = emptyList<Nothing>()
    override val propertyValueClassIndex get() = LIST_EMPTY_VALUE_CLASS_INDEX
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
