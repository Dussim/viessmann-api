package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.FactoryResetInfoValue
import xyz.dussim.viessmann.feature.api.ListBusTypeValue
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceInformationValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEebusDeviceValue
import xyz.dussim.viessmann.feature.api.ListEebusServicePartnerValue
import xyz.dussim.viessmann.feature.api.ListElectricalEnergyMatrixValue
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.ListEnergyChargedDeviceValue
import xyz.dussim.viessmann.feature.api.ListFuelCellErrorValue
import xyz.dussim.viessmann.feature.api.ListLogBookEntryValue
import xyz.dussim.viessmann.feature.api.ListOperatingDataCellsDetailValue
import xyz.dussim.viessmann.feature.api.ListPowerBalanceEntryValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListSensorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListVentilationMessageValue
import xyz.dussim.viessmann.feature.api.ListWifiNetworkValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.LogsValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.ProductInfoValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.UnknownConstraints
import xyz.dussim.viessmann.feature.api.UnknownValue
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentTypeMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmRecord
import kotlin.reflect.KClass

private val CLASS_REGISTRY: List<KClass<*>> =
    listOf(
        StringValue::class,
        BooleanValue::class,
        DoubleValue::class,
        ListDoubleValue::class,
        ListStringValue::class,
        ListDeviceErrorValue::class,
        ListZigbeeDeviceStatusValue::class,
        ListRoomActorValue::class,
        ListDeviceValue::class,
        ObjectOtherRoomConfigurationValue::class,
        ScheduleValue::class,
        ListEmptyValue::class,
        UnknownValue::class,
        BooleanConstraints::class,
        NumberConstraints::class,
        StringConstraints::class,
        ScheduleConstraints::class,
        UnknownConstraints::class,
        Command::class,
        Nothing::class,
        ListBusTypeValue::class,
        EnergyMatrixValue::class,
        LogsValue::class,
        ListLogBookEntryValue::class,
        ProductInfoValue::class,
        FactoryResetInfoValue::class,
        ListEebusDeviceValue::class,
        ListEebusServicePartnerValue::class,
        ListElectricalEnergyMatrixValue::class,
        ListOperatingDataCellsDetailValue::class,
        ListDeviceInformationValue::class,
        ListEnergyChargedDeviceValue::class,
        ListSensorValue::class,
        ListPowerBalanceEntryValue::class,
        ListFuelCellErrorValue::class,
        ListWifiNetworkValue::class,
        ListVentilationMessageValue::class,
    )

@PublishedApi
internal val STRING_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(StringValue::class)

@PublishedApi
internal val BOOLEAN_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(BooleanValue::class)

@PublishedApi
internal val DOUBLE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(DoubleValue::class)

@PublishedApi
internal val LIST_DOUBLE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListDoubleValue::class)

@PublishedApi
internal val LIST_STRING_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListStringValue::class)

@PublishedApi
internal val LIST_DEVICE_ERROR_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListDeviceErrorValue::class)

@PublishedApi
internal val LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListZigbeeDeviceStatusValue::class)

@PublishedApi
internal val LIST_ROOM_ACTOR_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListRoomActorValue::class)

@PublishedApi
internal val LIST_DEVICE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListDeviceValue::class)

@PublishedApi
internal val OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ObjectOtherRoomConfigurationValue::class)

@PublishedApi
internal val SCHEDULE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ScheduleValue::class)

@PublishedApi
internal val LIST_EMPTY_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEmptyValue::class)

@PublishedApi
internal val UNKNOWN_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(UnknownValue::class)

@PublishedApi
internal val BOOLEAN_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(BooleanConstraints::class)

@PublishedApi
internal val NUMBER_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(NumberConstraints::class)

@PublishedApi
internal val STRING_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(StringConstraints::class)

@PublishedApi
internal val SCHEDULE_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ScheduleConstraints::class)

@PublishedApi
internal val UNKNOWN_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(UnknownConstraints::class)

@PublishedApi
internal val COMMAND_CLASS_INDEX = CLASS_REGISTRY.indexOf(Command::class)

@PublishedApi
internal val MISSING_COMPONENT_CLASS_INDEX = CLASS_REGISTRY.indexOf(Nothing::class)

@PublishedApi
internal val LIST_BUS_TYPE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListBusTypeValue::class)

@PublishedApi
internal val ENERGY_MATRIX_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(EnergyMatrixValue::class)

@PublishedApi
internal val LOGS_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(LogsValue::class)

@PublishedApi
internal val LIST_LOG_BOOK_ENTRY_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListLogBookEntryValue::class)

@PublishedApi
internal val PRODUCT_INFO_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ProductInfoValue::class)

