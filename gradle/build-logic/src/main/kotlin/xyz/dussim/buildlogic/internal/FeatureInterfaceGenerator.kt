package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
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
        val className = featureToInterfaceName(featureName)

        val properties =
            feature["properties"]?.jsonObject?.entries?.sortedBy { it.key }?.mapNotNull { (name, property) ->
                val propObj = property.jsonObject
                if (isListEmpty(propObj)) {
                    logger.warn("Feature property $featureName::$name is empty list, skipping it as unknown what kind of object it holds")
                    null
                } else {
                    PropertyModel(name, mapPropertyToTypeName(propObj))
                }
            } ?: emptyList()

        val commands =
            feature["commands"]?.jsonObject?.entries?.sortedBy { it.key }?.map { (name, command) ->
                val commandObj = command.jsonObject
                val signature = getCommandSignature(name, commandObj)
                CommandModel(
                    name = name,
                    signature = signature,
                )
            } ?: emptyList()

        val model =
            FeatureModel(
                featureName = featureName,
                className = className,
                properties = properties,
                commands = commands,
            )

        return buildFeatureInterface(model, packageName, sharedCommands)
    }

    private fun featureToInterfaceName(featureName: String): String {
        val name =
            featureName
                .replace("{}", "N")
                .split(".")
                .joinToString("") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
        return if (name.endsWith("Feature")) name else "${name}Feature"
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
