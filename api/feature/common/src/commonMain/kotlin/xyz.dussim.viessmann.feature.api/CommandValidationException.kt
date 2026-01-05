package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult

class CommandValidationException(
    val commandClass: String,
    val validationResult: ValidationResult<ValidationError>,
) : RuntimeException() {
    override val message: String by lazy {
        "Validation failed for $commandClass:\n${validationResult.asIterable().joinToString("\n") { " - $it" }}"
    }
}
