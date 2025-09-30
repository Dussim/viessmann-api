package xyz.dussim.viessmann.feature.api

class CommandValidationException(
    val commandClass: String,
    val errors: List<String>,
) : IllegalArgumentException(
        """
Validation failed for $commandClass:
${errors.joinToString("\n") { " - $it" }}
        """.trimIndent(),
    )
