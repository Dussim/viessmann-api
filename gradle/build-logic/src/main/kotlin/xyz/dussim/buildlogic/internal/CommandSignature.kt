package xyz.dussim.buildlogic.internal

data class CommandSignature(
    val name: String,
    val parameters: List<ParameterSignature>,
) {
    val capitalizedName = name.replaceFirstChar { it.uppercaseChar() }

    fun interfaceName(forceFullParamNames: Boolean = false): String {
        if (parameters.isEmpty()) return capitalizedName
        val paramsPart =
            parameters.joinToString("") { p ->
                val typePart =
                    when (p.type) {
                        "string" -> "String"
                        "number" -> "Double"
                        "boolean" -> "Boolean"
                        "array:number" -> "ArrayDouble"
                        "array:string" -> "ArrayString"
                        "array:boolean" -> "ArrayBoolean"
                        "array:object" -> "ArrayObject"
                        "object" -> "Object"
                        "Schedule" -> "Schedule"
                        else -> p.type.replaceFirstChar { it.uppercase() }
                    }
                val pName = p.name.replaceFirstChar { it.uppercase() }
                if (!forceFullParamNames && (pName == "Value" || capitalizedName.endsWith(pName, ignoreCase = true))) {
                    typePart
                } else {
                    pName + typePart
                }
            }
        return "$capitalizedName$paramsPart"
    }
}

data class ParameterSignature(
    val name: String,
    val type: String,
) {
    companion object {
        fun normalizeCommandParameterType(
            type: String,
            featureName: String = "",
            commandName: String = "",
            parameterName: String = "",
        ): String =
            when (type) {
                "boolean" -> "boolean"
                "number", "integer" -> "number"
                "string" -> "string"
                "array", "array:number", "array:string", "array:boolean", "array:object" -> type
                "object" -> "object"
                "Schedule" -> "Schedule"
                "EnergyMatrix" -> "EnergyMatrix"
                else -> unsupportedCommandParameterType(featureName, commandName, parameterName, type)
            }

        fun unsupportedCommandParameterType(
            featureName: String,
            commandName: String,
            parameterName: String,
            type: String,
        ): Nothing =
            error(
                buildString {
                    append("Unsupported command parameter type '$type'")
                    if (featureName.isNotBlank()) append(" in feature '$featureName'")
                    if (commandName.isNotBlank()) append(", command '$commandName'")
                    if (parameterName.isNotBlank()) append(", parameter '$parameterName'")
                },
            )
    }
}
