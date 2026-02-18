@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import xyz.dussim.viessmann.feature.api.validation.BOOLEAN_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.DOUBLE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ENERGY_MATRIX_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.FACTORY_RESET_INFO_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_BUS_TYPE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DEVICE_ERROR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DEVICE_INFORMATION_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_DOUBLE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EEBUS_SERVICE_PARTNER_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ELECTRICAL_ENERGY_MATRIX_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EMPTY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ENERGY_CHARGED_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_FUEL_CELL_ERROR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_LOG_BOOK_ENTRY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_OPERATING_DATA_CELLS_DETAIL_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_POWER_BALANCE_ENTRY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ROOM_ACTOR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_SENSOR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_VENTILATION_MESSAGE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_WIFI_NETWORK_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LOGS_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.PRODUCT_INFO_VALUE_CLASS_INDEX
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
internal const val ENERGY_MATRIX = "EnergyMatrix"
internal const val ELECTRICAL_ENERGY_MATRIX = "ElectricalEnergyMatrix"
internal const val CO2_VALUES = "legendCo2Values"
internal const val AIR_QUALITY_VALUES = "legendAqiValues"

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

@JvmRecord
data class ListBusTypeValue(
    override val element: List<BusType>,
) : PropertyValue<List<BusType>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_BUS_TYPE_CLASS_INDEX

    companion object {
        val EMPTY = ListBusTypeValue(emptyList())
    }
}

@JvmRecord
data class EnergyMatrixValue(
    override val element: EnergyMatrix,
) : PropertyValue<EnergyMatrix> {
    override val propertyValueClassIndex get() = ENERGY_MATRIX_VALUE_CLASS_INDEX
}

@JvmRecord
data class LogsValue(
    override val element: Logs,
) : PropertyValue<Logs> {
    override val propertyValueClassIndex get() = LOGS_VALUE_CLASS_INDEX
}

@JvmRecord
data class ListLogBookEntryValue(
    override val element: List<LogBookEntry>,
) : PropertyValue<List<LogBookEntry>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_LOG_BOOK_ENTRY_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListLogBookEntryValue(emptyList())
    }
}

@JvmRecord
data class ProductInfoValue(
    override val element: ProductInfo,
) : PropertyValue<ProductInfo> {
    override val propertyValueClassIndex get() = PRODUCT_INFO_VALUE_CLASS_INDEX
}

@JvmRecord
data class FactoryResetInfoValue(
    override val element: FactoryResetInfo,
) : PropertyValue<FactoryResetInfo> {
    override val propertyValueClassIndex get() = FACTORY_RESET_INFO_VALUE_CLASS_INDEX
}

@JvmRecord
data class ListEebusDeviceValue(
    override val element: List<EebusDevice>,
) : PropertyValue<List<EebusDevice>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListEebusDeviceValue(emptyList())
    }
}

@JvmRecord
data class ListEebusServicePartnerValue(
    override val element: List<EebusServicePartner>,
) : PropertyValue<List<EebusServicePartner>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_EEBUS_SERVICE_PARTNER_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListEebusServicePartnerValue(emptyList())
    }
}

@JvmRecord
data class ListElectricalEnergyMatrixValue(
    override val element: List<ElectricalEnergyMatrix>,
) : PropertyValue<List<ElectricalEnergyMatrix>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_ELECTRICAL_ENERGY_MATRIX_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListElectricalEnergyMatrixValue(emptyList())
    }
}

@JvmRecord
data class ListOperatingDataCellsDetailValue(
    override val element: List<OperatingDataCellsDetail>,
) : PropertyValue<List<OperatingDataCellsDetail>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_OPERATING_DATA_CELLS_DETAIL_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListOperatingDataCellsDetailValue(emptyList())
    }
}

@JvmRecord
data class ListEnergyChargedDeviceValue(
    override val element: List<EnergyChargedDevice>,
) : PropertyValue<List<EnergyChargedDevice>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_ENERGY_CHARGED_DEVICE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListEnergyChargedDeviceValue(emptyList())
    }
}

@JvmRecord
data class ListDeviceInformationValue(
    override val element: List<DeviceInformation>,
) : PropertyValue<List<DeviceInformation>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_DEVICE_INFORMATION_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListDeviceInformationValue(emptyList())
    }
}

@JvmRecord
data class ListSensorValue(
    override val element: List<SensorValue>,
) : PropertyValue<List<SensorValue>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_SENSOR_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListSensorValue(emptyList())
    }
}

@JvmRecord
data class ListPowerBalanceEntryValue(
    override val element: List<PowerBalanceEntry>,
) : PropertyValue<List<PowerBalanceEntry>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_POWER_BALANCE_ENTRY_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListPowerBalanceEntryValue(emptyList())
    }
}

@JvmRecord
data class ListFuelCellErrorValue(
    override val element: List<FuelCellError>,
) : PropertyValue<List<FuelCellError>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_FUEL_CELL_ERROR_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListFuelCellErrorValue(emptyList())
    }
}

