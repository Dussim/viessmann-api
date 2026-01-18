package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.logging.Logger

class FeatureInterfaceGenerator(
    private val packageName: String,
    private val logger: Logger,
) {
    private val sharedCommands = mutableMapOf<CommandSignature, ClassName>()

    fun useSharedCommands(commands: Map<CommandSignature, ClassName>) {
        sharedCommands.putAll(commands)
    }

    fun getCommandSignature(
        name: String,
        command: JsonObject,
    ): CommandSignature {
        val params =
            command["params"]?.jsonObject?.entries?.sortedBy { it.key }?.map { (pName, pParam) ->
                ParameterSignature(pName, pParam.jsonObject["type"]?.jsonPrimitive?.content ?: "")
            } ?: emptyList()
        return CommandSignature(name, params)
    }

    fun generate(feature: JsonObject): FileSpec {
        val featureName = feature["feature"]?.jsonPrimitive?.content ?: ""
        val interfaceName = featureToInterfaceName(featureName)
        val typeSpec =
            TypeSpec
                .interfaceBuilder(interfaceName)
                .addSuperinterface(ClassName("xyz.dussim.viessmann.feature.api", "Feature", "Device"))
                .addAnnotation(
                    AnnotationSpec
                        .builder(ClassName("xyz.dussim.viessmann.api.feature.annotations", "GenerateFeatureImplementation"))
                        .addMember("%S", featureName)
                        .build(),
                )

        // Add properties
        feature["properties"]?.jsonObject?.entries?.sortedBy { it.key }?.forEach { (name, property) ->
            val propObj = property.jsonObject
            if (isListEmpty(propObj)) {
                logger.warn("Feature property $featureName::$name is empty list, skipping it as unknown what kind of object it holds")
            } else {
                val typeName = mapPropertyToTypeName(propObj)
                typeSpec.addProperty(
                    PropertySpec
                        .builder(name, typeName)
                        .build(),
                )
            }
        }

        // Add commands
        feature["commands"]?.jsonObject?.entries?.sortedBy { it.key }?.forEach { (name, command) ->
            val commandObj = command.jsonObject
            val commandInterfaceName = name.replaceFirstChar { it.uppercaseChar() }
            val signature = getCommandSignature(name, commandObj)
            val sharedClassName = sharedCommands[signature]

            if (sharedClassName != null) {
                typeSpec.addProperty(
                    PropertySpec
                        .builder(name, sharedClassName)
                        .build(),
                )
            } else {
                val commandInterface = generateCommandInterface(commandInterfaceName, name, signature.parameters)
                typeSpec.addType(commandInterface)

                typeSpec
                    .addProperty(
                        PropertySpec
                            .builder(name, ClassName("", interfaceName, commandInterfaceName))
                            .build(),
                    )
            }
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

        params.forEachIndexed { index, param ->
            typeSpec.addProperty(
                PropertySpec
                    .builder("constraint${index + 1}", mapParameterTypeToConstraintsTypeName(param.type), KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addStatement("return %N", param.name).build())
                    .addAnnotation(
                        AnnotationSpec
                            .builder(Deprecated::class.asClassName())
                            .addMember("message = \"Use '${param.name}' instead\"")
                            .addMember("replaceWith = %T(\"${param.name}\")", ReplaceWith::class.asClassName())
                            .addMember("level = %T.WARNING", DeprecationLevel::class.asClassName())
                            .build(),
                    ).build(),
            )
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

    private fun isListEmpty(property: JsonObject): Boolean {
        val value = property["value"]
        return value is JsonArray && value.isEmpty()
    }

    private fun mapPropertyToTypeName(property: JsonObject): TypeName {
        val type = property["type"]?.jsonPrimitive?.content ?: ""
        val value = property["value"]
        return when (type) {
            "string" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "StringValue")
            }

            "number" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "DoubleValue")
            }

            "boolean" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "BooleanValue")
            }

            "array" -> {
                val array = value as? JsonArray
                if (array.isNullOrEmpty()) {
                    ClassName("xyz.dussim.viessmann.feature.api", "ListEmptyValue")
                } else {
                    guessArrayClassName(array)
                }
            }

            "object" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "ObjectOtherRoomConfigurationValue")
            }

            "Schedule" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "ScheduleValue")
            }

            "DeviceList" -> {
                ClassName("xyz.dussim.viessmann.feature.api", "ListDeviceValue")
            }

            else -> {
                ClassName("xyz.dussim.viessmann.feature.api", "UnknownValue")
            }
        }
    }

    private fun guessArrayClassName(array: JsonArray): ClassName {
        val first = array.first()
        if (first is JsonObject) {
            if (first.containsKey("errorCode")) return ClassName("xyz.dussim.viessmann.feature.api", "ListDeviceErrorValue")
            if (first.containsKey("modelId")) return ClassName("xyz.dussim.viessmann.feature.api", "ListDeviceValue")
            if (first.containsKey("deviceId")) return ClassName("xyz.dussim.viessmann.feature.api", "ListRoomActorValue")
            if (first.containsKey("device")) return ClassName("xyz.dussim.viessmann.feature.api", "ListZigbeeDeviceStatusValue")
        } else if (first is kotlinx.serialization.json.JsonPrimitive) {
            if (first.isString) return ClassName("xyz.dussim.viessmann.feature.api", "ListStringValue")
            return ClassName("xyz.dussim.viessmann.feature.api", "ListDoubleValue")
        }
        return ClassName("xyz.dussim.viessmann.feature.api", "UnknownValue")
    }
}

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
