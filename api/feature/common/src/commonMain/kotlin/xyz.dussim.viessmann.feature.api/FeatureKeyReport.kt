package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.json.JsonObject

data class FeatureKeyReport(
    val feature: String,
    val wildcardFeature: String,
    val propertyKeys: List<String>,
    val commands: List<CommandKeyReport>,
) {
    val commandKeys: List<String> get() = commands.map { it.name }
}

data class CommandKeyReport(
    val name: String,
    val parameters: List<CommandParameterKeyReport>,
) {
    val parameterKeys: List<String> get() = parameters.map { it.name }
}

data class CommandParameterKeyReport(
    val name: String,
    val constraintKeys: List<String>,
)

fun Feature.toKeyReport(): FeatureKeyReport =
    FeatureKeyReport(
        feature = feature,
        wildcardFeature = wildcardFeature,
        propertyKeys = properties.keys.sorted(),
        commands =
            commands.entries
                .map { (commandName, command) ->
                    CommandKeyReport(
                        name = commandName,
                        parameters =
                            command.params.entries
                                .map { (parameterName, parameter) ->
                                    CommandParameterKeyReport(
                                        name = parameterName,
                                        constraintKeys = parameter.constraints.keyNames(),
                                    )
                                }.sortedBy { it.name },
                    )
                }.sortedBy { it.name },
    )

fun Iterable<Feature>.toKeyReports(): List<FeatureKeyReport> =
    map { it.toKeyReport() }
        .sortedWith(compareBy<FeatureKeyReport> { it.wildcardFeature }.thenBy { it.feature })

fun FeatureKeyReport.toKeyLogString(): String =
    buildString {
        appendLine(feature)
        if (feature != wildcardFeature) {
            appendLine("  wildcard: $wildcardFeature")
        }
        appendLine("  properties: ${propertyKeys.joinKeys()}")
        appendLine("  commands: ${commandKeys.joinKeys()}")
        if (commands.isNotEmpty()) {
            appendLine("  command params:")
            commands.forEach { command ->
                appendLine("    ${command.name}: ${command.parameterKeys.joinKeys()}")
            }
            appendLine("  command constraints:")
            commands.forEach { command ->
                if (command.parameters.isEmpty()) {
                    appendLine("    ${command.name}: <none>")
                } else {
                    command.parameters.forEach { parameter ->
                        appendLine("    ${command.name}.${parameter.name}: ${parameter.constraintKeys.joinKeys()}")
                    }
                }
            }
        }
    }.trimEnd()

fun Iterable<FeatureKeyReport>.toKeyLogString(): String = joinToString(separator = "\n\n") { it.toKeyLogString() }

fun Iterable<Feature>.toFeatureKeyLogString(): String = toKeyReports().toKeyLogString()

private fun Constraints<*>.keyNames(): List<String> =
    when (this) {
        BooleanConstraints -> {
            emptyList()
        }

        EnergyMatrixConstraints -> {
            emptyList()
        }

        is NumberConstraints -> {
            buildList {
                addIfPresent("min", min)
                addIfPresent("efficientLowerBorder", efficientLowerBorder)
                addIfPresent("efficientUpperBorder", efficientUpperBorder)
                addIfPresent("max", max)
                addIfPresent("stepping", stepping)
                addIfPresent("enum", enum)
            }
        }

        is StringConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("regEx", regEx)
                addIfPresent("enum", enum)
                addIfPresent("sameDayAllowed", sameDayAllowed)
            }
        }

        is ScheduleConstraints -> {
            listOf(
                "modes",
                "maxEntries",
                "resolution",
                "defaultMode",
                "overlapAllowed",
            )
        }

        is ArrayEmptyConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
            }
        }

        is ArrayNumberConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("enum", enum)
            }
        }

        is ArrayStringConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("enum", enum)
            }
        }

        is ArrayBooleanConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("enum", enum)
            }
        }

        is ArrayObjectConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("enum", enum)
            }
        }

        is ArrayUnknownConstraints -> {
            buildList {
                addIfPresent("minLength", minLength)
                addIfPresent("maxLength", maxLength)
                addIfPresent("enum", enum)
            }
        }

        is ObjectConstraints -> {
            buildList {
                addIfPresent("minProperties", minProperties)
                addIfPresent("maxProperties", maxProperties)
                addIfPresent("required", required)
                addIfPresent("additionalProperties", additionalProperties)
            }
        }

        is UnknownConstraints -> {
            (jsonElement as? JsonObject)?.keys.orEmpty().sorted()
        }
    }

private fun MutableList<String>.addIfPresent(
    key: String,
    value: Any?,
) {
    if (value != null) add(key)
}

private fun List<String>.joinKeys(): String = if (isEmpty()) "<none>" else joinToString()
