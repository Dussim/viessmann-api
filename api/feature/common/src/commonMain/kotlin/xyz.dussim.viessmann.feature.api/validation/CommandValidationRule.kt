package xyz.dussim.viessmann.feature.api.validation

import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Feature

interface CommandValidationRule : ValidationRule<Command, ValidationError> {
    val rule: ValidationRule<Feature, ValidationError>
}
