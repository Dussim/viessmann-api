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
internal inline fun <reified T : PropertyValue<*>> typedPropertyRuleProps(
    propertyName: String,
    expectedIndex: Int,
): ValidationRule<EfficientStringKeyMap<Property>, ValidationError> {
    val missingError = Invalid(MissingComponent(Property, propertyName, T::class))
    val getMismatchProperties = getMismatchProperties(Property, propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { properties ->
        val property = properties[propertyName, precomputedHash] ?: return@ValidationRule missingError
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
internal inline fun <reified T : PropertyValue<*>> typedListPropertyRuleProps(
    propertyName: String,
    expectedIndex: Int,
): ValidationRule<EfficientStringKeyMap<Property>, ValidationError> {
    val missingError = Invalid(MissingComponent(Property, propertyName, T::class))
    val getMismatchProperties = getMismatchProperties(Property, propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { properties ->
        val property = properties[propertyName, precomputedHash] ?: return@ValidationRule missingError
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

inline fun stringPropertyRule(propertyName: String) = typedPropertyRule<StringValue>(propertyName, stringValueClassIndex)

inline fun booleanPropertyRule(propertyName: String) = typedPropertyRule<BooleanValue>(propertyName, booleanValueClassIndex)

inline fun doublePropertyRule(propertyName: String) = typedPropertyRule<DoubleValue>(propertyName, doubleValueClassIndex)

inline fun listDoublePropertyRule(propertyName: String) = typedListPropertyRule<ListDoubleValue>(propertyName, listDoubleValueClassIndex)

inline fun listStringPropertyRule(propertyName: String) = typedListPropertyRule<ListStringValue>(propertyName, listStringValueClassIndex)

inline fun listDeviceErrorPropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceErrorValue>(propertyName, listDeviceErrorValueClassIndex)

inline fun listZigbeeDeviceStatusPropertyRule(propertyName: String) = typedListPropertyRule<ListZigbeeDeviceStatusValue>(propertyName, listZigbeeDeviceStatusValueClassIndex)

inline fun listRoomActorPropertyRule(propertyName: String) = typedListPropertyRule<ListRoomActorValue>(propertyName, listRoomActorValueClassIndex)

inline fun listDevicePropertyRule(propertyName: String) = typedListPropertyRule<ListDeviceValue>(propertyName, listDeviceValueClassIndex)

inline fun objectOtherRoomConfigurationPropertyRule(propertyName: String) =
    typedPropertyRule<ObjectOtherRoomConfigurationValue>(propertyName, objectOtherRoomConfigurationValueClassIndex)

inline fun schedulePropertyRule(propertyName: String) = typedPropertyRule<ScheduleValue>(propertyName, scheduleValueClassIndex)

inline fun stringPropertyRuleProps(propertyName: String) = typedPropertyRuleProps<StringValue>(propertyName, stringValueClassIndex)

inline fun booleanPropertyRuleProps(propertyName: String) = typedPropertyRuleProps<BooleanValue>(propertyName, booleanValueClassIndex)

inline fun doublePropertyRuleProps(propertyName: String) = typedPropertyRuleProps<DoubleValue>(propertyName, doubleValueClassIndex)

inline fun listDoublePropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDoubleValue>(propertyName, listDoubleValueClassIndex)

inline fun listStringPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListStringValue>(propertyName, listStringValueClassIndex)

inline fun listDeviceErrorPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDeviceErrorValue>(propertyName, listDeviceErrorValueClassIndex)

inline fun listZigbeeDeviceStatusPropertyRuleProps(propertyName: String) =
    typedListPropertyRuleProps<ListZigbeeDeviceStatusValue>(propertyName, listZigbeeDeviceStatusValueClassIndex)

inline fun listRoomActorPropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListRoomActorValue>(propertyName, listRoomActorValueClassIndex)

inline fun listDevicePropertyRuleProps(propertyName: String) = typedListPropertyRuleProps<ListDeviceValue>(propertyName, listDeviceValueClassIndex)

inline fun objectOtherRoomConfigurationPropertyRuleProps(propertyName: String) =
    typedPropertyRuleProps<ObjectOtherRoomConfigurationValue>(propertyName, objectOtherRoomConfigurationValueClassIndex)

inline fun schedulePropertyRuleProps(propertyName: String) = typedPropertyRuleProps<ScheduleValue>(propertyName, scheduleValueClassIndex)

inline fun numberOfParametersRule(
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

inline fun stringConstraintsRule(parameterName: String) = typedCommandRule<StringConstraints>(parameterName, stringConstraintsClassIndex)

inline fun numberConstraintsRule(parameterName: String) = typedCommandRule<NumberConstraints>(parameterName, numberConstraintsClassIndex)

inline fun booleanConstraintsRule(parameterName: String) = typedCommandRule<BooleanConstraints>(parameterName, booleanConstraintsClassIndex)

inline fun unknownConstraintsRule(parameterName: String) = typedCommandRule<UnknownConstraints>(parameterName, unknownConstraintsClassIndex)

inline fun scheduleConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> =
    typedCommandRule<ScheduleConstraints>(parameterName, scheduleConstraintsClassIndex)

inline fun commandRule(
    commandName: String,
    innerRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(ComponentType.Feature, commandName, Command::class))
    val combined = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        innerRule.validate(feature.commands[commandName, combined] ?: return@ValidationRule missingError)
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
