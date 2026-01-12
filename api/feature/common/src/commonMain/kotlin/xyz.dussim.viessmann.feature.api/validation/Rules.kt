@file:Suppress("NOTHING_TO_INLINE", "unused")

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
import xyz.dussim.viessmann.feature.api.validation.PropertyValidationErrors.getMismatchProperties
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Constraint
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Property
import xyz.dussim.viessmann.feature.api.validation.ValidationError.MissingComponent
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch.Expected
import xyz.dussim.viessmann.feature.api.validation.ValidationError.WrongFeatureImplementation
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

@PublishedApi
internal val featureDeviceClass = Feature.Device::class

@PublishedApi
internal val featureGatewayClass = Feature.Gateway::class

@PublishedApi
internal val featureGeofencingClass = Feature.Geofencing::class

@PublishedApi
internal inline fun propertyHash(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedPropertyRule(
    propertyName: String,
    expectedIndex: Int,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(Property, propertyName, T::class))
    val getMismatchProperties = getMismatchProperties(Property, propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, precomputedHash] ?: return@ValidationRule missingError
        if (property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClassIndex)
        }
    }
}

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedListPropertyRule(
    propertyName: String,
    expectedIndex: Int,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(Property, propertyName, T::class))
    val getMismatchProperties = getMismatchProperties(Property, propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, precomputedHash] ?: return@ValidationRule missingError
        if (property.value is ListEmptyValue || property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClassIndex)
        }
    }
}

@PublishedApi
internal inline fun <reified T : Constraints<*>> typedCommandRule(
    parameterName: String,
    expectedIndex: Int,
): ValidationRule<Command, ValidationError> {
    val missingError = Invalid(MissingComponent(ComponentType.Command, parameterName, T::class))
    val getMismatchProperties = getMismatchProperties(Constraint, parameterName, expectedIndex)
    val precomputedHash = propertyHash(parameterName.hashCode(), parameterName.length)

    return ValidationRule { target ->
        val property = target.params[parameterName, precomputedHash]?.constraints ?: return@ValidationRule missingError
        if (property is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.constraintsClassIndex)
        }
    }
}

fun stringPropertyRule(propertyName: String) = typedPropertyRule<StringValue>(propertyName, STRING_VALUE_CLASS_INDEX)

fun booleanPropertyRule(propertyName: String) = typedPropertyRule<BooleanValue>(propertyName, BOOLEAN_VALUE_CLASS_INDEX)

fun doublePropertyRule(propertyName: String) = typedPropertyRule<DoubleValue>(propertyName, DOUBLE_VALUE_CLASS_INDEX)

fun listDoublePropertyRule(propertyName: String) = typedListPropertyRule<ListDoubleValue>(propertyName, LIST_DOUBLE_VALUE_CLASS_INDEX)

fun listStringPropertyRule(propertyName: String) = typedListPropertyRule<ListStringValue>(propertyName, LIST_STRING_VALUE_CLASS_INDEX)

fun listDeviceErrorPropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceErrorValue>(propertyName, LIST_DEVICE_ERROR_VALUE_CLASS_INDEX)

fun listZigbeeDeviceStatusPropertyRule(propertyName: String) = typedListPropertyRule<ListZigbeeDeviceStatusValue>(propertyName, LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX)

fun listRoomActorPropertyRule(propertyName: String) = typedListPropertyRule<ListRoomActorValue>(propertyName, LIST_ROOM_ACTOR_VALUE_CLASS_INDEX)

fun listDevicePropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceValue>(propertyName, LIST_DEVICE_VALUE_CLASS_INDEX)

fun objectOtherRoomConfigurationPropertyRule(propertyName: String) =
    typedPropertyRule<ObjectOtherRoomConfigurationValue>(propertyName, OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX)

fun schedulePropertyRule(propertyName: String) = typedPropertyRule<ScheduleValue>(propertyName, SCHEDULE_VALUE_CLASS_INDEX)

fun numberOfParametersRule(
    expected: Int,
    name: String,
): ValidationRule<Command, ValidationError> {
    val expectedData = Expected(ComponentType.Command, name, expected)
    return ValidationRule { command ->
        if (command.params.size == expected) {
            return@ValidationRule ValidationResult.Valid
        } else {
            return@ValidationRule Invalid(
                NumberOfParametersMismatch(expectedData, command.params.size),
            )
        }
    }
}

fun stringConstraintsRule(parameterName: String) = typedCommandRule<StringConstraints>(parameterName, STRING_CONSTRAINTS_CLASS_INDEX)

fun numberConstraintsRule(parameterName: String) = typedCommandRule<NumberConstraints>(parameterName, NUMBER_CONSTRAINTS_CLASS_INDEX)

fun booleanConstraintsRule(parameterName: String) = typedCommandRule<BooleanConstraints>(parameterName, BOOLEAN_CONSTRAINTS_CLASS_INDEX)

fun unknownConstraintsRule(parameterName: String) = typedCommandRule<UnknownConstraints>(parameterName, UNKNOWN_CONSTRAINTS_CLASS_INDEX)

fun scheduleConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> =
    typedCommandRule<ScheduleConstraints>(parameterName, SCHEDULE_CONSTRAINTS_CLASS_INDEX)

fun commandRule(
    commandName: String,
    innerRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(ComponentType.Feature, commandName, Command::class))
    val combined = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        innerRule.validate(feature.commands[commandName, combined] ?: return@ValidationRule missingError)
    }
}

fun deviceFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
    val gatewayError = Invalid(WrongFeatureImplementation(featureName, featureGatewayClass, featureDeviceClass))
    val geofencingError = Invalid(WrongFeatureImplementation(featureName, featureGeofencingClass, featureDeviceClass))

    return ValidationRule { feature ->
        when (feature) {
            is Feature.Device -> ValidationResult.Valid
            is Feature.Gateway -> gatewayError
            is Feature.Geofencing -> geofencingError
        }
    }
}

fun gatewayFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
    val deviceError = Invalid(WrongFeatureImplementation(featureName, featureDeviceClass, featureGatewayClass))
    val geofencingError = Invalid(WrongFeatureImplementation(featureName, featureGeofencingClass, featureGatewayClass))

    return ValidationRule { feature ->
        when (feature) {
            is Feature.Gateway -> ValidationResult.Valid
            is Feature.Device -> deviceError
            is Feature.Geofencing -> geofencingError
        }
    }
}

fun geofencingFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
    val deviceError = Invalid(WrongFeatureImplementation(featureName, featureDeviceClass, featureGeofencingClass))
    val gatewayError = Invalid(WrongFeatureImplementation(featureName, featureGatewayClass, featureGeofencingClass))

    return ValidationRule { feature ->
        when (feature) {
            is Feature.Geofencing -> ValidationResult.Valid
            is Feature.Device -> deviceError
            is Feature.Gateway -> gatewayError
        }
    }
}