@PublishedApi
internal val FACTORY_RESET_INFO_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(FactoryResetInfoValue::class)

@PublishedApi
internal val LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEebusDeviceValue::class)

@PublishedApi
internal val LIST_EEBUS_SERVICE_PARTNER_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEebusServicePartnerValue::class)

@PublishedApi
internal val LIST_ELECTRICAL_ENERGY_MATRIX_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListElectricalEnergyMatrixValue::class)

@PublishedApi
internal val LIST_OPERATING_DATA_CELLS_DETAIL_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListOperatingDataCellsDetailValue::class)

@PublishedApi
internal val LIST_DEVICE_INFORMATION_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListDeviceInformationValue::class)

@PublishedApi
internal val LIST_ENERGY_CHARGED_DEVICE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEnergyChargedDeviceValue::class)

@PublishedApi
internal val LIST_SENSOR_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListSensorValue::class)

@PublishedApi
internal val LIST_POWER_BALANCE_ENTRY_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListPowerBalanceEntryValue::class)

@PublishedApi
internal val LIST_FUEL_CELL_ERROR_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListFuelCellErrorValue::class)

@PublishedApi
internal val LIST_WIFI_NETWORK_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListWifiNetworkValue::class)

@PublishedApi
internal val LIST_VENTILATION_MESSAGE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListVentilationMessageValue::class)

private val SIZE = CLASS_REGISTRY.size

private val EXPECTED_ACTUAL_CLASSES by lazy {
    buildList(CLASS_REGISTRY.size * CLASS_REGISTRY.size) {
        for (i in CLASS_REGISTRY.indices) {
            for (j in CLASS_REGISTRY.indices) {
                add(CLASS_REGISTRY[i] to CLASS_REGISTRY[j])
            }
        }
    }
}

@JvmInline
value class ExpectedActualClass internal constructor(
    internal val indexIntoList: Int,
) {
    companion object {
        fun of(
            expected: Int,
            actual: Int,
        ) = ExpectedActualClass(expected * SIZE + actual)

        fun of(expected: Int) = ExpectedActualClass(expected * SIZE + MISSING_COMPONENT_CLASS_INDEX)
    }

    init {
        require(indexIntoList >= 0) { "Index into list must be non-negative, got $indexIntoList" }
        require(indexIntoList < SIZE * SIZE) { "Index into list must be less than ${SIZE * SIZE}, got $indexIntoList" }
    }

    val expectedClass: KClass<*> get() = EXPECTED_ACTUAL_CLASSES[indexIntoList].first

    val actualClass: KClass<*> get() = EXPECTED_ACTUAL_CLASSES[indexIntoList].second

    override fun toString(): String =
        if (actualClass == Nothing::class) {
            "expected '$expectedClass' but got nothing"
        } else {
            "expected '$expectedClass' but got actual '$actualClass'"
        }
}

// Cache common error types to avoid repeated allocations
@Suppress("NOTHING_TO_INLINE")
object PropertyValidationErrors {
    fun interface MismatchPropertiesGetter {
        operator fun invoke(actualIndex: Int): ValidationResult<ComponentTypeMismatch>
    }

    @PublishedApi
    internal val mismatches =
        Array(SIZE) { IntToObjectMap.of<ValidationResult<ComponentTypeMismatch>>() }

    inline fun getMismatchProperties(
        nameOfComponent: String,
        expectedIndex: Int,
    ): MismatchPropertiesGetter =
        { actualIndex ->
            val map = mismatches[expectedIndex]
            val value = map[actualIndex]
            if (value != null) {
                value
            } else {
                val value =
                    Invalid(
                        ComponentTypeMismatch(
                            nameOfComponent,
                            ExpectedActualClass.of(expectedIndex, actualIndex),
                        ),
                    )
                mismatches[expectedIndex] =
                    IntToObjectMap.of(
                        actualIndex,
                        value,
                        map,
                    )

                value
            }
        }
}

sealed interface ValidationError {
    @JvmRecord
    data class MissingComponent(
        val name: String,
        val expectedActualClass: ExpectedActualClass,
    ) : ValidationError {
        override fun toString(): String = "Missing component '$name': $expectedActualClass"
    }

    @JvmRecord
    data class ComponentTypeMismatch(
        val name: String,
        val expectedActualClass: ExpectedActualClass,
    ) : ValidationError {
        override fun toString(): String = "Component type mismatch '$name': $expectedActualClass"
    }

    @JvmRecord
    data class NumberOfParametersMismatch(
        val expected: Expected,
        val actual: Int,
    ) : ValidationError {
        data class Expected(
            val name: String,
            val expected: Int,
        )

        override fun toString(): String = "Number of parameters mismatch in component '${expected.name}': expected $expected, actual $actual"
    }
}
