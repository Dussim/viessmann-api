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
import xyz.dussim.viessmann.feature.api.validation.LIST_EEBUS_DEVICES_PAIRED_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EEBUS_SERVICE_PARTNER_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ELECTRICAL_ENERGY_MATRIX_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_EMPTY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ENERGY_CHARGED_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_FUEL_CELL_ERROR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_LOG_BOOK_ENTRY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ONBOARD_UPDATER_LAST_ERROR_CODE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_OPERATING_DATA_CELLS_DETAIL_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_POWER_BALANCE_ENTRY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ROOM_ACTOR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_SENSOR_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_SOLARLOG_DEVICES_PAIRED_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_SOLARLOG_DEVICE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_SYSTEM_MESSAGE_ENTRY_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_VENTILATION_MESSAGE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_WIFI_NETWORK_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.LOGS_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.NULLABLE_BOOLEAN_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.NULLABLE_DOUBLE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.NULLABLE_STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.PRODUCT_INFO_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.SCHEDULE_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.STRING_VALUE_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.TEST_RESULT_VALUE_CLASS_INDEX
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
internal const val CONSOLIDATOR_VALUE_LIST = "ConsolidatorValueList"
internal const val TEST_RESULT = "testResult"

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
data class NullableBooleanValue(
    override val element: Boolean?,
) : PropertyValue<Boolean?> {
    override val propertyValueClassIndex get() = NULLABLE_BOOLEAN_VALUE_CLASS_INDEX
}

@JvmRecord
data class NullableDoubleValue(
    override val element: Double?,
) : PropertyValue<Double?> {
    override val propertyValueClassIndex get() = NULLABLE_DOUBLE_VALUE_CLASS_INDEX
}

@JvmRecord
data class NullableStringValue(
    override val element: String?,
) : PropertyValue<String?> {
    override val propertyValueClassIndex get() = NULLABLE_STRING_VALUE_CLASS_INDEX
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
data class ListOnboardUpdaterLastErrorCodeValue(
    override val element: List<OnboardUpdaterLastErrorCode>,
) : PropertyValue<List<OnboardUpdaterLastErrorCode>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_ONBOARD_UPDATER_LAST_ERROR_CODE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListOnboardUpdaterLastErrorCodeValue(emptyList())
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
data class ListEebusDevicesPairedValue(
    override val element: List<EebusDevicesPaired>,
) : PropertyValue<List<EebusDevicesPaired>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_EEBUS_DEVICES_PAIRED_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListEebusDevicesPairedValue(emptyList())
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
data class ListSolarlogDeviceValue(
    override val element: List<SolarlogDevice>,
) : PropertyValue<List<SolarlogDevice>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_SOLARLOG_DEVICE_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListSolarlogDeviceValue(emptyList())
    }
}

@JvmRecord
data class ListSolarlogDevicesPairedValue(
    override val element: List<SolarlogDevicesPaired>,
) : PropertyValue<List<SolarlogDevicesPaired>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_SOLARLOG_DEVICES_PAIRED_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListSolarlogDevicesPairedValue(emptyList())
    }
}

