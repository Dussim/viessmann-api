package xyz.dussim.buildlogic

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.api.logging.Logger
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.Command4
import xyz.dussim.viessmann.feature.api.Command5
import xyz.dussim.viessmann.feature.api.Command6
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.Parameter
import xyz.dussim.viessmann.feature.api.Schedule
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints

class FeatureInterfaceGenerator(
    private val packageName: String,
    private val logger: Logger,
) {
    fun generate(feature: DeviceFeature): FileSpec {
        val interfaceName = featureToInterfaceName(feature.feature)
        val typeSpec =
            TypeSpec
                .interfaceBuilder(interfaceName)
                .addSuperinterface(Feature.Device::class)
                .addAnnotation(
                    AnnotationSpec
                        .builder(GenerateFeatureImplementation::class)
                        .addMember("%S", feature.feature)
                        .build(),
                )

        // Add properties
        feature.properties.entries.sortedBy { it.key }.forEach { (name, property) ->
            val typeName = property.value::class.asClassName()
            if (property.value is ListEmptyValue) {
                logger.warn("Property ${feature.feature}.$name is empty list, skipping it as unknown what kind of object it holds")
            } else {
                typeSpec.addProperty(
                    PropertySpec
                        .builder(name, typeName)
                        .build(),
                )
            }
        }

        // Add commands
        feature.commands.entries.sortedBy { it.key }.forEach { (name, command) ->
            val commandInterfaceName = name.replaceFirstChar { it.uppercaseChar() }
            val commandInterface = generateCommandInterface(commandInterfaceName, command)
            typeSpec.addType(commandInterface)

            typeSpec
                .addProperty(
                    PropertySpec
                        .builder(name, ClassName("", interfaceName, commandInterfaceName))
                        .build(),
                )
        }

        return FileSpec
            .builder(packageName, interfaceName)
            .addType(
                typeSpec
                    .addType(TypeSpec.companionObjectBuilder().build())
                    .build(),
            ).build()
    }

    private fun featureToInterfaceName(featureName: String): String {
        val name =
            featureName
                .replace("{}", "N")
                .split(".")
                .joinToString("") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
        return if (name.endsWith("Feature")) name else "${name}Feature"
    }

    private fun generateCommandInterface(
        name: String,
        command: Command,
    ): TypeSpec {
        val params = command.params.entries.sortedBy { it.key }
        val commandClassName =
            when (params.size) {
                0 -> Command0::class.asClassName()
                1 -> Command1::class.asClassName()
                2 -> Command2::class.asClassName()
                3 -> Command3::class.asClassName()
                4 -> Command4::class.asClassName()
                5 -> Command5::class.asClassName()
                6 -> Command6::class.asClassName()
                else -> error("Too many parameters for command")
            }

        val typeSpec =
            TypeSpec
                .interfaceBuilder(name)
                .addType(TypeSpec.companionObjectBuilder().build())

        if (params.isNotEmpty()) {
            val typeArguments = params.map { mapParameterToType(it.value) }
            typeSpec.addSuperinterface(commandClassName.parameterizedBy(typeArguments))
        } else {
            typeSpec.addSuperinterface(commandClassName)
        }

        params.forEach { (paramName, parameter) ->
            typeSpec.addProperty(
                PropertySpec
                    .builder(paramName, mapParameterToConstraintsTypeName(parameter))
                    .build(),
            )
        }

        return typeSpec.build()
    }

    private fun mapParameterToType(parameter: Parameter): TypeName =
        when (parameter.type) {
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
                val scheduleClass = Schedule::class.asClassName()
                val listClass = List::class.asClassName()
                val mapClass = Map::class.asClassName()
                mapClass.parameterizedBy(
                    String::class.asTypeName(),
                    listClass.parameterizedBy(scheduleClass),
                )
            }

            else -> {
                error("Unknown parameter type: ${parameter.type}")
            }
        }

    private fun mapParameterToConstraintsTypeName(parameter: Parameter): TypeName =
        when (parameter.type) {
            "string" -> StringConstraints::class.asClassName()
            "number" -> NumberConstraints::class.asClassName()
            "boolean" -> BooleanConstraints::class.asClassName()
            "Schedule" -> ScheduleConstraints::class.asClassName()
            else -> error("Unknown parameter type: ${parameter.type}")
        }
}
