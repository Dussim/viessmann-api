package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.ArrayBooleanConstraints
import xyz.dussim.viessmann.feature.api.ArrayConstraints
import xyz.dussim.viessmann.feature.api.ArrayEmptyConstraints
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.ArrayObjectConstraints
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.ArrayUnknownConstraints
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EnergyMatrixConstraints
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.FactoryResetInfoValue
import xyz.dussim.viessmann.feature.api.ListBusTypeValue
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceInformationValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEebusDeviceValue
import xyz.dussim.viessmann.feature.api.ListEebusDevicesPairedValue
import xyz.dussim.viessmann.feature.api.ListEebusServicePartnerValue
import xyz.dussim.viessmann.feature.api.ListElectricalEnergyMatrixValue
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.ListEnergyChargedDeviceValue
import xyz.dussim.viessmann.feature.api.ListFuelCellErrorValue
import xyz.dussim.viessmann.feature.api.ListLogBookEntryValue
import xyz.dussim.viessmann.feature.api.ListOnboardUpdaterLastErrorCodeValue
import xyz.dussim.viessmann.feature.api.ListOperatingDataCellsDetailValue
import xyz.dussim.viessmann.feature.api.ListPowerBalanceEntryValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListSensorValue
import xyz.dussim.viessmann.feature.api.ListSolarlogDeviceValue
import xyz.dussim.viessmann.feature.api.ListSolarlogDevicesPairedValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListSystemMessageEntryValue
import xyz.dussim.viessmann.feature.api.ListVentilationMessageValue
import xyz.dussim.viessmann.feature.api.ListWifiNetworkValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.LogsValue
import xyz.dussim.viessmann.feature.api.NullableBooleanValue
import xyz.dussim.viessmann.feature.api.NullableDoubleValue
import xyz.dussim.viessmann.feature.api.NullableStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectConstraints
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.ProductInfoValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.TestResultValue
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
        NullableStringValue::class,
        NullableBooleanValue::class,
        NullableDoubleValue::class,
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
        ArrayConstraints::class,
        ArrayEmptyConstraints::class,
        ArrayNumberConstraints::class,
        ArrayStringConstraints::class,
        ArrayBooleanConstraints::class,
        ArrayObjectConstraints::class,
        ArrayUnknownConstraints::class,
        ObjectConstraints::class,
        ScheduleConstraints::class,
        EnergyMatrixConstraints::class,
        UnknownConstraints::class,
        Command::class,
        Nothing::class,
        ListBusTypeValue::class,
        EnergyMatrixValue::class,
        LogsValue::class,
        ListLogBookEntryValue::class,
        ListOnboardUpdaterLastErrorCodeValue::class,
        ProductInfoValue::class,
        FactoryResetInfoValue::class,
        ListEebusDeviceValue::class,
        ListEebusDevicesPairedValue::class,
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
        ListSystemMessageEntryValue::class,
        TestResultValue::class,
        ListSolarlogDeviceValue::class,
        ListSolarlogDevicesPairedValue::class,
    )

@PublishedApi
internal val STRING_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(StringValue::class)

@PublishedApi
internal val BOOLEAN_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(BooleanValue::class)

@PublishedApi
internal val DOUBLE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(DoubleValue::class)

@PublishedApi
internal val NULLABLE_STRING_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(NullableStringValue::class)

@PublishedApi
internal val NULLABLE_BOOLEAN_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(NullableBooleanValue::class)

@PublishedApi
internal val NULLABLE_DOUBLE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(NullableDoubleValue::class)

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
internal val ARRAY_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayConstraints::class)

@PublishedApi
internal val ARRAY_EMPTY_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayEmptyConstraints::class)

@PublishedApi
internal val ARRAY_NUMBER_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayNumberConstraints::class)

@PublishedApi
internal val ARRAY_STRING_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayStringConstraints::class)

@PublishedApi
internal val ARRAY_BOOLEAN_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayBooleanConstraints::class)

@PublishedApi
internal val ARRAY_OBJECT_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayObjectConstraints::class)

@PublishedApi
internal val ARRAY_UNKNOWN_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ArrayUnknownConstraints::class)

@PublishedApi
internal val OBJECT_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ObjectConstraints::class)

@PublishedApi
internal val SCHEDULE_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(ScheduleConstraints::class)

@PublishedApi
internal val ENERGY_MATRIX_CONSTRAINTS_CLASS_INDEX = CLASS_REGISTRY.indexOf(EnergyMatrixConstraints::class)

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
internal val LIST_ONBOARD_UPDATER_LAST_ERROR_CODE_VALUE_CLASS_INDEX =
    CLASS_REGISTRY.indexOf(ListOnboardUpdaterLastErrorCodeValue::class)

