package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Constraints
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
import xyz.dussim.viessmann.feature.api.PropertyValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.UnknownConstraints
import xyz.dussim.viessmann.feature.api.UnknownValue
import xyz.dussim.viessmann.feature.api.constraintsClass
import xyz.dussim.viessmann.feature.api.propertyValueClass
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Command
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Constraint
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Feature
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Property
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentTypeMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationError.MissingComponent
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationError.WrongFeatureImplementation
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import kotlin.jvm.JvmField
import kotlin.jvm.JvmRecord
import kotlin.reflect.KClass

@JvmRecord
@ConsistentCopyVisibility
data class ExpectedActualClass private constructor(
    val expected: KClass<*>,
    val actual: KClass<*>,
) {
    companion object {
        private val PROPERTIES =
            buildMap<KClass<out PropertyValue<*>>, MutableMap<KClass<out PropertyValue<*>>, ExpectedActualClass>> {
                val allTypes =
                    listOf(
                        UnknownValue::class,
                        BooleanValue::class,
                        DoubleValue::class,
                        StringValue::class,
                        ListDoubleValue::class,
                        ListStringValue::class,
                        ListDeviceErrorValue::class,
                        ListZigbeeDeviceStatusValue::class,
                        ListRoomActorValue::class,
                        ListDeviceValue::class,
                        ObjectOtherRoomConfigurationValue::class,
                        ScheduleValue::class,
                        ListEmptyValue::class,
                    )

                allTypes.forEach { expected ->
                    allTypes.forEach { actual ->
                        getOrPut(expected) { mutableMapOf() }
                            .getOrPut(actual) { ExpectedActualClass(expected, actual) }
                    }
                }
            }

        private val CONSTRAINTS =
            buildMap<KClass<out Constraints<*>>, MutableMap<KClass<out Constraints<*>>, ExpectedActualClass>> {
                val allTypes =
                    listOf(
                        UnknownConstraints::class,
                        BooleanConstraints::class,
                        NumberConstraints::class,
                        StringConstraints::class,
                        ScheduleConstraints::class,
                    )

                allTypes.forEach { expected ->
                    allTypes.forEach { actual ->
                        getOrPut(expected) { mutableMapOf() }
                            .getOrPut(actual) { ExpectedActualClass(expected, actual) }
                    }
                }
            }

        fun ofProperties(
            expected: KClass<out PropertyValue<*>>,
            actual: KClass<out PropertyValue<*>>,
        ): ExpectedActualClass = PROPERTIES.getValue(expected).getValue(actual)

        fun ofConstraints(
            expected: KClass<out Constraints<*>>,
            actual: KClass<out Constraints<*>>,
        ): ExpectedActualClass = CONSTRAINTS.getValue(expected).getValue(actual)
    }
}

// Cache common error types to avoid repeated allocations, I identified kotlin was caching those ::class calls, but it was still slower than this
@Suppress("NOTHING_TO_INLINE")
object PropertyValidationErrors {
    // Pre-compute KClass references
    @JvmField
    val stringValueClass = StringValue::class

    @JvmField
    val booleanValueClass = BooleanValue::class

    @JvmField
    val doubleValueClass = DoubleValue::class

    @JvmField
    val listDoubleValueClass = ListDoubleValue::class

    @JvmField
    val listStringValueClass = ListStringValue::class

    @JvmField
    val listDeviceErrorValueClass = ListDeviceErrorValue::class

    @JvmField
    val listZigbeeDeviceStatusValueClass = ListZigbeeDeviceStatusValue::class

    @JvmField
    val listRoomActorValueClass = ListRoomActorValue::class

    @JvmField
    val listDeviceValueClass = ListDeviceValue::class

    @JvmField
    val objectOtherRoomConfigurationValueClass = ObjectOtherRoomConfigurationValue::class

    @JvmField
    val scheduleValueClass = ScheduleValue::class

    @JvmField
    val listEmptyValueClass = ListEmptyValue::class

    @JvmField
    val unknownValueClass = UnknownValue::class

