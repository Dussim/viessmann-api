@file:Suppress("NOTHING_TO_INLINE", "unused")

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
import xyz.dussim.viessmann.feature.api.Constraints
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EnergyMatrixConstraints
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.FactoryResetInfoValue
import xyz.dussim.viessmann.feature.api.Feature
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
import xyz.dussim.viessmann.feature.api.PropertyValue
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.TestResultValue
import xyz.dussim.viessmann.feature.api.UnknownConstraints
import xyz.dussim.viessmann.feature.api.validation.PropertyValidationErrors.getMismatchProperties
import xyz.dussim.viessmann.feature.api.validation.ValidationError.MissingComponent
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch.Expected
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid

val MISSING_COMMAND_EXPECTED_CLASS = ExpectedActualClass.of(COMMAND_CLASS_INDEX)

inline fun propertyHash(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)

@PublishedApi
internal inline fun <reified T : PropertyValue<*>> typedPropertyRule(
    propertyName: String,
    expectedIndex: Int,
    required: Boolean,
): ValidationRule<Feature, ValidationError> {
    val missingResult =
        if (required) {
            Invalid(MissingComponent(propertyName, ExpectedActualClass.of(expectedIndex)))
        } else {
            ValidationResult.Valid
        }
    val getMismatchProperties = getMismatchProperties(propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, precomputedHash] ?: return@ValidationRule missingResult
        if (property.value is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(property.value.propertyValueClassIndex)
        }
    }
}

