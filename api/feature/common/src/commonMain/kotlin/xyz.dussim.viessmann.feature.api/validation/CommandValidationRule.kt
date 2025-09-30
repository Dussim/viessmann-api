package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.Feature

interface CommandValidationRule {
    val commandName: String

    fun validateFromFeatureContext(feature: Feature): ValidationResult<ValidationError>
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun CommandValidationRule.invoke(feature: Feature): ValidationResult<ValidationError> = validateFromFeatureContext(feature)
