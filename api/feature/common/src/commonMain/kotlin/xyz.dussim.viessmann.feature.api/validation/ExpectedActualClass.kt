package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
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

// Pre-compute KClass references
private val stringValueClass = StringValue::class
private val booleanValueClass = BooleanValue::class
private val doubleValueClass = DoubleValue::class
private val listDoubleValueClass = ListDoubleValue::class
private val listStringValueClass = ListStringValue::class
private val listDeviceErrorValueClass = ListDeviceErrorValue::class
private val listZigbeeDeviceStatusValueClass = ListZigbeeDeviceStatusValue::class
private val listRoomActorValueClass = ListRoomActorValue::class
private val listDeviceValueClass = ListDeviceValue::class
private val objectOtherRoomConfigurationValueClass = ObjectOtherRoomConfigurationValue::class
private val scheduleValueClass = ScheduleValue::class
private val listEmptyValueClass = ListEmptyValue::class
private val unknownValueClass = UnknownValue::class
private val booleanConstraintsClass = BooleanConstraints::class
private val numberConstraintsClass = NumberConstraints::class
private val stringConstraintsClass = StringConstraints::class
private val scheduleConstraintsClass = ScheduleConstraints::class
private val unknownConstraintsClass = UnknownConstraints::class

@PublishedApi
internal const val stringValueClassIndex = 0

@PublishedApi
internal const val booleanValueClassIndex = 1

@PublishedApi
internal const val doubleValueClassIndex = 2

@PublishedApi
internal const val listDoubleValueClassIndex = 3

@PublishedApi
internal const val listStringValueClassIndex = 4

@PublishedApi
internal const val listDeviceErrorValueClassIndex = 5

@PublishedApi
internal const val listZigbeeDeviceStatusValueClassIndex = 6

@PublishedApi
internal const val listRoomActorValueClassIndex = 7

@PublishedApi
internal const val listDeviceValueClassIndex = 8

@PublishedApi
internal const val objectOtherRoomConfigurationValueClassIndex = 9

@PublishedApi
internal const val scheduleValueClassIndex = 10

@PublishedApi
internal const val listEmptyValueClassIndex = 11

@PublishedApi
internal const val unknownValueClassIndex = 12

@PublishedApi
internal const val booleanConstraintsClassIndex = 13

@PublishedApi
internal const val numberConstraintsClassIndex = 14

@PublishedApi
internal const val stringConstraintsClassIndex = 15

@PublishedApi
internal const val scheduleConstraintsClassIndex = 16

@PublishedApi
internal const val unknownConstraintsClassIndex = 17

// this is funny but... equals call is faster than == due to kotlin inserting intrinsics for == call to first check for null on this, but we know it can't be null
@Suppress("ReplaceCallWithBinaryOperator")
fun KClass<*>.toInt(): Int =
    when {
        this.equals(stringValueClass) -> stringValueClassIndex
        this.equals(booleanValueClass) -> booleanValueClassIndex
        this.equals(doubleValueClass) -> doubleValueClassIndex
        this.equals(listDoubleValueClass) -> listDoubleValueClassIndex
        this.equals(listStringValueClass) -> listStringValueClassIndex
        this.equals(listDeviceErrorValueClass) -> listDeviceErrorValueClassIndex
        this.equals(listZigbeeDeviceStatusValueClass) -> listZigbeeDeviceStatusValueClassIndex
        this.equals(listRoomActorValueClass) -> listRoomActorValueClassIndex
        this.equals(listDeviceValueClass) -> listDeviceValueClassIndex
        this.equals(objectOtherRoomConfigurationValueClass) -> objectOtherRoomConfigurationValueClassIndex
        this.equals(scheduleValueClass) -> scheduleValueClassIndex
        this.equals(listEmptyValueClass) -> listEmptyValueClassIndex
        this.equals(unknownValueClass) -> unknownValueClassIndex
        this.equals(booleanConstraintsClass) -> booleanConstraintsClassIndex
        this.equals(numberConstraintsClass) -> numberConstraintsClassIndex
        this.equals(stringConstraintsClass) -> stringConstraintsClassIndex
        this.equals(scheduleConstraintsClass) -> scheduleConstraintsClassIndex
        this.equals(unknownConstraintsClass) -> unknownConstraintsClassIndex
        else -> error("Unknown class: ${this.simpleName}")
    }

private val EXPECTED_ACTUAL_CLASSES by lazy {
    val all =
        listOf(
            stringValueClass,
            booleanValueClass,
            doubleValueClass,
            listDoubleValueClass,
            listStringValueClass,
            listDeviceErrorValueClass,
            listZigbeeDeviceStatusValueClass,
            listRoomActorValueClass,
            listDeviceValueClass,
            objectOtherRoomConfigurationValueClass,
            scheduleValueClass,
            listEmptyValueClass,
            unknownValueClass,
            booleanConstraintsClass,
            numberConstraintsClass,
            stringConstraintsClass,
            scheduleConstraintsClass,
            unknownConstraintsClass,
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
    init {
        require(indexIntoList >= 0) { "Index into list must be non-negative, got $indexIntoList" }
        require(indexIntoList < 18 * 18) { "Index into list must be less than 18 * 18, got $indexIntoList" }
    }

    val expectedClass: KClass<*> get() = EXPECTED_ACTUAL_CLASSES[indexIntoList].first
    val actualClass: KClass<*> get() = EXPECTED_ACTUAL_CLASSES[indexIntoList].second

    companion object {
        fun of(
            expected: Int,
            actual: Int,
        ) = ExpectedActualClass(expected * 18 + actual)
    }
}

// Cache common error types to avoid repeated allocations, I identified kotlin was caching those ::class calls, but it was still slower than this
@Suppress("NOTHING_TO_INLINE")
object PropertyValidationErrors {
    fun interface MismatchPropertiesGetter {
        operator fun invoke(actualIndex: Int): ValidationResult<ComponentTypeMismatch>
    }

    @PublishedApi
    internal val mismatches =
        Array(18) { IntToObjectMap.of<ValidationResult<ComponentTypeMismatch>>() }

    inline fun getMismatchProperties(
        typeOfComponent: ValidationError.ComponentType,
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
                            typeOfComponent,
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
    enum class ComponentType {
        Feature,
        Property,
        Command,
        Constraint,
    }

    @JvmRecord
    data class MissingComponent(
        val typeOfComponent: ComponentType,
        val nameOfComponent: String,
        val expectedTypeOfComponent: KClass<*>,
    ) : ValidationError

    @JvmRecord
    data class ComponentTypeMismatch(
        val typeOfComponent: ComponentType,
        val nameOfComponent: String,
        val expectedActualClass: ExpectedActualClass,
    ) : ValidationError

    @JvmRecord
    data class NumberOfParametersMismatch(
        val expected: Expected,
        val actual: Int,
    ) : ValidationError {
        data class Expected(
            val typeOfComponent: ComponentType,
            val nameOfComponent: String,
            val expected: Int,
        )
    }

    @JvmRecord
    data class WrongFeatureImplementation(
        val featureName: String,
        val expected: KClass<out Feature>,
        val actual: KClass<out Feature>,
    ) : ValidationError
}
