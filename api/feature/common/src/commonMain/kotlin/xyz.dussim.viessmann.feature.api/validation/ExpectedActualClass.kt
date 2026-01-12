package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
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

@PublishedApi
internal const val STRING_VALUE_CLASS_INDEX = 0

@PublishedApi
internal const val BOOLEAN_VALUE_CLASS_INDEX = 1

@PublishedApi
internal const val DOUBLE_VALUE_CLASS_INDEX = 2

@PublishedApi
internal const val LIST_DOUBLE_VALUE_CLASS_INDEX = 3

@PublishedApi
internal const val LIST_STRING_VALUE_CLASS_INDEX = 4

@PublishedApi
internal const val LIST_DEVICE_ERROR_VALUE_CLASS_INDEX = 5

@PublishedApi
internal const val LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX = 6

@PublishedApi
internal const val LIST_ROOM_ACTOR_VALUE_CLASS_INDEX = 7

@PublishedApi
internal const val LIST_DEVICE_VALUE_CLASS_INDEX = 8

@PublishedApi
internal const val OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX = 9

@PublishedApi
internal const val SCHEDULE_VALUE_CLASS_INDEX = 10

@PublishedApi
internal const val LIST_EMPTY_VALUE_CLASS_INDEX = 11

@PublishedApi
internal const val UNKNOWN_VALUE_CLASS_INDEX = 12

@PublishedApi
internal const val BOOLEAN_CONSTRAINTS_CLASS_INDEX = 13

@PublishedApi
internal const val NUMBER_CONSTRAINTS_CLASS_INDEX = 14

@PublishedApi
internal const val STRING_CONSTRAINTS_CLASS_INDEX = 15

@PublishedApi
internal const val SCHEDULE_CONSTRAINTS_CLASS_INDEX = 16

@PublishedApi
internal const val UNKNOWN_CONSTRAINTS_CLASS_INDEX = 17

@PublishedApi
internal const val COMMAND_CLASS_INDEX = 18

@PublishedApi
internal const val FEATURE_DEVICE_CLASS_INDEX = 19

@PublishedApi
internal const val FEATURE_GATEWAY_CLASS_INDEX = 20

@PublishedApi
internal const val FEATURE_GEOFENCING_CLASS_INDEX = 21

@PublishedApi
internal const val MISSING_COMPONENT_CLASS_INDEX = 22

private val EXPECTED_ACTUAL_CLASSES by lazy {
    val all =
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
            Feature.Device::class,
            Feature.Gateway::class,
            Feature.Geofencing::class,
            Nothing::class,
        )

    buildList(all.size * all.size) {
        for (i in all.indices) {
            for (j in all.indices) {
                add(all[i] to all[j])
            }
        }
    }
}

@JvmInline
value class ExpectedActualClass internal constructor(
    internal val indexIntoList: Int,
) {
    companion object {
        private const val SIZE = 23

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

    val firstIndex get() = indexIntoList / SIZE
    val secondIndex get() = indexIntoList % SIZE
}

// Cache common error types to avoid repeated allocations
@Suppress("NOTHING_TO_INLINE")
object PropertyValidationErrors {
    fun interface MismatchPropertiesGetter {
        operator fun invoke(actualIndex: Int): ValidationResult<ComponentTypeMismatch>
    }

    @PublishedApi
    internal val mismatches =
        Array(23) { IntToObjectMap.of<ValidationResult<ComponentTypeMismatch>>() }

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
    ) : ValidationError

    @JvmRecord
    data class ComponentTypeMismatch(
        val name: String,
        val expectedActualClass: ExpectedActualClass,
    ) : ValidationError

    @JvmRecord
    data class NumberOfParametersMismatch(
        val expected: Expected,
        val actual: Int,
    ) : ValidationError {
        data class Expected(
            val nameOfComponent: String,
            val expected: Int,
        )
    }
}