@JvmRecord
data class TestResultValue(
    override val element: TestResult,
) : PropertyValue<TestResult> {
    override val propertyValueClassIndex get() = TEST_RESULT_VALUE_CLASS_INDEX
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

@JvmRecord
data class ListSystemMessageEntryValue(
    override val element: List<SystemMessageEntry>,
) : PropertyValue<List<SystemMessageEntry>>,
    ListPropertyValue {
    override val propertyValueClassIndex get() = LIST_SYSTEM_MESSAGE_ENTRY_VALUE_CLASS_INDEX

    companion object {
        val EMPTY = ListSystemMessageEntryValue(emptyList())
    }
}

data object ListEmptyValue : PropertyValue<List<Nothing>>, ListPropertyValue {
    override val element = emptyList<Nothing>()
    override val propertyValueClassIndex get() = LIST_EMPTY_VALUE_CLASS_INDEX
}

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
    val heatingCircuit: Int? = null, // this is not in documentation
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

/**
 * Shared bus/product entry shape. Bus topology entries provide device metadata, while product matrix entries provide
 * Viessmann product metadata. Fields present in only one variant are nullable.
 */
@JvmRecord
@Serializable
data class BusType(
    val busAddress: Int,
    val busType: String, // should be enum
    val deviceObjectProperty: String? = null, // should be enum
    val deviceFunction: String? = null, // should be enum
    val softwareVersion: String? = null,
    val hardwareVersion: String? = null,
    val etn: String? = null,
    val viessmannIdentificationNumber: String? = null,
    val productFamily: String? = null, // should be enum
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
data class OnboardUpdaterLastErrorCode(
    val deviceFamily: Int,
    val error: String,
    val subCode: String,
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
data class EebusDevicesPaired(
    val type: String,
    val busAddress: String,
)

@JvmRecord
@Serializable
data class EebusServicePartner(
    val type: String,
    val id: String,
    val ski: String,
)

/**
 * Canonical electrical energy matrix entries use tariff fields, but the backend currently also returns an alternate
 * shape with a plain `value`, `busType`, and `busAddress`. That alternate shape is probably a backend/API contract
 * error, but it is accepted here so `ems.power.instantaneous` can still be decoded.
 */
@JvmRecord
@Serializable
data class ElectricalEnergyMatrix(
    val ident: Int,
    val parent: Int,
    val type: String, // should be enum,
    val `class`: String, // should be enum,
    val tariff1: Tariff? = null,
    val tariff2: Tariff? = null,
    val value: Double? = null,
    val unit: String,
    val busType: String? = null,
    val busAddress: String? = null,
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
        val total: Tariff? = null,
        val value: Double? = null,
        val unit: String,
        val busType: String? = null,
        val busAddress: String? = null,
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

@JvmRecord
@Serializable
data class FuelCellError(
    @Serializable(with = LenientInstantSerializer::class)
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

@JvmRecord
@Serializable
data class SolarlogDevice(
    val index: String,
    val type: String,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
)

@JvmRecord
@Serializable
data class SolarlogDevicesPaired(
    val type: String,
    val index: String,
)

@JvmRecord
@Serializable
data class TestResult(
    val measuredValue: Measurement,
    val cutOffLimit: Measurement,
    val cutOffValue: Measurement,
    val cutOffTime: Measurement,
    val status: Status,
) {
    @JvmRecord
    @Serializable
    data class Measurement(
        val value: Double,
        val unit: String,
    )

    @JvmRecord
    @Serializable
    data class Status(
        val value: String,
    )
}

/**
 * Unified message shape for legacy and E3 cooling/ventilation messages. `status` and `count` are present only in the
 * legacy variant; `busType` and `busAddress` are present only in the E3 variant.
 */
@JvmRecord
@Serializable
data class VentilationMessage(
    val timestamp: Instant,
    val errorCode: String,
    val priority: String,
    val status: String? = null,
    val count: Double? = null,
    val busType: String? = null,
    val busAddress: Int? = null,
)

@JvmRecord
@Serializable
data class SystemMessageEntry(
    val code: String,
    val firstAppearanceTime: Instant,
    val firstGoneTime: Instant,
    val lastAppearanceTime: Instant,
    val lastGoneTime: Instant,
    val counter: Double,
    val busAddress: String,
    val busType: String,
    val controller: String,
    val active: Boolean,
    val dataTracing: String,
    val audiences: List<String>,
)

@JvmName("ofBoolean")
fun Property.Companion.of(boolean: Boolean): Property = Property(BOOLEAN, BooleanValue(boolean))

@JvmName("ofDouble")
fun Property.Companion.of(double: Double): Property = Property(NUMBER, DoubleValue(double))

@JvmName("ofNullableBoolean")
fun Property.Companion.ofNullable(boolean: Boolean?): Property = Property(BOOLEAN, NullableBooleanValue(boolean))

@JvmName("ofNullableDouble")
fun Property.Companion.ofNullable(double: Double?): Property = Property(NUMBER, NullableDoubleValue(double))

@JvmName("ofNullableString")
fun Property.Companion.ofNullable(string: String?): Property = Property(STRING, NullableStringValue(string))

@JvmName("ofString")
fun Property.Companion.of(string: String): Property = Property(STRING, StringValue(string))

private inline fun <T> List<T>.toProperty(
    type: String = ARRAY,
    value: (List<T>) -> PropertyValue<*>,
): Property = if (isEmpty()) Property.ofEmptyList() else Property(type, value(this))

@JvmName("ofStrings")
fun Property.Companion.of(strings: List<String>): Property = strings.toProperty(value = ::ListStringValue)

@JvmName("ofStrings")
fun Property.Companion.of(vararg string: String): Property = Property.of(string.toList())

@JvmName("ofDoubles")
fun Property.Companion.of(list: List<Double>): Property = list.toProperty(value = ::ListDoubleValue)

@JvmName("ofDoubles")
fun Property.Companion.of(vararg double: Double): Property = Property.of(double.toList())

@JvmName("ofDeviceErrors")
fun Property.Companion.of(list: List<DeviceError>): Property = list.toProperty(value = ::ListDeviceErrorValue)

@JvmName("ofDeviceErrors")
fun Property.Companion.of(vararg deviceError: DeviceError): Property = Property.of(deviceError.toList())

@JvmName("ofZigbeeDeviceStatuses")
fun Property.Companion.of(list: List<ZigbeeDeviceStatus>): Property = list.toProperty(value = ::ListZigbeeDeviceStatusValue)

@JvmName("ofRoomActors")
fun Property.Companion.of(list: List<RoomActor>): Property = list.toProperty(value = ::ListRoomActorValue)

@JvmName("ofRoomActors")
fun Property.Companion.of(vararg roomActor: RoomActor): Property = Property.of(roomActor.toList())

@JvmName("ofDevices")
fun Property.Companion.of(list: List<Device>): Property = list.toProperty(DEVICE_LIST, ::ListDeviceValue)

@JvmName("ofDevices")
fun Property.Companion.of(vararg device: Device): Property = Property.of(device.toList())

@JvmName("ofOtherRoomConfiguration")
fun Property.Companion.of(otherRoomConfiguration: OtherRoomConfiguration): Property = Property(OBJECT, ObjectOtherRoomConfigurationValue(otherRoomConfiguration))

@JvmName("ofSchedules")
fun Property.Companion.of(scheduleMap: Map<String, List<Schedule>>): Property = Property(SCHEDULE, ScheduleValue(scheduleMap))

@JvmName("ofSchedules")
fun Property.Companion.of(vararg schedule: Pair<String, List<Schedule>>): Property =
    if (schedule.isEmpty()) Property.ofEmptyList() else Property(SCHEDULE, ScheduleValue(schedule.toMap()))

@JvmName("ofEnergyMatrix")
fun Property.Companion.of(energyMatrix: EnergyMatrix): Property = Property(ENERGY_MATRIX, EnergyMatrixValue(energyMatrix))

@JvmName("ofSolarlogDevices")
fun Property.Companion.of(list: List<SolarlogDevice>): Property = list.toProperty(value = ::ListSolarlogDeviceValue)

@JvmName("ofSolarlogDevices")
fun Property.Companion.of(vararg device: SolarlogDevice): Property = Property.of(device.toList())

@JvmName("ofSolarlogDevicesPaired")
fun Property.Companion.of(list: List<SolarlogDevicesPaired>): Property = list.toProperty(value = ::ListSolarlogDevicesPairedValue)

@JvmName("ofSolarlogDevicesPaired")
fun Property.Companion.of(vararg device: SolarlogDevicesPaired): Property = Property.of(device.toList())

@JvmName("ofOnboardUpdaterLastErrorCodes")
fun Property.Companion.of(list: List<OnboardUpdaterLastErrorCode>): Property = list.toProperty(value = ::ListOnboardUpdaterLastErrorCodeValue)

@JvmName("ofOnboardUpdaterLastErrorCodes")
fun Property.Companion.of(vararg lastErrorCode: OnboardUpdaterLastErrorCode): Property = Property.of(lastErrorCode.toList())

@JvmName("ofTestResult")
fun Property.Companion.of(testResult: TestResult): Property = Property(TEST_RESULT, TestResultValue(testResult))

@JvmName("ofEebusDevicesPaired")
fun Property.Companion.of(list: List<EebusDevicesPaired>): Property = list.toProperty(value = ::ListEebusDevicesPairedValue)

@JvmName("ofEebusDevicesPaired")
fun Property.Companion.of(vararg device: EebusDevicesPaired): Property = Property.of(device.toList())

fun Property.Companion.ofEmptyList(): Property = Property(ARRAY, ListEmptyValue)
