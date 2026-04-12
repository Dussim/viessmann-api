package xyz.dussim.buildlogic.internal

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import org.gradle.api.logging.Logger
import java.io.File

/**
 * Generates valid JSON instances for each feature defined in YAML OpenAPI specs.
 * Each JSON represents a single valid feature response using example values from the schema.
 */
class YamlFeatureJsonGenerator(
    private val logger: Logger,
) {
    data class GenerationResult(
        val featureName: String,
        val json: JsonObject,
        val removalDate: java.time.LocalDate? = null,
    )

    fun generateJsonForFile(file: File): GenerationResult? =
        try {
            val api = parseYaml(file) ?: return null
            // Pre-load the raw YAML for the current file so $ref resolution can find sibling schemas
            val rawYaml = loadRawYaml(file)
            currentFileYamlKey = if (rawYaml != null) file.absolutePath else null
            val (featureName, responseSchema) = extractFeatureSchema(api) ?: return null
            val json = generateFeatureJson(api, featureName, responseSchema, file.parentFile)
            val removalDate = extractRemovalDate(api)
            GenerationResult(featureName, json, removalDate)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn("FAIL generating JSON for ${file.name}: ${e::class.simpleName}: ${e.message}")
            null
        }

    private fun extractRemovalDate(api: OpenAPI): java.time.LocalDate? {
        val pathItem = api.paths.values.firstOrNull { it.get != null } ?: return null
        val getOp = pathItem.get ?: return null
        if (getOp.deprecated != true) return null

        val deprecationInfo = getOp.extensions?.get("x-deprecation-info") as? Map<*, *>
        val removalDateString = deprecationInfo?.get("removal-date")?.toString() ?: return null

        return try {
            java.time.LocalDate.parse(removalDateString)
        } catch (e: java.time.format.DateTimeParseException) {
            logger.warn("Invalid removal-date format: $removalDateString. Expected YYYY-MM-DD.")
            null
        }
    }

    private fun parseYaml(file: File): OpenAPI? {
        val parseOptions =
            ParseOptions().apply {
                isResolve = true
                isResolveFully = true
            }
        return OpenAPIV3Parser().readLocation(file.toURI().toString(), null, parseOptions).openAPI
    }

    private fun extractFeatureSchema(api: OpenAPI): Pair<String, Schema<*>>? {
        val (path, pathItem) =
            api.paths.entries.firstOrNull { !it.key.contains("/commands/") } ?: return null
        val getOp = pathItem.get ?: return null

        val featureName = extractFeatureNameFromPath(path) ?: return null
        val responseSchema =
            getOp.responses
                ?.get("200")
                ?.content
                ?.get("application/json")
                ?.schema ?: return null

        return featureName to responseSchema
    }

    private fun extractFeatureNameFromPath(path: String): String? {
        val prefix = "/features/"
        val idx = path.indexOf(prefix)
        if (idx == -1) return null
        val afterFeatures = path.substring(idx + prefix.length)
        return afterFeatures.split("/").first().replace("{N}", "{}")
    }

    private fun generateFeatureJson(
        api: OpenAPI,
        featureName: String,
        responseSchema: Schema<*>,
        baseDir: File,
    ): JsonObject {
        val resolved = resolveSchemaComposition(responseSchema, featureName)
        return schemaToJson(resolved, featureName, api, baseDir) as? JsonObject ?: buildDefaultFeatureJson(featureName)
    }

    private fun buildDefaultFeatureJson(featureName: String): JsonObject =
        JsonObject(
            mapOf(
                "feature" to JsonPrimitive(featureName),
                "gatewayId" to JsonPrimitive("3837105891312345"),
                "deviceId" to JsonPrimitive("0"),
                "timestamp" to JsonPrimitive("2024-01-01T00:00:00.000Z"),
                "isEnabled" to JsonPrimitive(true),
                "isReady" to JsonPrimitive(true),
                "apiVersion" to JsonPrimitive(1),
                "uri" to JsonPrimitive("/v1/gateways/3837105891312345/devices/0/features/$featureName"),
                "properties" to JsonObject(emptyMap()),
                "commands" to JsonObject(emptyMap()),
            ),
        )

    private fun schemaToJson(
        schema: Schema<*>,
        featureName: String,
        api: OpenAPI,
        baseDir: File,
    ): JsonElement {
        val result = schemaToJsonInternal(schema, featureName, api, baseDir)
        return fixBooleanCoercion(result, schema)
    }

    /**
     * YAML 1.1 parses "off"/"on"/"yes"/"no" as booleans.
     * This recursively walks the schema and result tree, converting booleans back to strings
     * wherever the schema declares a string type.
     */
    private fun fixBooleanCoercion(
        element: JsonElement,
        schema: Schema<*>,
    ): JsonElement {
        // Direct string property coerced to boolean
        if (schema.type == "string" && element is JsonPrimitive && element.booleanOrNull != null) {
            return JsonPrimitive(element.content)
        }
        // Array with string items
        if (schema.type == "array" && schema.items?.type == "string" && element is JsonArray) {
            return JsonArray(
                element.map { item ->
                    if (item is JsonPrimitive && item.booleanOrNull != null) {
                        JsonPrimitive(item.content)
                    } else {
                        item
                    }
                },
            )
        }
        // Recurse into object properties
        if (element is JsonObject && schema.properties != null) {
            val fixed =
                element.mapValues { (key, value) ->
                    val propSchema = schema.properties?.get(key)
                    if (propSchema != null) fixBooleanCoercion(value, propSchema) else value
                }
            return JsonObject(fixed)
        }
        return element
    }

    @Suppress("CyclomaticComplexity")
    private fun schemaToJsonInternal(
        schema: Schema<*>,
        featureName: String,
        api: OpenAPI,
        baseDir: File,
    ): JsonElement {
        // If there's an example, use it directly
        schema.example?.let { example ->
            return exampleToJson(example, api, baseDir)
        }

        // If there's a const, use it
        schema.const?.let { constVal ->
            return exampleToJson(constVal, api, baseDir)
        }

        // If there's an enum, use the first value
        schema.enum?.firstOrNull()?.let { enumVal ->
            return exampleToJson(enumVal, api, baseDir)
        }

        return when (schema.type) {
            "object" -> {
                objectSchemaToJson(schema, featureName, api, baseDir)
            }

            "array" -> {
                arraySchemaToJson(schema, featureName, api, baseDir)
            }

            "string" -> {
                JsonPrimitive(schema.default?.toString() ?: "example-string")
            }

            "integer" -> {
                JsonPrimitive(schema.default?.toString()?.toIntOrNull() ?: 0)
            }

            "number" -> {
                JsonPrimitive(schema.default?.toString()?.toDoubleOrNull() ?: 0.0)
            }

            "boolean" -> {
                JsonPrimitive(schema.default?.toString()?.toBooleanStrictOrNull() ?: true)
            }

            null -> {
                // Try to infer from properties
                if (schema.properties != null) {
                    objectSchemaToJson(schema, featureName, api, baseDir)
                } else if (schema.items != null) {
                    arraySchemaToJson(schema, featureName, api, baseDir)
                } else {
                    JsonNull
                }
            }

            else -> {
                JsonNull
            }
        }
    }

    private fun objectSchemaToJson(
        schema: Schema<*>,
        featureName: String,
        api: OpenAPI,
        baseDir: File,
    ): JsonObject {
        val props = schema.properties ?: return JsonObject(emptyMap())
        val entries = mutableMapOf<String, JsonElement>()

        for ((name, propSchema) in props) {
            val resolved = resolveSchemaComposition(propSchema, featureName)
            entries[name] = schemaToJson(resolved, featureName, api, baseDir)
        }

        return JsonObject(entries)
    }

    private fun arraySchemaToJson(
        schema: Schema<*>,
        featureName: String,
        api: OpenAPI,
        baseDir: File,
    ): JsonArray {
        val items = schema.items ?: return JsonArray(emptyList())
        val resolved = resolveSchemaComposition(items, featureName)
        val element = schemaToJson(resolved, featureName, api, baseDir)
        return JsonArray(listOf(element))
    }

    private fun extractRef(example: Any?): String? =
        when (example) {
            is com.fasterxml.jackson.databind.node.ObjectNode -> {
                if (example.has("\$ref")) example.get("\$ref").textValue() else null
            }

            is Map<*, *> -> {
                example["\$ref"] as? String
            }

            else -> {
                null
            }
        }

    @Suppress("CyclomaticComplexity")
    private fun exampleToJson(
        example: Any?,
        api: OpenAPI,
        baseDir: File,
    ): JsonElement {
        extractRef(example)?.let { ref ->
            val resolvedExample = resolveExampleRef(ref, api, baseDir)
            if (resolvedExample != null) {
                return exampleToJson(resolvedExample, api, baseDir)
            } else {
                logger.warn("Could not resolve example ref: $ref")
                return JsonNull
            }
        }
        return when (example) {
            null -> {
                JsonNull
            }

            is Boolean -> {
                JsonPrimitive(example)
            }

            is Int -> {
                JsonPrimitive(example)
            }

            is Long -> {
                JsonPrimitive(example)
            }

            is Float -> {
                JsonPrimitive(example)
            }

            is Double -> {
                JsonPrimitive(example)
            }

            is java.math.BigDecimal -> {
                if (example.stripTrailingZeros().scale() <= 0) {
                    JsonPrimitive(example.longValueExact())
                } else {
                    JsonPrimitive(example.toDouble())
                }
            }

            is Number -> {
                JsonPrimitive(example.toDouble())
            }

            is String -> {
                JsonPrimitive(example)
            }

            is List<*> -> {
                // YAML 1.1 parses "off"/"on"/"yes"/"no" as booleans in string enum lists;
                // if the list contains a mix of strings and booleans, convert booleans to strings
                val hasStrings = example.any { it is String }
                val hasBooleans = example.any { it is Boolean }
                if (hasStrings && hasBooleans) {
                    JsonArray(
                        example.map { item ->
                            if (item is String) JsonPrimitive(item) else JsonPrimitive(item.toString())
                        },
                    )
                } else {
                    JsonArray(example.map { exampleToJson(it, api, baseDir) })
                }
            }

            is Map<*, *> -> {
                val entries = example.entries.associate { (k, v) -> k.toString() to exampleToJson(v, api, baseDir) }
                JsonObject(entries)
            }

            is com.fasterxml.jackson.databind.node.ArrayNode -> {
                JsonArray(example.map { exampleToJson(jacksonNodeToKotlin(it), api, baseDir) })
            }

            is com.fasterxml.jackson.databind.node.ObjectNode -> {
                val entries = mutableMapOf<String, JsonElement>()
                example.fields().forEach { (k, v) -> entries[k] = exampleToJson(jacksonNodeToKotlin(v), api, baseDir) }
                JsonObject(entries)
            }

            is com.fasterxml.jackson.databind.JsonNode -> {
                jacksonNodeToJsonElement(example)
            }

            is java.time.OffsetDateTime -> {
                JsonPrimitive(
                    java.time.format.DateTimeFormatter
                        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .format(example.withOffsetSameInstant(java.time.ZoneOffset.UTC)),
                )
            }

            is java.time.LocalDate -> {
                JsonPrimitive(example.toString())
            }

            else -> {
                JsonPrimitive(example.toString())
            }
        }
    }

    private fun resolveExampleRef(
        ref: String,
        api: OpenAPI,
        baseDir: File,
    ): Any? {
        val hashIdx = ref.indexOf("#")
        val filePath = if (hashIdx != -1) ref.substring(0, hashIdx) else ""
        val pointer = if (hashIdx != -1) ref.substring(hashIdx + 1) else ref

        val prefix = "/components/schemas/"
        val schemaName =
            if (pointer.contains(prefix)) {
                pointer.substring(pointer.lastIndexOf(prefix) + prefix.length)
            } else {
                pointer.split("/").last()
            }

        // Try current API first
        val schema = api.components?.schemas?.get(schemaName)
        if (schema != null && schema.example != null) return schema.example

        val entry =
            api.components
                ?.schemas
                ?.entries
                ?.firstOrNull { it.key.endsWith(schemaName) }
        if (entry != null && entry.value.example != null) return entry.value.example

        // Try external file if specified
        if (filePath.isNotEmpty()) {
            val externalFile = File(baseDir, filePath).canonicalFile
            if (externalFile.exists()) {
                val rawYaml = loadRawYaml(externalFile)
                val example = extractExampleFromRawYaml(rawYaml, schemaName)
                if (example != null) return example
            }
        } else if (currentFileYamlKey != null) {
            // Only search the YAML loaded for the current file, not all cached YAMLs
            val rawYaml = yamlCache[currentFileYamlKey]
            if (rawYaml != null) {
                val example = extractExampleFromRawYaml(rawYaml, schemaName)
                if (example != null) return example
            }
        }

        return null
    }

    /** Tracks which YAML cache key corresponds to the file currently being processed. */
    private var currentFileYamlKey: String? = null

    private val yamlCache = mutableMapOf<String, Map<String, Any?>>()

    private companion object {
        private val SCHEMA_INDICATOR_KEYS =
            setOf(
                "type",
                "properties",
                "items",
                "\$ref",
                "allOf",
                "oneOf",
                "anyOf",
                "enum",
                "const",
                "additionalProperties",
                "format",
                "default",
                "required",
            )
    }

    private fun loadRawYaml(file: File): Map<String, Any?>? {
        val cacheKey = file.absolutePath
        yamlCache[cacheKey]?.let { return it }

        return try {
            val mapper =
                com.fasterxml.jackson.databind
                    .ObjectMapper(
                        com.fasterxml.jackson.dataformat.yaml
                            .YAMLFactory(),
                    )

            @Suppress("UNCHECKED_CAST")
            val raw = mapper.readValue(file, Map::class.java) as Map<String, Any?>
            yamlCache[cacheKey] = raw
            raw
        } catch (e: Exception) {
            logger.debug("Failed to load raw YAML from ${file.name}: ${e.message}")
            null
        }
    }

    private fun extractExampleFromRawYaml(
        raw: Map<String, Any?>?,
        schemaName: String,
    ): Any? {
        val components = raw?.get("components") as? Map<*, *>
        val schemas = components?.get("schemas") as? Map<*, *>

        // Try exact match
        var schema = schemas?.get(schemaName) as? Map<*, *>
        if (schema == null) {
            // Try matching by the end of the key (e.g., "common-properties.yaml#/components/schemas/SchemaName")
            schema = schemas?.entries?.firstOrNull { (it.key as? String)?.endsWith(schemaName) == true }?.value as? Map<*, *>
        }

        if (schema != null) {
            // Check if there is a top-level example field
            schema["example"]?.let { return it }
            // Some schemas might have 'value' if it's a specific type of example
            schema["value"]?.let { return it }
            // Some "schemas" under components/schemas are actually raw example payloads
            // (no OpenAPI schema keywords). Recognize by the absence of schema-indicator keys
            // and return the entry itself as the example value.
            if (!schema.containsAnyKey(SCHEMA_INDICATOR_KEYS)) {
                return schema
            }
        }
        return null
    }

    private fun Map<*, *>.containsAnyKey(keys: Set<String>): Boolean = keys.any { containsKey(it) }

    // region Schema composition resolution (reused logic from YamlFeatureInterfaceGenerator)

    private fun resolveSchemaComposition(
        schema: Schema<*>,
        featureName: String,
    ): Schema<*> {
        val compositionParts = schema.allOf ?: schema.oneOf ?: schema.anyOf
        if (compositionParts != null) {
            return compositionParts
                .map { resolveSchemaComposition(it, featureName) }
                .fold(Schema<Any>()) { acc, next -> deepMergeSchemas(acc, next, featureName) }
        }

        val result = Schema<Any>()
        var hasContent = false

        schema.type?.let {
            result.type = it
            hasContent = true
        }
        schema.const?.let {
            result.const = it
            hasContent = true
        }
        schema.example?.let {
            result.example = it
            hasContent = true
        }

        @Suppress("UNCHECKED_CAST")
        (schema.enum as? MutableList<Any>)?.let {
            result.enum = it.toMutableList()
            hasContent = true
        }

        schema.properties?.let { props ->
            val mergedProps = mutableMapOf<String, Schema<*>>()
            for ((name, prop) in props) {
                val resolved = resolveSchemaComposition(prop, featureName)
                val existing = mergedProps[name]
                mergedProps[name] = if (existing != null) deepMergeSchemas(existing, resolved, featureName) else resolved
            }
            result.properties = mergedProps
            if (result.type == null) result.type = "object"
            hasContent = true
        }

        schema.required?.let {
            result.required = it
            hasContent = true
        }

        schema.items?.let { items ->
            result.items = resolveSchemaComposition(items, featureName)
            if (result.type == null) result.type = "array"
            hasContent = true
        }

        val addl = schema.additionalProperties
        if (addl is Schema<*>) {
            @Suppress("UNCHECKED_CAST")
            result.additionalProperties = resolveSchemaComposition(addl, featureName) as Schema<Any>
            if (result.type == null) result.type = "object"
            hasContent = true
        } else if (addl != null) {
            result.additionalProperties = addl
            hasContent = true
        }

        return if (hasContent) result else schema
    }

    private fun deepMergeSchemas(
        a: Schema<*>,
        b: Schema<*>,
        featureName: String,
    ): Schema<Any> {
        val left = resolveSchemaComposition(a, featureName)
        val right = resolveSchemaComposition(b, featureName)
        val merged = Schema<Any>()

        merged.type = mergeTypes(left.type, right.type)
        mergeProperties(left, right, featureName)?.let { merged.properties = it }
        mergeEnums(left, right)?.let { merged.enum = it }
        merged.example = mergeExample(left.example, right.example)
        mergeConst(left.const, right.const)?.let { merged.const = it }
        mergeItems(left.items, right.items, featureName)?.let { merged.items = it }
        mergeRequired(left.required, right.required)?.let { merged.required = it }
        mergeAdditionalProperties(left.additionalProperties, right.additionalProperties, featureName)?.let {
            merged.additionalProperties = it
        }

        if (merged.type == null) {
            when {
                merged.properties != null -> merged.type = "object"
                merged.items != null -> merged.type = "array"
                merged.additionalProperties is Schema<*> -> merged.type = "object"
            }
        }

        return merged
    }

    private fun mergeTypes(
        leftType: String?,
        rightType: String?,
    ): String? =
        when {
            leftType == "object" || rightType == "object" -> "object"
            leftType != null -> leftType
            else -> rightType
        }

    private fun mergeProperties(
        left: Schema<*>,
        right: Schema<*>,
        featureName: String,
    ): MutableMap<String, Schema<*>>? {
        val leftProps = left.properties ?: emptyMap()
        val rightProps = right.properties ?: emptyMap()
        if (leftProps.isEmpty() && rightProps.isEmpty()) return null

        val merged = LinkedHashMap<String, Schema<*>>(leftProps)
        for ((key, value) in rightProps) {
            val existing = merged[key]
            merged[key] = if (existing != null) deepMergeSchemas(existing, value, featureName) else value
        }
        return merged
    }

    private fun mergeEnums(
        left: Schema<*>,
        right: Schema<*>,
    ): MutableList<Any>? {
        val lEnum = left.enum
        val rEnum = right.enum
        if (lEnum == null && rEnum == null) return null

        val union =
            buildList {
                if (lEnum != null) addAll(lEnum)
                if (rEnum != null) addAll(rEnum)
            }.distinct()

        return if (union.isNotEmpty()) union.toMutableList() else null
    }

    private fun mergeExample(
        left: Any?,
        right: Any?,
    ): Any? =
        when {
            left != null && right != null && left == right -> left
            left != null -> left
            else -> right
        }

    private fun mergeConst(
        left: Any?,
        right: Any?,
    ): Any? =
        when {
            left != null && right != null -> if (left == right) left else null
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeItems(
        left: Schema<*>?,
        right: Schema<*>?,
        featureName: String,
    ): Schema<*>? =
        when {
            left != null && right != null -> deepMergeSchemas(left, right, featureName)
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeRequired(
        left: List<String>?,
        right: List<String>?,
    ): List<String>? =
        when {
            left != null && right != null -> left.intersect(right.toSet()).toList()
            left != null -> left
            right != null -> right
            else -> null
        }

    private fun mergeAdditionalProperties(
        left: Any?,
        right: Any?,
        featureName: String,
    ): Any? =
        when {
            left is Schema<*> && right is Schema<*> -> deepMergeSchemas(left, right, featureName)
            left is Schema<*> -> left
            right is Schema<*> -> right
            left != null -> left
            right != null -> right
            else -> null
        }

    // endregion

    private fun jacksonNodeToKotlin(node: com.fasterxml.jackson.databind.JsonNode): Any? =
        when {
            node.isNull -> {
                null
            }

            node.isBoolean -> {
                node.booleanValue()
            }

            node.isInt -> {
                node.intValue()
            }

            node.isLong -> {
                node.longValue()
            }

            node.isFloat -> {
                node.floatValue()
            }

            node.isDouble -> {
                node.doubleValue()
            }

            node.isNumber -> {
                node.numberValue()
            }

            node.isTextual -> {
                node.textValue()
            }

            node.isArray -> {
                node.map { jacksonNodeToKotlin(it) }
            }

            node.isObject -> {
                val map = mutableMapOf<String, Any?>()
                node.fields().forEach { (k, v) -> map[k] = jacksonNodeToKotlin(v) }
                map
            }

            else -> {
                node.toString()
            }
        }

    private fun jacksonNodeToJsonElement(node: com.fasterxml.jackson.databind.JsonNode): JsonElement =
        when {
            node.isNull -> {
                JsonNull
            }

            node.isBoolean -> {
                JsonPrimitive(node.booleanValue())
            }

            node.isInt -> {
                JsonPrimitive(node.intValue())
            }

            node.isLong -> {
                JsonPrimitive(node.longValue())
            }

            node.isFloat || node.isDouble -> {
                JsonPrimitive(node.doubleValue())
            }

            node.isNumber -> {
                JsonPrimitive(node.numberValue().toDouble())
            }

            node.isTextual -> {
                JsonPrimitive(node.textValue())
            }

            node.isArray -> {
                JsonArray(node.map { jacksonNodeToJsonElement(it) })
            }

            node.isObject -> {
                val entries = mutableMapOf<String, JsonElement>()
                node.fields().forEach { (k, v) -> entries[k] = jacksonNodeToJsonElement(v) }
                JsonObject(entries)
            }

            else -> {
                JsonPrimitive(node.toString())
            }
        }
}
