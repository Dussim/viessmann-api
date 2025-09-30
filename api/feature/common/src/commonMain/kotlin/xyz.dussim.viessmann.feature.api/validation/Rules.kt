@file:Suppress("NOTHING_TO_INLINE", "unused")

package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Constraints
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
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
import xyz.dussim.viessmann.feature.api.Property
import xyz.dussim.viessmann.feature.api.PropertyValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.UnknownConstraints
import xyz.dussim.viessmann.feature.api.constraintsClass
import xyz.dussim.viessmann.feature.api.propertyValueClass
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Constraint
import xyz.dussim.viessmann.feature.api.validation.ValidationError.ComponentType.Property
import xyz.dussim.viessmann.feature.api.validation.ValidationError.MissingComponent
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationError.WrongFeatureImplementation
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

@PublishedApi
internal val featureDeviceClass = Feature.Device::class

@PublishedApi
internal val featureGatewayClass = Feature.Gateway::class

@PublishedApi
internal val featureGeofencingClass = Feature.Geofencing::class

@PublishedApi
internal inline fun combineToLong(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedPropertyRule(propertyName: String): ValidationRule<Feature, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Property, propertyName, T::class))
    val getMismatchProperties = PropertyValidationErrors.getMismatchProperties(Property, propertyName, T::class)
    val combined = combineToLong(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, combined] ?: return@ValidationRule missingError
        if (property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClass)
        }
    }
}

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedPropertyRuleProps(propertyName: String): ValidationRule<EfficientStringKeyMap<Property>, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Property, propertyName, T::class))
    val getMismatchProperties = PropertyValidationErrors.getMismatchProperties(Property, propertyName, T::class)
    val combined = combineToLong(propertyName.hashCode(), propertyName.length)

    return ValidationRule { properties ->
        val property = properties[propertyName, combined] ?: return@ValidationRule missingError
        if (property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClass)
        }
    }
}

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedListPropertyRule(propertyName: String): ValidationRule<Feature, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Property, propertyName, T::class))
    val getMismatchProperties = PropertyValidationErrors.getMismatchProperties(Property, propertyName, T::class)
    val combined = combineToLong(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, combined] ?: return@ValidationRule missingError
        if (property.value is ListEmptyValue || property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClass)
        }
    }
}

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedListPropertyRuleProps(propertyName: String): ValidationRule<EfficientStringKeyMap<Property>, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Property, propertyName, T::class))
    val getMismatchProperties = PropertyValidationErrors.getMismatchProperties(Property, propertyName, T::class)
    val combined = combineToLong(propertyName.hashCode(), propertyName.length)

    return ValidationRule { properties ->
        val property = properties[propertyName, combined] ?: return@ValidationRule missingError
        if (property.value is ListEmptyValue || property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClass)
        }
    }
}

@PublishedApi
internal inline fun <reified T : Constraints<*>> typedCommandRule(propertyName: String): ValidationRule<Command, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Command, propertyName, T::class))
    val getMismatchProperties = PropertyValidationErrors.getMismatchConstraints(Constraint, propertyName, T::class)
    val combined = combineToLong(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.params[propertyName, combined]?.constraints ?: return@ValidationRule missingError
        if (property is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.constraintsClass)
        }
    }
}

inline fun stringPropertyRule(propertyName: String) = typedPropertyRule<StringValue>(propertyName)

inline fun booleanPropertyRule(propertyName: String) = typedPropertyRule<BooleanValue>(propertyName)

inline fun doublePropertyRule(propertyName: String) = typedPropertyRule<DoubleValue>(propertyName)

inline fun listDoublePropertyRule(propertyName: String) = typedListPropertyRule<ListDoubleValue>(propertyName)

inline fun listStringPropertyRule(propertyName: String) = typedListPropertyRule<ListStringValue>(propertyName)

inline fun listDeviceErrorPropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceErrorValue>(propertyName)

inline fun listZigbeeDeviceStatusPropertyRule(propertyName: String) = typedListPropertyRule<ListZigbeeDeviceStatusValue>(propertyName)

inline fun listRoomActorPropertyRule(propertyName: String) = typedListPropertyRule<ListRoomActorValue>(propertyName)

inline fun listDevicePropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceValue>(propertyName)

inline fun objectOtherRoomConfigurationPropertyRule(propertyName: String) = typedPropertyRule<ObjectOtherRoomConfigurationValue>(propertyName)

inline fun schedulePropertyRule(propertyName: String) = typedPropertyRule<ScheduleValue>(propertyName)

inline fun stringPropertyRuleProps(propertyName: String) = typedPropertyRuleProps<StringValue>(propertyName)

inline fun booleanPropertyRuleProps(propertyName: String) = typedPropertyRuleProps<BooleanValue>(propertyName)

inline fun doublePropertyRuleProps(propertyName: String) = typedPropertyRuleProps<DoubleValue>(propertyName)

inline fun listDoublePropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDoubleValue>(propertyName)

inline fun listStringPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListStringValue>(propertyName)

inline fun listDeviceErrorPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDeviceErrorValue>(propertyName)

inline fun listZigbeeDeviceStatusPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListZigbeeDeviceStatusValue>(propertyName)

inline fun listRoomActorPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListRoomActorValue>(propertyName)

inline fun listDevicePropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDeviceValue>(propertyName)

inline fun objectOtherRoomConfigurationPropertyRuleProps(propertyName: String) = typedPropertyRuleProps<ObjectOtherRoomConfigurationValue>(propertyName)

inline fun schedulePropertyRuleProps(propertyName: String) = typedPropertyRuleProps<ScheduleValue>(propertyName)

inline fun numberOfParametersRule(
    expected: Int,
    name: String,
): ValidationRule<Command, ValidationError> {
    val info = NumberOfParametersMismatch.Info(ComponentType.Command, name, expected)
    return ValidationRule { command ->
        if (command.params.size == expected) {
            return@ValidationRule ValidationResult.Valid
        } else {
            return@ValidationRule ValidationResult.of(
                NumberOfParametersMismatch(info, command.params.size),
            )
        }
    }
}

inline fun stringConstraintsRule(parameterName: String) = typedCommandRule<StringConstraints>(parameterName)

inline fun numberConstraintsRule(parameterName: String) = typedCommandRule<NumberConstraints>(parameterName)

inline fun booleanConstraintsRule(parameterName: String) = typedCommandRule<BooleanConstraints>(parameterName)

inline fun unknownConstraintsRule(parameterName: String) = typedCommandRule<UnknownConstraints>(parameterName)

inline fun scheduleConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> = typedCommandRule<ScheduleConstraints>(parameterName)

inline fun commandRule(
    commandName: String,
    innerRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val missingError = ValidationResult.of(MissingComponent(ComponentType.Feature, commandName, Command::class))
    val combined = combineToLong(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        val command = feature.commands[commandName, combined] ?: return@ValidationRule missingError
        innerRule(command)
    }
}

inline fun deviceFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
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

inline fun gatewayFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
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

inline fun geofencingFeatureRule(featureName: String): ValidationRule<Feature, ValidationError> {
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
