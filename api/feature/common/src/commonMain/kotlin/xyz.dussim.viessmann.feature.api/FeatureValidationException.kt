package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

class FeatureValidationException(
    val featureClass: String,
    val feature: Feature,
    val validationRule: ValidationRule<Feature, ValidationError>,
) : RuntimeException() {
    private val validationDetails: ValidationDetails by lazy(LazyThreadSafetyMode.PUBLICATION) { validationDetails() }

    val validationResult: ValidationResult<ValidationError>
        get() = validationDetails.validationResult

    override val message: String
        get() = validationDetails.message

    private fun validationDetails(): ValidationDetails {
        val result = validationRule.validate(feature)
        return ValidationDetails(
            validationResult = result,
            message =
                buildString(initialMessageCapacity()) {
                    append(HEADER_PREFIX)
                    append(featureClass)
                    append(HEADER_SUFFIX)
                    appendFeature(feature)
                    append(VALIDATION_ERRORS_HEADER)
                    result.asIterable().forEach { error ->
                        append(VALIDATION_ERROR_PREFIX)
                        append(error)
                    }
                },
        )
    }

    private data class ValidationDetails(
        val validationResult: ValidationResult<ValidationError>,
        val message: String,
    )

    private fun initialMessageCapacity(): Int {
        val featureFixedFieldsCapacity =
            featureClass.length +
                feature.feature.length +
                feature.wildcardFeature.length +
                feature.deviceId.nullableStringLength() +
                feature.gatewayId.nullableStringLength() +
                feature.isActive.toString().length +
                feature.isEnabled.toString().length +
                feature.isReady.toString().length +
                feature.apiVersion.toString().length +
                feature.timestamp.toString().length +
                feature.uri.length
        val paramsCount = feature.commands.values.sumOf { it.params.size }
        return BASE_MESSAGE_CAPACITY +
            featureFixedFieldsCapacity +
            feature.properties.size * PROPERTY_CAPACITY_HEURISTIC +
            feature.commands.size * COMMAND_CAPACITY_HEURISTIC +
            paramsCount * PARAMETER_CAPACITY_HEURISTIC
    }

    private fun StringBuilder.appendFeature(feature: Feature) {
        append(FEATURE_START)
        append(FEATURE_NAME_PREFIX)
        appendQuoted(feature.feature)
        append(FEATURE_WILDCARD_PREFIX)
        appendQuoted(feature.wildcardFeature)
        append(FEATURE_DEVICE_ID_PREFIX)
        appendNullableQuoted(feature.deviceId)
        append(FEATURE_GATEWAY_ID_PREFIX)
        appendNullableQuoted(feature.gatewayId)
        append(FEATURE_IS_ACTIVE_PREFIX)
        append(feature.isActive)
        append(FEATURE_IS_ENABLED_PREFIX)
        append(feature.isEnabled)
        append(FEATURE_IS_READY_PREFIX)
        append(feature.isReady)
        append(FEATURE_API_VERSION_PREFIX)
        append(feature.apiVersion)
        append(FEATURE_TIMESTAMP_PREFIX)
        append(feature.timestamp)
        append(FEATURE_URI_PREFIX)
        appendQuoted(feature.uri)
        appendProperties(feature.properties)
        appendCommands(feature.commands)
        append(BLOCK_END)
    }

    private fun StringBuilder.appendProperties(properties: EfficientStringKeyMap<Property>) {
        append(PROPERTIES_START)
        properties.forEach { (name, property) ->
            append(ITEM_PREFIX)
            append(name)
            append(PROPERTY_TYPE_PREFIX)
            appendQuoted(property.type)
            append(PROPERTY_VALUE_PREFIX)
            append(property.value.element)
            append(PROPERTY_UNIT_PREFIX)
            appendNullableQuoted(property.unit)
            append(CLOSE_PAREN)
        }
        append(MAP_END)
    }

    private fun StringBuilder.appendCommands(commands: EfficientStringKeyMap<Command>) {
        append(COMMANDS_START)
        commands.forEach { (name, command) ->
            append(ITEM_PREFIX)
            append(name)
            append(COMMAND_START)
            append(COMMAND_URI_PREFIX)
            appendQuoted(command.uri)
            append(COMMAND_NAME_PREFIX)
            appendQuoted(command.name)
            append(COMMAND_IS_EXECUTABLE_PREFIX)
            append(command.isExecutable)
            append(PARAMS_START)
            command.params.forEach { (paramName, parameter) ->
                append(PARAM_PREFIX)
                append(paramName)
                append(PARAMETER_TYPE_PREFIX)
                appendQuoted(parameter.type)
                append(PARAMETER_REQUIRED_PREFIX)
                append(parameter.required)
                append(PARAMETER_CONSTRAINTS_PREFIX)
                append(parameter.constraints)
                append(CLOSE_PAREN)
            }
            append(PARAMS_END)
            append(COMMAND_END)
        }
        append(MAP_END)
    }

    private fun StringBuilder.appendNullableQuoted(value: String?) {
        if (value == null) {
            append(NULL_VALUE)
        } else {
            appendQuoted(value)
        }
    }

    private fun StringBuilder.appendQuoted(value: String) {
        append('\'')
        append(value)
        append('\'')
    }

    private fun String?.nullableStringLength(): Int = this?.length ?: NULL_VALUE.length

    private companion object {
        private const val HEADER_PREFIX = "Validation failed for "
        private const val HEADER_SUFFIX = " while parsing feature:"
        private const val VALIDATION_ERRORS_HEADER = "\nValidation errors:"
        private const val VALIDATION_ERROR_PREFIX = "\n - "
        private const val FEATURE_START = "\nFeature("
        private const val FEATURE_NAME_PREFIX = "\n  feature = "
        private const val FEATURE_WILDCARD_PREFIX = "\n  wildcardFeature = "
        private const val FEATURE_DEVICE_ID_PREFIX = "\n  deviceId = "
        private const val FEATURE_GATEWAY_ID_PREFIX = "\n  gatewayId = "
        private const val FEATURE_IS_ACTIVE_PREFIX = "\n  isActive = "
        private const val FEATURE_IS_ENABLED_PREFIX = "\n  isEnabled = "
        private const val FEATURE_IS_READY_PREFIX = "\n  isReady = "
        private const val FEATURE_API_VERSION_PREFIX = "\n  apiVersion = "
        private const val FEATURE_TIMESTAMP_PREFIX = "\n  timestamp = "
        private const val FEATURE_URI_PREFIX = "\n  uri = "
        private const val PROPERTIES_START = "\n  properties = {"
        private const val COMMANDS_START = "\n  commands = {"
        private const val ITEM_PREFIX = "\n    "
        private const val PROPERTY_TYPE_PREFIX = " = Property(type = "
        private const val PROPERTY_VALUE_PREFIX = ", value = "
        private const val PROPERTY_UNIT_PREFIX = ", unit = "
        private const val COMMAND_START = " = Command("
        private const val COMMAND_URI_PREFIX = "\n      uri = "
        private const val COMMAND_NAME_PREFIX = "\n      name = "
        private const val COMMAND_IS_EXECUTABLE_PREFIX = "\n      isExecutable = "
        private const val PARAMS_START = "\n      params = {"
        private const val PARAM_PREFIX = "\n        "
        private const val PARAMETER_TYPE_PREFIX = " = Parameter(type = "
        private const val PARAMETER_REQUIRED_PREFIX = ", required = "
        private const val PARAMETER_CONSTRAINTS_PREFIX = ", constraints = "
        private const val PARAMS_END = "\n      }"
        private const val COMMAND_END = "\n    )"
        private const val MAP_END = "\n  }"
        private const val BLOCK_END = "\n)"
        private const val CLOSE_PAREN = ")"
        private const val NULL_VALUE = "null"

        private const val PROPERTY_CAPACITY_HEURISTIC = 96
        private const val COMMAND_CAPACITY_HEURISTIC = 192
        private const val PARAMETER_CAPACITY_HEURISTIC = 96

        private const val BASE_MESSAGE_CAPACITY =
            HEADER_PREFIX.length +
                HEADER_SUFFIX.length +
                VALIDATION_ERRORS_HEADER.length +
                FEATURE_START.length +
                FEATURE_NAME_PREFIX.length +
                FEATURE_WILDCARD_PREFIX.length +
                FEATURE_DEVICE_ID_PREFIX.length +
                FEATURE_GATEWAY_ID_PREFIX.length +
                FEATURE_IS_ACTIVE_PREFIX.length +
                FEATURE_IS_ENABLED_PREFIX.length +
                FEATURE_IS_READY_PREFIX.length +
                FEATURE_API_VERSION_PREFIX.length +
                FEATURE_TIMESTAMP_PREFIX.length +
                FEATURE_URI_PREFIX.length +
                PROPERTIES_START.length +
                COMMANDS_START.length +
                MAP_END.length * 2 +
                BLOCK_END.length
    }
}
