package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
    ): TypeSpec {
        val commandClassName =
            when (params.size) {
                0 -> ClassName("xyz.dussim.viessmann.feature.api", "Command0")
                1 -> ClassName("xyz.dussim.viessmann.feature.api", "Command1")
                2 -> ClassName("xyz.dussim.viessmann.feature.api", "Command2")
                3 -> ClassName("xyz.dussim.viessmann.feature.api", "Command3")
                4 -> ClassName("xyz.dussim.viessmann.feature.api", "Command4")
                5 -> ClassName("xyz.dussim.viessmann.feature.api", "Command5")
                6 -> ClassName("xyz.dussim.viessmann.feature.api", "Command6")
                else -> error("Too many parameters for command")
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

            else -> {
                error("Unknown parameter type: $type")
            }
        }

    private fun mapParameterTypeToConstraintsTypeName(type: String): TypeName =
        when (type) {
            "string" -> ClassName("xyz.dussim.viessmann.feature.api", "StringConstraints")
            "number" -> ClassName("xyz.dussim.viessmann.feature.api", "NumberConstraints")
            "boolean" -> ClassName("xyz.dussim.viessmann.feature.api", "BooleanConstraints")
            "Schedule" -> ClassName("xyz.dussim.viessmann.feature.api", "ScheduleConstraints")
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
