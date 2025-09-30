package xyz.dussim.viessmann.feature.api

class FeatureValidationException(
    val featureClass: String,
    val errors: List<String>,
) : IllegalArgumentException(
        """
Validation failed for $featureClass:
${errors.joinToString("\n"){" - $it"}}
        """.trimIndent(),
    )
