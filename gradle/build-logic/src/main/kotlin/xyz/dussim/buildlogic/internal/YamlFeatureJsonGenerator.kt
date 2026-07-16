package xyz.dussim.buildlogic.internal

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
        val removalDate: LocalDate? = null,
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

    private fun extractRemovalDate(api: OpenAPI): LocalDate? {
        val pathItem = api.paths.values.firstOrNull { it.get != null } ?: return null
        val getOp = pathItem.get ?: return null
        if (getOp.deprecated != true) return null

        val deprecationInfo = getOp.extensions?.get("x-deprecation-info") as? Map<*, *>
        val removalDateString = deprecationInfo?.get("removal-date")?.toString() ?: return null

        return try {
            LocalDate.parse(removalDateString)
        } catch (e: DateTimeParseException) {
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
        val resolved = OpenApiSchemaResolver.resolve(responseSchema)
        val featureJson = schemaToJson(resolved, featureName, api, baseDir) as? JsonObject ?: buildDefaultFeatureJson(featureName)
        return featureJson.withMetadata(buildFeatureMetadata(api, featureName, resolved))
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

        if (schema.nullable == true) {
            return JsonNull
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
            val resolved = OpenApiSchemaResolver.resolve(propSchema)
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
        val resolved = OpenApiSchemaResolver.resolve(items)
        val element = schemaToJson(resolved, featureName, api, baseDir)
        return JsonArray(listOf(element))
    }

    private fun JsonObject.withMetadata(metadata: JsonObject): JsonObject =
        JsonObject(
            toMutableMap().apply {
                put("_metadata", metadata)
            },
        )

    private fun buildFeatureMetadata(
        api: OpenAPI,
        featureName: String,
        responseSchema: Schema<*>,
    ): JsonObject =
        JsonObject(
            mapOf(
                "properties" to JsonArray(extractPropertyMetadata(responseSchema)),
                "commands" to JsonArray(extractCommandMetadata(api, featureName, responseSchema)),
            ),
        )

    private fun extractPropertyMetadata(responseSchema: Schema<*>): List<JsonObject> =
        mapResolvedProperties(responseSchema.properties?.get("properties")) { name, schema, required ->
            buildFeaturePropertyMetadata(name = name, schema = schema, required = required)
        }

    /**
     * Resolves [container], iterates its `properties` in sorted order and yields the resolved
     * property schema plus `required` flag derived from the container's `required` list.
     */
    private fun <T : Any> mapResolvedProperties(
        container: Schema<*>?,
        transform: (name: String, schema: Schema<*>, required: Boolean) -> T?,
    ): List<T> {
        val resolved = OpenApiSchemaResolver.resolve(container ?: return emptyList())
        val entries = resolved.properties ?: return emptyList()
        val required = resolved.required?.toSet() ?: emptySet()
        return entries.entries
            .sortedBy { it.key }
            .mapNotNull { (name, schema) ->
                transform(name, OpenApiSchemaResolver.resolve(schema), name in required)
            }
    }

    private fun extractCommandMetadata(
        api: OpenAPI,
        featureName: String,
        responseSchema: Schema<*>,
    ): List<JsonObject> =
        mapResolvedProperties(responseSchema.properties?.get("commands")) { name, commandSchema, required ->
            val commandFields = commandSchema.properties ?: return@mapResolvedProperties null
            val commandName = commandFields["name"]?.example?.toString() ?: name
            val requestParameters = extractRequestParameterMetadata(api, featureName, commandName)
            val responseParameters = extractResponseParameterMetadata(commandFields["params"])
            val parameters = mergeCommandParameterMetadata(responseParameters, requestParameters)

            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(commandName),
                    "required" to JsonPrimitive(required),
                    "parameters" to JsonArray(parameters),
                ),
            )
        }

    private fun extractRequestParameterMetadata(
        api: OpenAPI,
        featureName: String,
        commandName: String,
    ): List<JsonObject> {
        val commandPathPart = "/features/$featureName/commands/$commandName"
        val postOp =
            api.paths
                ?.entries
                ?.firstOrNull { (path, _) -> path.endsWith(commandPathPart) }
                ?.value
                ?.post
                ?: return emptyList()
        val requestSchema =
            postOp.requestBody
                ?.content
                ?.get("application/json")
                ?.schema
                ?: return emptyList()

        return mapResolvedProperties(requestSchema) { name, schema, required ->
            buildSchemaFieldMetadata(name = name, schema = schema, required = required)
        }
    }

    private fun extractResponseParameterMetadata(paramsSchema: Schema<*>?): List<JsonObject> =
        mapResolvedProperties(paramsSchema) { name, paramSchema, requiredFromSet ->
            val paramFields = paramSchema.properties ?: return@mapResolvedProperties null
            val type = paramFields["type"]?.example?.toString() ?: schemaType(paramSchema)
            val required = (paramFields["required"]?.example as? Boolean) ?: requiredFromSet
            buildFieldMetadata(
                name = name,
                type = type,
                subtype = schemaSubtype(paramFields["value"]),
                required = required,
                nullable = paramFields["value"]?.nullable == true,
            )
        }

    private fun buildFeaturePropertyMetadata(
        name: String,
        schema: Schema<*>,
        required: Boolean,
    ): JsonObject {
        val fields = schema.properties ?: emptyMap()
        val valueSchema = fields["value"]
        val type = fields["type"]?.example?.toString() ?: schemaType(valueSchema ?: schema)
        return buildFieldMetadata(
            name = name,
            type = type,
            subtype = schemaSubtype(valueSchema, name),
            required = required,
            nullable = valueSchema?.nullable == true || schema.nullable == true,
        )
    }

    private fun buildSchemaFieldMetadata(
        name: String,
        schema: Schema<*>,
        required: Boolean,
    ): JsonObject =
        buildFieldMetadata(
            name = name,
            type = schemaType(schema),
            subtype = schemaSubtype(schema),
            required = required,
            nullable = schema.nullable == true,
        )

    private fun buildFieldMetadata(
        name: String,
        type: String,
        subtype: String?,
        required: Boolean,
        nullable: Boolean,
    ): JsonObject =
        JsonObject(
            mapOf(
                "name" to JsonPrimitive(name),
                "type" to JsonPrimitive(type),
                "subtype" to (subtype?.let(::JsonPrimitive) ?: JsonNull),
                "required" to JsonPrimitive(required),
                "nullable" to JsonPrimitive(nullable),
            ),
        )

    private fun schemaType(schema: Schema<*>): String =
        when {
            schema.type != null -> schema.type
            schema.properties != null -> "object"
            schema.items != null -> "array"
            else -> "unknown"
        }

    private fun schemaSubtype(
        schema: Schema<*>?,
        propertyName: String? = null,
    ): String? {
        val resolved = OpenApiSchemaResolver.resolve(schema ?: return null)
        return when (schemaType(resolved)) {
            "array" -> {
                val items = OpenApiSchemaResolver.resolve(resolved.items ?: return null)
                if (schemaType(items) == "object") {
                    arrayObjectSubtype(items.properties, propertyName)
                } else {
                    schemaType(items)
                }
            }

            "object" -> {
                objectSubtype(resolved.properties)
            }

            else -> {
                null
            }
        }
    }

    private fun arrayObjectSubtype(
        itemProperties: Map<String, Schema<*>>?,
        propertyName: String?,
    ): String? {
        for (matcher in ARRAY_OBJECT_MATCHERS) {
            if (itemProperties.containsAll(matcher.requiredKeys) && (matcher.excludedKeys.isEmpty() || !itemProperties.containsAny(matcher.excludedKeys))) {
                return matcher.subtype
            }
        }

        if (itemProperties.containsAll(listOf("value", "unit", "type"))) {
            return when (itemProperties?.get("type")?.example?.toString()) {
                "string" -> "string"
                "number" -> "number"
                else -> "powerBalanceEntry"
            }
        }

        if (propertyName == "actors") {
            return "roomActor"
        }

        return "object"
    }

    private fun objectSubtype(properties: Map<String, Schema<*>>?): String? {
        for (matcher in OBJECT_MATCHERS) {
            if (properties.containsAll(matcher.requiredKeys)) {
                return matcher.subtype
            }
        }
        return null
    }

    private fun Map<String, Schema<*>>?.containsAll(keys: List<String>): Boolean = this != null && keys.all(::containsKey)

    private fun Map<String, Schema<*>>?.containsAny(keys: List<String>): Boolean = this != null && keys.any(::containsKey)

    private data class ArrayObjectSubtypeMatcher(
        val requiredKeys: List<String>,
        val excludedKeys: List<String> = emptyList(),
        val subtype: String,
    )

    private data class ObjectSubtypeMatcher(
        val requiredKeys: List<String>,
        val subtype: String,
    )

    private fun mergeCommandParameterMetadata(
        responseParameters: List<JsonObject>,
        requestParameters: List<JsonObject>,
    ): List<JsonObject> {
        val requestByName = requestParameters.associateBy { it["name"] }
        val mergedResponseParameters =
            responseParameters.map { response ->
                val request = requestByName[response["name"]]
                if (request != null && response["subtype"] is JsonNull && request["subtype"] !is JsonNull) {
                    JsonObject(
                        response.toMutableMap().apply {
                            put("subtype", request.getValue("subtype"))
                        },
                    )
                } else {
                    response
                }
            }
        val responseNames = responseParameters.map { it["name"] }.toSet()
        return mergedResponseParameters + requestParameters.filterNot { it["name"] in responseNames }
    }

    private fun extractRef(example: Any?): String? =
        when (example) {
            is ObjectNode -> {
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

            is BigDecimal -> {
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

            is JsonNode -> {
                // ArrayNode/ObjectNode may contain nested $ref entries; route them back through
                // exampleToJson via the kotlin-typed projection so $ref resolution still works.
                when (example) {
                    is ArrayNode -> {
                        JsonArray(example.map { exampleToJson(jacksonNodeToKotlin(it), api, baseDir) })
                    }

                    is ObjectNode -> {
                        val entries = mutableMapOf<String, JsonElement>()
                        example.properties().forEach { (k, v) -> entries[k] = exampleToJson(jacksonNodeToKotlin(v), api, baseDir) }
                        JsonObject(entries)
                    }

                    else -> {
                        jacksonNodeToJsonElement(example)
                    }
                }
            }

            is OffsetDateTime -> {
                JsonPrimitive(OFFSET_DATE_TIME_FORMATTER.format(example.withOffsetSameInstant(ZoneOffset.UTC)))
            }

            is LocalDate -> {
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
        private val YAML_MAPPER: ObjectMapper = ObjectMapper(YAMLFactory())

        private val OFFSET_DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        private val ARRAY_OBJECT_MATCHERS =
            listOf(
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("errorCode", "timestamp", "accessLevel", "priority", "audiences", "busAddress", "busType"),
                    subtype = "deviceError",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("device", "value"),
                    subtype = "zigbeeDeviceStatus",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("deviceId", "heatingCircuit"),
                    subtype = "roomActor",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("fingerprint"),
                    subtype = "device",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("timestamp", "actor", "status", "event", "circuit", "stateMachine", "additionalInfo"),
                    subtype = "logBookEntry",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("deviceFamily", "error", "subCode"),
                    subtype = "onboardUpdaterLastErrorCode",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("type", "brand", "model", "id", "ski"),
                    subtype = "eebusDevice",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("type", "id", "ski"),
                    subtype = "eebusServicePartner",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("type", "busAddress"),
                    excludedKeys = listOf("brand", "model", "id", "ski", "busType", "value", "unit"),
                    subtype = "eebusDevicesPaired",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("type", "index"),
                    excludedKeys = listOf("manufacturer", "model", "serialNumber", "value", "unit"),
                    subtype = "solarlogDevicesPaired",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("voltageValue", "cellBalance", "functionStatus", "safetyStatus"),
                    subtype = "operatingDataCellsDetail",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("id", "role", "status", "memberId", "value", "unit"),
                    subtype = "energyChargedDevice",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("deviceObjectProperty", "deviceFunction", "softwareVersion", "hardwareVersion", "etn"),
                    excludedKeys = listOf("busAddress", "busType"),
                    subtype = "deviceInformation",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("timestamp", "errorCode", "accessLevel", "priority"),
                    excludedKeys = listOf("busAddress", "busType", "audiences"),
                    subtype = "fuelCellError",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("ssid", "signalStrength"),
                    subtype = "wifiNetwork",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("timestamp", "errorCode", "status", "count", "priority"),
                    subtype = "ventilationMessage",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys =
                        listOf(
                            "code",
                            "firstAppearanceTime",
                            "firstGoneTime",
                            "lastAppearanceTime",
                            "lastGoneTime",
                            "counter",
                            "busAddress",
                            "busType",
                            "controller",
                            "active",
                            "dataTracing",
                            "audiences",
                        ),
                    subtype = "systemMessageEntry",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("busAddress", "busType"),
                    excludedKeys = listOf("value", "unit", "type"),
                    subtype = "busType",
                ),
                ArrayObjectSubtypeMatcher(
                    requiredKeys = listOf("index", "manufacturer", "model", "serialNumber"),
                    subtype = "solarlogDevice",
                ),
            )

        private val OBJECT_MATCHERS =
            listOf(
                ObjectSubtypeMatcher(listOf("hydraulicBalance"), "otherRoomConfiguration"),
                ObjectSubtypeMatcher(listOf("logs"), "logs"),
                ObjectSubtypeMatcher(listOf("busType", "busAddress", "viessmannIdentificationNumber", "productFamily"), "productInfo"),
                ObjectSubtypeMatcher(listOf("day", "month", "year"), "factoryResetInfo"),
                ObjectSubtypeMatcher(listOf("type", "value", "unit"), "property"),
            )

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
            @Suppress("UNCHECKED_CAST")
            val raw = YAML_MAPPER.readValue(file, Map::class.java) as Map<String, Any?>
            yamlCache[cacheKey] = raw
            raw
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
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

    /**
     * Projects a Jackson [JsonNode] into a plain Kotlin value tree (`Boolean`/`Number`/`String`/`List`/`Map`).
     * Used to feed back into [exampleToJson] so that `$ref` resolution can recursively traverse nested examples.
     */
    private fun jacksonNodeToKotlin(node: JsonNode): Any? =
        when {
            node.isNull -> null
            node.isBoolean -> node.booleanValue()
            node.isInt -> node.intValue()
            node.isLong -> node.longValue()
            node.isFloat -> node.floatValue()
            node.isDouble -> node.doubleValue()
            node.isNumber -> node.numberValue()
            node.isTextual -> node.textValue()
            node.isArray -> node.map(::jacksonNodeToKotlin)
            node.isObject -> buildMap(node.size()) { node.properties().forEach { (k, v) -> put(k, jacksonNodeToKotlin(v)) } }
            else -> node.toString()
        }

    /** Direct projection of a Jackson [JsonNode] into a kotlinx [JsonElement] without going through [exampleToJson]. */
    private fun jacksonNodeToJsonElement(node: JsonNode): JsonElement =
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
                JsonArray(node.map(::jacksonNodeToJsonElement))
            }

            node.isObject -> {
                JsonObject(buildMap(node.size()) { node.properties().forEach { (k, v) -> put(k, jacksonNodeToJsonElement(v)) } })
            }

            else -> {
                JsonPrimitive(node.toString())
            }
        }
}
