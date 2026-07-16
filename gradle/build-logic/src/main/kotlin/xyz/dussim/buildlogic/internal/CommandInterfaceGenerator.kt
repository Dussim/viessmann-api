package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

object CommandInterfaceGenerator {
    fun generateCommandInterface(
        name: String,
        commandName: String,
        params: List<ParameterSignature>,
        featureName: String = "",
    ): TypeSpec {
        val commandClassName =
            when (params.size) {
                in 0..8 -> {
                    ClassName("xyz.dussim.viessmann.feature.api", "Command${params.size}")
                }

                else -> {
                    error(
                        "Too many parameters (${params.size}) for command '$commandName'" +
                            (if (featureName.isNotEmpty()) " in feature '$featureName'" else "") +
                            ". Max supported: 8. Params: ${params.map { it.name }}",
                    )
                }
            }

        val typeSpec =
            TypeSpec
                .interfaceBuilder(name)
                .addAnnotation(
                    AnnotationSpec
                        .builder(ClassName("xyz.dussim.viessmann.api.feature.annotations", "CommandName"))
                        .addMember("%S", commandName)
                        .build(),
                ).addType(TypeSpec.companionObjectBuilder().build())

        if (params.isNotEmpty()) {
            val typeArguments = params.map { mapParameterTypeToType(it.type) }
            typeSpec.addSuperinterface(commandClassName.parameterizedBy(typeArguments))
        } else {
            typeSpec.addSuperinterface(commandClassName)
        }

        params.forEach { param ->
            typeSpec.addProperty(
                PropertySpec
                    .builder(param.name, mapParameterTypeToConstraintsTypeName(param.type))
                    .build(),
            )
        }

        return typeSpec.build()
    }

    private fun mapParameterTypeToType(type: String): TypeName =
        when (type) {
            "string" -> {
                String::class.asTypeName()
            }

            "number" -> {
                Double::class.asTypeName()
            }

            "boolean" -> {
                Boolean::class.asTypeName()
            }

            "Schedule" -> {
                val scheduleClass = ClassName("xyz.dussim.viessmann.feature.api", "Schedule")
                val listClass = List::class.asClassName()
                val mapClass = Map::class.asClassName()
                mapClass.parameterizedBy(
                    String::class.asTypeName(),
                    listClass.parameterizedBy(scheduleClass),
                )
            }

            "EnergyMatrix" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "EnergyMatrix")
            }

            "array:number" -> {
                List::class.asClassName().parameterizedBy(Double::class.asTypeName())
            }

            "array:string" -> {
                List::class.asClassName().parameterizedBy(String::class.asTypeName())
            }

            "array:boolean" -> {
                List::class.asClassName().parameterizedBy(Boolean::class.asTypeName())
            }

            "array:object" -> {
                val jsonObjectClass = ClassName("kotlinx.serialization.json", "JsonObject")
                List::class.asClassName().parameterizedBy(jsonObjectClass)
            }

            "object" -> {
                ClassName("kotlinx.serialization.json", "JsonObject")
            }

            else -> {
                error("Unknown parameter type: $type")
            }
        }

    private fun mapParameterTypeToConstraintsTypeName(type: String): TypeName =
        when (type) {
            "string" -> ClassName("xyz.dussim.viessmann.feature.api", "StringConstraints")
            "number" -> ClassName("xyz.dussim.viessmann.feature.api", "NumberConstraints")
            "boolean" -> ClassName("xyz.dussim.viessmann.feature.api", "BooleanConstraints")
            "array:number" -> ClassName("xyz.dussim.viessmann.feature.api", "ArrayNumberConstraints")
            "array:string" -> ClassName("xyz.dussim.viessmann.feature.api", "ArrayStringConstraints")
            "array:boolean" -> ClassName("xyz.dussim.viessmann.feature.api", "ArrayBooleanConstraints")
            "array:object" -> ClassName("xyz.dussim.viessmann.feature.api", "ArrayObjectConstraints")
            "object" -> ClassName("xyz.dussim.viessmann.feature.api", "ObjectConstraints")
            "Schedule" -> ClassName("xyz.dussim.viessmann.feature.api", "ScheduleConstraints")
            "EnergyMatrix" -> ClassName("xyz.dussim.viessmann.feature.api", "EnergyMatrixConstraints")
            else -> error("Unknown parameter type: $type")
        }
}

fun identifySharedCommands(
    allSignatures: List<CommandSignature>,
    packageName: String,
): Map<CommandSignature, ClassName> {
    val shareableSignatures =
        allSignatures
            .groupBy { it }
            .filter { it.value.size > 1 }
            .map { it.key }

    val allUniqueSignatures = allSignatures.distinct()
    val signaturesByName = allUniqueSignatures.groupBy { it.name }

    val sharedCommandsPackage = "$packageName.commands"
    val result =
        shareableSignatures
            .associateWith { sig ->
                val useShortName = (signaturesByName[sig.name]?.size ?: 0) == 1
                val interfaceName = if (useShortName) sig.capitalizedName else sig.interfaceName()
                ClassName(sharedCommandsPackage, interfaceName)
            }.toMutableMap()

    // Fix collisions where different signatures map to the same ClassName
    val collisions = result.entries.groupBy { it.value }.filter { it.value.size > 1 }
    for ((_, entries) in collisions) {
        for (entry in entries) {
            result[entry.key] = ClassName(sharedCommandsPackage, entry.key.interfaceName(forceFullParamNames = true))
        }
    }

    return result
}