@PublishedApi
internal val PRODUCT_INFO_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ProductInfoValue::class)

@PublishedApi
internal val FACTORY_RESET_INFO_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(FactoryResetInfoValue::class)

@PublishedApi
internal val LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEebusDeviceValue::class)

@PublishedApi
internal val LIST_EEBUS_DEVICES_PAIRED_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListEebusDevicesPairedValue::class)

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

@PublishedApi
internal val LIST_SYSTEM_MESSAGE_ENTRY_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListSystemMessageEntryValue::class)

@PublishedApi
internal val TEST_RESULT_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(TestResultValue::class)

@PublishedApi
internal val LIST_SOLARLOG_DEVICE_VALUE_CLASS_INDEX = CLASS_REGISTRY.indexOf(ListSolarlogDeviceValue::class)

@PublishedApi
internal val LIST_SOLARLOG_DEVICES_PAIRED_VALUE_CLASS_INDEX =
    CLASS_REGISTRY.indexOf(ListSolarlogDevicesPairedValue::class)

private val SIZE = CLASS_REGISTRY.size
internal val CLASS_REGISTRY_SIZE = SIZE

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

object PropertyValidationErrors {
    private val SIZE = CLASS_REGISTRY_SIZE
    private val TABLE_SIZE = SIZE * SIZE
    private val GETTER_OFFSET = TABLE_SIZE + 1

    class Getter(
        private val slots: Array<Any?>,
        private val expectedIndex: Int,
    ) {
        operator fun invoke(actualIndex: Int): ValidationResult<ComponentTypeMismatch> {
            val slot = expectedIndex * SIZE + actualIndex + 1

            @Suppress("UNCHECKED_CAST")
            val value = slots[slot] as ValidationResult<ComponentTypeMismatch>?

            if (value != null) {
                return value
            } else {
                val name = slots[0] as String
                val built = Invalid(ComponentTypeMismatch(name, ExpectedActualClass.of(expectedIndex, actualIndex)))
                slots[slot] = built
                return built
            }
        }
    }

    private val nameCache = StringKeyedCache<Array<Any?>>()

    fun getMismatchProperties(
        nameOfComponent: String,
        expectedIndex: Int,
    ): Getter {
        val slots =
            nameCache.getOrPut(nameOfComponent) { n ->
                arrayOfNulls<Any?>(GETTER_OFFSET + SIZE).also { it[0] = n }
            }
        val getterSlot = GETTER_OFFSET + expectedIndex
        (slots[getterSlot] as Getter?)?.let { return it }
        val getter = Getter(slots, expectedIndex)
        slots[getterSlot] = getter
        return getter
    }
}

internal class StringKeyedCache<V : Any> {
    private class Table<V : Any>(
        val names: Array<String?>,
        val values: Array<V?>,
        val size: Int,
    )

    private var table: Table<V> = Table(arrayOfNulls(16), arrayOfNulls<Any?>(16) as Array<V?>, 0)

    fun getOrPut(
        name: String,
        create: (String) -> V,
    ): V {
        val t = table
        find(t, name)?.let { return it }
        val value = create(name)
        table = insertInto(t, name, value)
        return value
    }

    private fun find(
        t: Table<V>,
        name: String,
    ): V? {
        val mask = t.names.size - 1
        var slot = (name.hashCode() xor (name.hashCode() ushr 16)) and mask
        while (true) {
            val n = t.names[slot] ?: return null
            if (n == name) return t.values[slot]
            slot = (slot + 1) and mask
        }
    }

    private fun insertInto(
        old: Table<V>,
        name: String,
        value: V,
    ): Table<V> {
        val grow = (old.size + 1) * 2 >= old.names.size
        val cap = if (grow) old.names.size * 2 else old.names.size
        val names = arrayOfNulls<String>(cap)
        val values = arrayOfNulls<Any?>(cap) as Array<V?>
        val mask = cap - 1
        for (i in old.names.indices) {
            val n = old.names[i] ?: continue
            val v = old.values[i] ?: continue
            place(n, v, names, mask, values)
        }
        place(name, value, names, mask, values)
        return Table(names, values, old.size + 1)
    }

    private fun place(
        n: String,
        v: V,
        names: Array<String?>,
        mask: Int,
        values: Array<V?>,
    ) {
        var slot = (n.hashCode() xor (n.hashCode() ushr 16)) and mask
        while (names[slot] != null) slot = (slot + 1) and mask
        names[slot] = n
        values[slot] = v
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
