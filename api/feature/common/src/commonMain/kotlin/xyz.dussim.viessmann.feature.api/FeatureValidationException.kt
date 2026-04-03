package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult

class FeatureValidationException(
    val featureClass: String,
    val validationResult: ValidationResult<ValidationError>,
    val featureName: String? = null,
) : RuntimeException() {
    override val message: String by lazy {
        val header =
            if (featureName != null) {
                "Validation failed for $featureClass while parsing feature '$featureName'"
            } else {
                "Validation failed for $featureClass"
            }
        "$header:\n${validationResult.asIterable().joinToString("\n") { " - $it" }}"
    }
}