@JvmRecord
data class ListWifiNetworkValue(
    override val element: List<WifiNetwork>,
) : PropertyValue<List<WifiNetwork>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_WIFI_NETWORK_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListWifiNetworkValue(emptyList())
    }
}

@JvmRecord
data class ListVentilationMessageValue(
    override val element: List<VentilationMessage>,
) : PropertyValue<List<VentilationMessage>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_VENTILATION_MESSAGE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListVentilationMessageValue(emptyList())
    }
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
    val busAddress: String? = null, // ?
    val busType: String? = null, // ?
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
    val heatingCircuit: Int, // this is not in documentation
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

@JvmRecord
@Serializable
data class BusType(
    val busAddress: Int,
    val busType: String, // should be enum
    val deviceObjectProperty: String, // should be enum
    val deviceFunction: String, // should be enum
    val softwareVersion: String,
    val hardwareVersion: String,
    val etn: String,
)

@JvmRecord
@Serializable
data class EnergyMatrix(
    val producer: List<EnergyMatrixEntry>,
    val consumer: List<EnergyMatrixEntry>,
    val storage: List<EnergyMatrixEntry>,
    val inverter: List<EnergyMatrixEntry>,
)

@JvmRecord
@Serializable
data class EnergyMatrixEntry(
    val ident: Int,
    val parent: Int,
    val type: String, // should be enum
    val busType: String,
    val busAddress: String,
)

@JvmRecord
@Serializable
data class Logs(
    val logs: Optolink,
    val default: String,
)

@JvmRecord
@Serializable
data class Optolink(
    val optolink: String,
)

@JvmRecord
@Serializable
data class LogBookEntry(
    val timestamp: Instant,
    val actor: String, // should be enum
    val status: Int,
    val event: String, // should be enum
    val circuit: String, // should be enum
    val stateMachine: String, // should be enum
    val additionalInfo: Int,
)

@JvmRecord
@Serializable
data class ProductInfo(
    val busType: String, // should be enum
    val busAddress: Int,
    val viessmannIdentificationNumber: String,
    val productFamily: String, // should be enum
)

@JvmRecord
@Serializable
data class FactoryResetInfo(
    val day: Int,
    val month: Int,
    val year: Int,
)

@JvmRecord
@Serializable
data class EebusDevice(
    val type: String,
    val brand: String,
    val model: String,
    val id: String,
    val ski: String,
)

@JvmRecord
@Serializable
data class EebusServicePartner(
    val type: String,
    val id: String,
    val ski: String,
)

@JvmRecord
@Serializable
data class ElectricalEnergyMatrix(
    val ident: Int,
    val parent: Int,
    val type: String, // should be enum,
    val `class`: String, // should be enum,
    val tariff1: Tariff,
    val tariff2: Tariff,
    val unit: String,
    val children: List<Child> = emptyList(),
) {
    @JvmRecord
    @Serializable
    data class Tariff(
        val delivered: Double,
        val received: Double,
    )

    @JvmRecord
    @Serializable
    data class Child(
        val ident: Int,
        val parent: Int,
        val type: String, // should be enum,
        val `class`: String, // should be enum,
        val total: Tariff,
        val unit: String,
        val children: List<Child> = emptyList(),
    )
}

@JvmRecord
@Serializable
data class OperatingDataCellsDetail(
    val voltageValue: Property,
    val cellBalance: Property,
    val functionStatus: Property,
    val safetyStatus: Property,
)

@JvmRecord
@Serializable
data class EnergyChargedDevice(
    val id: String,
    val role: String,
    val status: String,
    val memberId: Int,
    val value: Double? = null,
    val unit: String? = null,
)

@JvmRecord
@Serializable
data class DeviceInformation(
    val deviceObjectProperty: String? = null, // should be enum
    val deviceFunction: String? = null, // should be enum
    val softwareVersion: String? = null,
    val hardwareVersion: String? = null,
    val etn: String? = null,
)

@JvmRecord
@Serializable
data class SensorValue(
    val level: String,
    val lowerBorder: Double,
    val upperBorder: Double,
)

@JvmRecord
@Serializable
data class PowerBalanceEntry(
    val value: Double,
    val unit: String,
    val type: String,
)

@OptIn(kotlin.time.ExperimentalTime::class)
@JvmRecord
@Serializable
data class FuelCellError(
    val timestamp: Instant,
    val errorCode: String,
    val accessLevel: String,
    val priority: String,
)

@JvmRecord
@Serializable
data class WifiNetwork(
    val ssid: String,
    val signalStrength: Double,
)

@OptIn(kotlin.time.ExperimentalTime::class)
@JvmRecord
@Serializable
data class VentilationMessage(
    val timestamp: Instant,
    val errorCode: String,
    val status: String,
    val count: Double,
    val priority: String,
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
