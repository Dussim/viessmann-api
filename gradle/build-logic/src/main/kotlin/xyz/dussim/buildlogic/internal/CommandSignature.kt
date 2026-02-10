package xyz.dussim.buildlogic.internal

data class CommandSignature(
    val name: String,
    val parameters: List<ParameterSignature>,
) {
    val capitalizedName = name.replaceFirstChar { it.uppercaseChar() }

    val interfaceName: String
        get() {
            if (parameters.isEmpty()) return capitalizedName
            val paramsPart =
                parameters.joinToString("") { p ->
                    val typePart =
                        when (p.type) {
                            "string" -> "String"
                            "number" -> "Double"
                            "boolean" -> "Boolean"
                            "Schedule" -> "Schedule"
                            else -> p.type.replaceFirstChar { it.uppercase() }
                        }
                    val pName = p.name.replaceFirstChar { it.uppercase() }
                    if (pName == "Value" || capitalizedName.endsWith(pName, ignoreCase = true)) {
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
)
