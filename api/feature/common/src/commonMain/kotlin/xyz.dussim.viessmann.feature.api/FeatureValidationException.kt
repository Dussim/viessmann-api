package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult

class FeatureValidationException(
    val featureClass: String,
    val validationResult: ValidationResult<ValidationError>,
) : RuntimeException() {
    override val message: String by lazy {
        "Validation failed for $featureClass:\n${validationResult.asIterable().joinToString("\n") { " - $it" }}"
    }
}