@PublishedApi
internal inline fun <reified T : PropertyValue<*>, reified NonNullT : PropertyValue<*>> typedNullablePropertyRule(
    propertyName: String,
    expectedIndex: Int,
    required: Boolean,
): ValidationRule<Feature, ValidationError> {
    val missingResult =
        if (required) {
            Invalid(MissingComponent(propertyName, ExpectedActualClass.of(expectedIndex)))
        } else {
            ValidationResult.Valid
        }
    val getMismatchProperties = getMismatchProperties(propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, precomputedHash] ?: return@ValidationRule missingResult
        if (property.value is T || property.value is NonNullT) {
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
    required: Boolean,
): ValidationRule<Feature, ValidationError> {
    val missingResult =
        if (required) {
            Invalid(MissingComponent(propertyName, ExpectedActualClass.of(expectedIndex)))
        } else {
            ValidationResult.Valid
        }
    val getMismatchProperties = getMismatchProperties(propertyName, expectedIndex)
    val precomputedHash = propertyHash(propertyName.hashCode(), propertyName.length)

    return ValidationRule { target ->
        val property = target.properties[propertyName, precomputedHash] ?: return@ValidationRule missingResult
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
    val missingError = Invalid(MissingComponent(parameterName, ExpectedActualClass.of(expectedIndex)))
    val getMismatchProperties = getMismatchProperties(parameterName, expectedIndex)
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

fun stringPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<StringValue>(propertyName, STRING_VALUE_CLASS_INDEX, required)

fun booleanPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<BooleanValue>(propertyName, BOOLEAN_VALUE_CLASS_INDEX, required)

fun doublePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<DoubleValue>(propertyName, DOUBLE_VALUE_CLASS_INDEX, required)

fun nullableStringPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedNullablePropertyRule<NullableStringValue, StringValue>(propertyName, NULLABLE_STRING_VALUE_CLASS_INDEX, required)

fun nullableBooleanPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedNullablePropertyRule<NullableBooleanValue, BooleanValue>(propertyName, NULLABLE_BOOLEAN_VALUE_CLASS_INDEX, required)

fun nullableDoublePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedNullablePropertyRule<NullableDoubleValue, DoubleValue>(propertyName, NULLABLE_DOUBLE_VALUE_CLASS_INDEX, required)

fun listDoublePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListDoubleValue>(propertyName, LIST_DOUBLE_VALUE_CLASS_INDEX, required)

fun listStringPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListStringValue>(propertyName, LIST_STRING_VALUE_CLASS_INDEX, required)

fun listDeviceErrorPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListDeviceErrorValue>(propertyName, LIST_DEVICE_ERROR_VALUE_CLASS_INDEX, required)

fun listZigbeeDeviceStatusPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListZigbeeDeviceStatusValue>(propertyName, LIST_ZIGBEE_DEVICE_STATUS_VALUE_CLASS_INDEX, required)

fun listRoomActorPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListRoomActorValue>(propertyName, LIST_ROOM_ACTOR_VALUE_CLASS_INDEX, required)

fun listDevicePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListDeviceValue>(propertyName, LIST_DEVICE_VALUE_CLASS_INDEX, required)

fun objectOtherRoomConfigurationPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<ObjectOtherRoomConfigurationValue>(
    propertyName,
    OBJECT_OTHER_ROOM_CONFIGURATION_VALUE_CLASS_INDEX,
    required,
)

fun schedulePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<ScheduleValue>(propertyName, SCHEDULE_VALUE_CLASS_INDEX, required)

fun listBusTypePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListBusTypeValue>(propertyName, LIST_BUS_TYPE_CLASS_INDEX, required)

fun energyMatrixPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<EnergyMatrixValue>(propertyName, ENERGY_MATRIX_VALUE_CLASS_INDEX, required)

fun logsPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<LogsValue>(propertyName, LOGS_VALUE_CLASS_INDEX, required)

fun listLogBookEntryPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListLogBookEntryValue>(propertyName, LIST_LOG_BOOK_ENTRY_VALUE_CLASS_INDEX, required)

fun listOnboardUpdaterLastErrorCodePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListOnboardUpdaterLastErrorCodeValue>(
    propertyName,
    LIST_ONBOARD_UPDATER_LAST_ERROR_CODE_VALUE_CLASS_INDEX,
    required,
)

fun productInfoPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<ProductInfoValue>(propertyName, PRODUCT_INFO_VALUE_CLASS_INDEX, required)

fun factoryResetInfoPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<FactoryResetInfoValue>(propertyName, FACTORY_RESET_INFO_VALUE_CLASS_INDEX, required)

fun listEebusDevicePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListEebusDeviceValue>(propertyName, LIST_EEBUS_DEVICE_VALUE_CLASS_INDEX, required)

fun listEebusDevicesPairedPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListEebusDevicesPairedValue>(
    propertyName,
    LIST_EEBUS_DEVICES_PAIRED_VALUE_CLASS_INDEX,
    required,
)

fun listEebusServicePartnerPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListEebusServicePartnerValue>(propertyName, LIST_EEBUS_SERVICE_PARTNER_VALUE_CLASS_INDEX, required)

fun listElectricalEnergyMatrixPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListElectricalEnergyMatrixValue>(
    propertyName,
    LIST_ELECTRICAL_ENERGY_MATRIX_VALUE_CLASS_INDEX,
    required,
)

fun listOperatingDataCellsDetailPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListOperatingDataCellsDetailValue>(
    propertyName,
    LIST_OPERATING_DATA_CELLS_DETAIL_VALUE_CLASS_INDEX,
    required,
)

fun listEnergyChargedDevicePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListEnergyChargedDeviceValue>(propertyName, LIST_ENERGY_CHARGED_DEVICE_VALUE_CLASS_INDEX, required)

fun listDeviceInformationPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListDeviceInformationValue>(propertyName, LIST_DEVICE_INFORMATION_VALUE_CLASS_INDEX, required)

fun listSensorPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListSensorValue>(propertyName, LIST_SENSOR_VALUE_CLASS_INDEX, required)

fun listPowerBalanceEntryPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListPowerBalanceEntryValue>(propertyName, LIST_POWER_BALANCE_ENTRY_VALUE_CLASS_INDEX, required)

fun listFuelCellErrorPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListFuelCellErrorValue>(propertyName, LIST_FUEL_CELL_ERROR_VALUE_CLASS_INDEX, required)

fun listWifiNetworkPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListWifiNetworkValue>(propertyName, LIST_WIFI_NETWORK_VALUE_CLASS_INDEX, required)

fun listVentilationMessagePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListVentilationMessageValue>(propertyName, LIST_VENTILATION_MESSAGE_VALUE_CLASS_INDEX, required)

fun listSystemMessageEntryPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListSystemMessageEntryValue>(propertyName, LIST_SYSTEM_MESSAGE_ENTRY_VALUE_CLASS_INDEX, required)

fun listSolarlogDevicePropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListSolarlogDeviceValue>(propertyName, LIST_SOLARLOG_DEVICE_VALUE_CLASS_INDEX, required)

fun listSolarlogDevicesPairedPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedListPropertyRule<ListSolarlogDevicesPairedValue>(
    propertyName,
    LIST_SOLARLOG_DEVICES_PAIRED_VALUE_CLASS_INDEX,
    required,
)

fun testResultPropertyRule(
    propertyName: String,
    required: Boolean = true,
) = typedPropertyRule<TestResultValue>(propertyName, TEST_RESULT_VALUE_CLASS_INDEX, required)

fun numberOfParametersRule(
    expected: Int,
    name: String,
): ValidationRule<Command, ValidationError> {
    val expectedData = Expected(name, expected)
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

/**
 * Combines the expected parameter constraint and the one-parameter count check.
 *
 * The aggregate path preserves the existing error order: the parameter error is reported before
 * [NumberOfParametersMismatch], and both are retained when both checks fail.
 */
fun oneParameterCommandRule(
    commandName: String,
    parameterRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Command, ValidationError> {
    val expectedData = Expected(commandName, 1)

    return ValidationRule { command ->
        val parameterResult = parameterRule.validate(command)
        if (command.params.size == 1) return@ValidationRule parameterResult

        val countResult = Invalid(NumberOfParametersMismatch(expectedData, command.params.size))
        ValidationResult.of(parameterResult, countResult)
    }
}

/**
 * Fail-fast counterpart of [oneParameterCommandRule].
 *
 * Parameter validation remains first to preserve the generated command validator's observable
 * first-error behaviour.
 */
fun oneParameterCommandFailFastRule(
    commandName: String,
    parameterRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Command, ValidationError> {
    val expectedData = Expected(commandName, 1)

    return ValidationRule { command ->
        val parameterResult = parameterRule.validate(command)
        if (parameterResult.isInvalid) return@ValidationRule parameterResult

        if (command.params.size == 1) {
            ValidationResult.Valid
        } else {
            Invalid(NumberOfParametersMismatch(expectedData, command.params.size))
        }
    }
}

fun stringConstraintsRule(parameterName: String) = typedCommandRule<StringConstraints>(parameterName, STRING_CONSTRAINTS_CLASS_INDEX)

fun numberConstraintsRule(parameterName: String) = typedCommandRule<NumberConstraints>(parameterName, NUMBER_CONSTRAINTS_CLASS_INDEX)

fun booleanConstraintsRule(parameterName: String) = typedCommandRule<BooleanConstraints>(parameterName, BOOLEAN_CONSTRAINTS_CLASS_INDEX)

fun arrayNumberConstraintsRule(parameterName: String) = typedArrayCommandRule<ArrayNumberConstraints>(parameterName, ARRAY_NUMBER_CONSTRAINTS_CLASS_INDEX)

fun arrayStringConstraintsRule(parameterName: String) = typedArrayCommandRule<ArrayStringConstraints>(parameterName, ARRAY_STRING_CONSTRAINTS_CLASS_INDEX)

fun arrayBooleanConstraintsRule(parameterName: String) = typedArrayCommandRule<ArrayBooleanConstraints>(parameterName, ARRAY_BOOLEAN_CONSTRAINTS_CLASS_INDEX)

fun arrayObjectConstraintsRule(parameterName: String) = typedArrayCommandRule<ArrayObjectConstraints>(parameterName, ARRAY_OBJECT_CONSTRAINTS_CLASS_INDEX)

fun arrayUnknownConstraintsRule(parameterName: String) = typedArrayCommandRule<ArrayUnknownConstraints>(parameterName, ARRAY_UNKNOWN_CONSTRAINTS_CLASS_INDEX)

fun arrayConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> {
    val missingError = Invalid(MissingComponent(parameterName, ExpectedActualClass.of(ARRAY_CONSTRAINTS_CLASS_INDEX)))
    val getMismatchProperties = getMismatchProperties(parameterName, ARRAY_CONSTRAINTS_CLASS_INDEX)
    val precomputedHash = propertyHash(parameterName.hashCode(), parameterName.length)

    return ValidationRule { target ->
        val constraints = target.params[parameterName, precomputedHash]?.constraints ?: return@ValidationRule missingError
        if (constraints is ArrayConstraints) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(constraints.constraintsClassIndex)
        }
    }
}

/**
 * Accepts `ArrayEmptyConstraints` as valid for any typed array rule: schemas declaring `type: "array"` without an
 * `enum` deserialize to `ArrayEmptyConstraints`, carry no element-type information, and must still match generated
 * typed rules. Mirrors the `ArrayEmptyConstraints` fallback in `CommandImplementationGenerator`.
 */
@PublishedApi
internal inline fun <reified T : ArrayConstraints> typedArrayCommandRule(
    parameterName: String,
    expectedIndex: Int,
): ValidationRule<Command, ValidationError> {
    val missingError = Invalid(MissingComponent(parameterName, ExpectedActualClass.of(expectedIndex)))
    val getMismatchProperties = getMismatchProperties(parameterName, expectedIndex)
    val precomputedHash = propertyHash(parameterName.hashCode(), parameterName.length)

    return ValidationRule { target ->
        val constraints = target.params[parameterName, precomputedHash]?.constraints ?: return@ValidationRule missingError
        if (constraints is ArrayEmptyConstraints || constraints is T) {
            ValidationResult.Valid
        } else {
            getMismatchProperties(constraints.constraintsClassIndex)
        }
    }
}

fun objectConstraintsRule(parameterName: String) = typedCommandRule<ObjectConstraints>(parameterName, OBJECT_CONSTRAINTS_CLASS_INDEX)

fun unknownConstraintsRule(parameterName: String) = typedCommandRule<UnknownConstraints>(parameterName, UNKNOWN_CONSTRAINTS_CLASS_INDEX)

fun scheduleConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> =
    typedCommandRule<ScheduleConstraints>(parameterName, SCHEDULE_CONSTRAINTS_CLASS_INDEX)

fun energyMatrixConstraintsRule(parameterName: String): ValidationRule<Command, ValidationError> =
    typedCommandRule<EnergyMatrixConstraints>(parameterName, ENERGY_MATRIX_CONSTRAINTS_CLASS_INDEX)

fun zeroParameterCommandRule(commandName: String): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(commandName, MISSING_COMMAND_EXPECTED_CLASS))
    val expectedData = Expected(commandName, 0)
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        val command = feature.commands[commandName, precomputedHash] ?: return@ValidationRule missingError
        if (command.params.size == 0) {
            ValidationResult.Valid
        } else {
            Invalid(NumberOfParametersMismatch(expectedData, command.params.size))
        }
    }
}

/**
 * Validates an optional zero-parameter command. An absent command is valid; a present command must not declare
 * parameters.
 */
fun optionalZeroParameterCommandRule(commandName: String): ValidationRule<Feature, ValidationError> {
    val expectedData = Expected(commandName, 0)
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        val command = feature.commands[commandName, precomputedHash] ?: return@ValidationRule ValidationResult.Valid
        if (command.params.size == 0) {
            ValidationResult.Valid
        } else {
            Invalid(NumberOfParametersMismatch(expectedData, command.params.size))
        }
    }
}

fun commandRule(
    commandName: String,
    innerRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(commandName, MISSING_COMMAND_EXPECTED_CLASS))
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        innerRule.validate(feature.commands[commandName, precomputedHash] ?: return@ValidationRule missingError)
    }
}

/**
 * Validates a required command with the generated fail-fast command path.
 */
fun commandFailFastRule(
    commandName: String,
    innerRule: GeneratedValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val missingError = Invalid(MissingComponent(commandName, MISSING_COMMAND_EXPECTED_CLASS))
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        innerRule.validateFailFast(feature.commands[commandName, precomputedHash] ?: return@ValidationRule missingError)
    }
}

/**
 * Validates an optional command. An absent command is valid; a present command is checked by [innerRule].
 */
fun optionalCommandRule(
    commandName: String,
    innerRule: ValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        feature.commands[commandName, precomputedHash]
            ?.let(innerRule::validate)
            ?: ValidationResult.Valid
    }
}

/**
 * Validates an optional command with the generated fail-fast command path.
 */
fun optionalCommandFailFastRule(
    commandName: String,
    innerRule: GeneratedValidationRule<Command, ValidationError>,
): ValidationRule<Feature, ValidationError> {
    val precomputedHash = propertyHash(commandName.hashCode(), commandName.length)

    return ValidationRule { feature ->
        feature.commands[commandName, precomputedHash]
            ?.let(innerRule::validateFailFast)
            ?: ValidationResult.Valid
    }
}
