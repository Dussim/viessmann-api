package xyz.dussim.buildlogic.internal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object FeatureMerger {
    fun mergeAll(features: List<JsonObject>): List<JsonObject> =
        features
            .groupBy { feature ->
                val featureName = feature["feature"]?.jsonPrimitive?.content ?: ""
                featureName.toWildcardFeature()
            }.map { (collapsedName, featuresToMerge) ->
                mergeFeatures(featuresToMerge, collapsedName)
            }

    private fun mergeFeatures(
        features: List<JsonObject>,
        collapsedName: String,
    ): JsonObject {
        val first = features.first()
        if (features.size == 1) {
            return buildJsonObject {
                first.forEach { (key, value) ->
                    if (key == "feature") {
                        put(key, collapsedName)
                    } else {
                        put(key, value)
                    }
                }
            }
        }

        val allProperties = mutableMapOf<String, JsonObject>()
        val allCommands = mutableMapOf<String, JsonObject>()

        features.forEach { feature ->
            feature["properties"]?.jsonObject?.forEach { (name, property) ->
                val propObj = property.jsonObject
                val existing = allProperties[name]
                if (existing == null || (isListEmpty(existing) && !isListEmpty(propObj))) {
                    allProperties[name] = propObj
                }
            }
            feature["commands"]?.jsonObject?.forEach { (name, command) ->
                val cmdObj = command.jsonObject
                val existing = allCommands[name]
                if (existing == null || (isParamsEmpty(existing) && !isParamsEmpty(cmdObj))) {
                    allCommands[name] = cmdObj
                }
            }
        }

        return buildJsonObject {
            first.forEach { (key, value) ->
                when (key) {
                    "feature" -> put(key, collapsedName)
                    "properties" -> put(key, buildJsonObject { allProperties.forEach { (k, v) -> put(k, v) } })
                    "commands" -> put(key, buildJsonObject { allCommands.forEach { (k, v) -> put(k, v) } })
                    else -> put(key, value)
                }
            }
        }
    }

    private fun isListEmpty(property: JsonObject): Boolean {
        val value = property["value"]
        return value is JsonArray && value.isEmpty()
    }

    private fun isParamsEmpty(command: JsonObject): Boolean {
        val params = command["params"]?.jsonObject
        return params.isNullOrEmpty()
    }

    private fun String.toWildcardFeature(): String =
        split(".").joinToString(".") { segment ->
            if (segment.isNotEmpty() && segment.all { it.isDigit() }) "{}" else segment
        }
}
