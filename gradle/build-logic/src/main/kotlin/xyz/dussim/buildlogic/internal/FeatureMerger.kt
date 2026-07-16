package xyz.dussim.buildlogic.internal

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
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
                    if (key == "feature") put(key, collapsedName) else put(key, value)
                }
            }
        }

        val mergedProperties = mergeByObjectKey(features, "properties", ::isListEmpty)
        val mergedCommands = mergeByObjectKey(features, "commands", ::isParamsEmpty)
        val mergedMetadataProperties = mergeMetadataArray(features, "properties") { false }
        val mergedMetadataCommands = mergeMetadataArray(features, "commands", ::isMetadataParametersEmpty)

        return buildJsonObject {
            first.forEach { (key, value) ->
                when (key) {
                    "feature" -> {
                        put(key, collapsedName)
                    }

                    "properties" -> {
                        put(key, JsonObject(mergedProperties))
                    }

                    "commands" -> {
                        put(key, JsonObject(mergedCommands))
                    }

                    "_metadata" -> {
                        put(
                            key,
                            buildJsonObject {
                                put("properties", JsonArray(mergedMetadataProperties.values.toList()))
                                put("commands", JsonArray(mergedMetadataCommands.values.toList()))
                            },
                        )
                    }

                    else -> {
                        put(key, value)
                    }
                }
            }
        }
    }

    /**
     * Folds the top-level `properties`/`commands` JSON object from every feature into a single map keyed by name.
     * If [isExisting] reports the already-collected entry as "empty" and the new entry is non-empty, it replaces it.
     */
    private fun mergeByObjectKey(
        features: List<JsonObject>,
        key: String,
        isExisting: (JsonObject) -> Boolean,
    ): Map<String, JsonObject> {
        val result = linkedMapOf<String, JsonObject>()
        features.forEach { feature ->
            feature[key]?.jsonObject?.forEach { (name, value) ->
                val obj = value.jsonObject
                val existing = result[name]
                if (existing == null || (isExisting(existing) && !isExisting(obj))) {
                    result[name] = obj
                }
            }
        }
        return result
    }

    /**
     * Folds an array under `_metadata.<key>` from every feature, deduplicating by the `name` field.
     * If [isEmpty] reports the existing entry as empty and the new one is non-empty, it replaces it.
     */
    private fun mergeMetadataArray(
        features: List<JsonObject>,
        key: String,
        isEmpty: (JsonObject) -> Boolean,
    ): Map<String, JsonObject> {
        val result = linkedMapOf<String, JsonObject>()
        features.forEach { feature ->
            feature["_metadata"]?.jsonObject?.get(key)?.jsonArray?.forEach { element ->
                val obj = element.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@forEach
                val existing = result[name]
                if (existing == null || (isEmpty(existing) && !isEmpty(obj))) {
                    result[name] = obj
                }
            }
        }
        return result
    }

    private fun isListEmpty(property: JsonObject): Boolean {
        val value = property["value"]
        return value is JsonArray && value.isEmpty()
    }

    private fun isParamsEmpty(command: JsonObject): Boolean = command["params"]?.jsonObject.isNullOrEmpty()

    private fun isMetadataParametersEmpty(command: JsonObject): Boolean = command["parameters"]?.jsonArray.isNullOrEmpty()

    private fun String.toWildcardFeature(): String =
        split(".").joinToString(".") { segment ->
            if (segment.isNotEmpty() && segment.all { it.isDigit() }) {
                "{}"
            } else {
                segment.replace(embeddedIndexPattern, "{}")
            }
        }

    private val embeddedIndexPattern = "(?<!_)\\d+(?=[A-Z])".toRegex()
}