    @JvmField
    val booleanConstraintsClass = BooleanConstraints::class

    @JvmField
    val numberConstraintsClass = NumberConstraints::class

    @JvmField
    val stringConstraintsClass = StringConstraints::class

    @JvmField
    val scheduleConstraintsClass = ScheduleConstraints::class

    @JvmField
    val unknownConstraintsClass = UnknownConstraints::class

    @PublishedApi
    internal val mismatches =
        Array(18) { IntToObjectMap.of<ValidationResult<ComponentTypeMismatch>>() }

    inline fun getMismatchProperties(
        typeOfComponent: ValidationError.ComponentType,
        nameOfComponent: String,
        expectedClass: KClass<out PropertyValue<*>>,
    ): (actualClass: KClass<out PropertyValue<*>>) -> ValidationResult<ComponentTypeMismatch> {
        val expectedIndex = expectedClass.toInt()
        return { actualClass ->
            val actualIndex = actualClass.toInt()
            val map = mismatches[expectedIndex]
            val value = map[actualIndex]
            if (value != null) {
                value
            } else {
                val value =
                    ValidationResult.of(
                        ComponentTypeMismatch(
                            typeOfComponent,
                            nameOfComponent,
                            ExpectedActualClass.ofProperties(expectedClass, actualClass),
                        ),
                    )
                mismatches[expectedIndex] =
                    IntToObjectMap.of(
                        actualClass.toInt(),
                        value,
                        map,
                    )

                value
            }
        }
    }

    inline fun getMismatchConstraints(
        typeOfComponent: ValidationError.ComponentType,
        nameOfComponent: String,
        expectedClass: KClass<out Constraints<*>>,
    ): (actualClass: KClass<out Constraints<*>>) -> ValidationResult<ComponentTypeMismatch> {
        val expectedIndex = expectedClass.toInt()
        return { actualClass ->
            val actualIndex = actualClass.toInt()
            val map = mismatches[expectedIndex]
            val value = map[actualIndex]
            if (value != null) {
                value
            } else {
                val value =
                    ValidationResult.of(
                        ComponentTypeMismatch(
                            typeOfComponent,
                            nameOfComponent,
                            ExpectedActualClass.ofConstraints(expectedClass, actualClass),
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

    // this is funny but... equals call is faster than == due to kotlin inserting intrinsics for == call to first check for null on this, but we know it can't be null
    @Suppress("ReplaceCallWithBinaryOperator")
    fun KClass<*>.toInt(): Int =
        when {
            this.equals(stringValueClass) -> 0
            this.equals(booleanValueClass) -> 1
            this.equals(doubleValueClass) -> 2
            this.equals(listDoubleValueClass) -> 3
            this.equals(listStringValueClass) -> 4
            this.equals(listDeviceErrorValueClass) -> 5
            this.equals(listZigbeeDeviceStatusValueClass) -> 6
            this.equals(listRoomActorValueClass) -> 7
            this.equals(listDeviceValueClass) -> 8
            this.equals(objectOtherRoomConfigurationValueClass) -> 9
            this.equals(scheduleValueClass) -> 10
            this.equals(listEmptyValueClass) -> 11
            this.equals(unknownValueClass) -> 12
            this.equals(booleanConstraintsClass) -> 13
            this.equals(numberConstraintsClass) -> 14
            this.equals(stringConstraintsClass) -> 15
            this.equals(scheduleConstraintsClass) -> 16
            this.equals(unknownConstraintsClass) -> 17
            else -> error("Unknown class: ${this.simpleName}")
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
        val info: Info,
        val actual: Int,
    ) : ValidationError {
        data class Info(
            val typeOfComponent: ComponentType,
            val nameOfComponent: String,
            val expected: Int,
        )

        val typeOfComponent: ComponentType get() = info.typeOfComponent
        val nameOfComponent: String get() = info.nameOfComponent
        val expected: Int get() = info.expected
    }

    @JvmRecord
    data class WrongFeatureImplementation(
        val featureName: String,
        val expected: KClass<out Feature>,
        val actual: KClass<out Feature>,
    ) : ValidationError
}
