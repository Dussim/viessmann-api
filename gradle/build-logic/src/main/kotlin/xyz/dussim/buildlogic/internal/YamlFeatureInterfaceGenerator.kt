package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import org.gradle.api.logging.Logger
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException

// region Data models

data class YamlFeatureInterface(
    val featureName: String,
    val className: String,
    val properties: List<YamlPropertyDeclaration>,
    val commands: List<YamlCommandDeclaration>,
    val isDeprecated: Boolean = false,
    val deprecationMessage: String? = null,
    val removalDate: LocalDate? = null,
)

data class YamlPropertyDeclaration(
    val name: String,
    val type: String,
    val isRequired: Boolean = true,
)

data class YamlCommandDeclaration(
    val propertyName: String,
    val commandName: String,
    val interfaceName: String,
    val parameters: List<YamlCommandParameter>,
    val isRequired: Boolean = true,
)

data class YamlCommandParameter(
    val name: String,
    val type: String,
    val constraintType: String,
)

class UnsupportedPropertyTypeException(
    val featureName: String,
    val propertyName: String,
    val type: String,
    val reason: String,
) : IllegalArgumentException(
        "Unsupported property type in feature '$featureName', property '$propertyName': type='$type'. $reason",
    )

// endregion

class YamlFeatureInterfaceGenerator(
    private val packageName: String,
    private val logger: Logger,
) {
    private val sharedCommands = mutableMapOf<CommandSignature, ClassName>()

    fun useSharedCommands(commands: Map<CommandSignature, ClassName>) {
        sharedCommands.putAll(commands)
    }

    fun parseYamlFile(file: File): YamlFeatureInterface? =
        try {
            val api = parseFeatureForInterface(file) ?: return null
            openApiToFeatureInterface(api)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn("FAIL: ${file.name} — ${e::class.simpleName}: ${e.message}")
            null
        }

    fun getCommandSignature(command: YamlCommandDeclaration): CommandSignature {
        val params =
            command.parameters.map { param ->
                val apiType =
                    when (param.type) {
                        "Boolean" -> "boolean"
                        "Double" -> "number"
                        else -> "string"
                    }
                ParameterSignature(param.name, apiType)
            }
        return CommandSignature(command.commandName, params)
    }

    fun generate(feature: YamlFeatureInterface): FileSpec {
        val properties =
            feature.properties.map { property ->
                val typeName = ClassName("xyz.dussim.viessmann.feature.api", property.type)
                val propertyType = if (property.isRequired) typeName else typeName.copy(nullable = true)
                PropertyModel(property.name, propertyType)
            }

        val commands =
            feature.commands.map { command ->
                CommandModel(
                    name = command.propertyName,
                    signature = getCommandSignature(command),
                    isRequired = command.isRequired,
                )
            }

        val model =
            FeatureModel(
                featureName = feature.featureName,
                className = feature.className,
                properties = properties,
                commands = commands,
                isDeprecated = feature.isDeprecated,
                deprecationMessage = feature.deprecationMessage,
            )

        return buildFeatureInterface(model, packageName, sharedCommands)
    }

    // region YAML parsing

    private fun parseFeatureForInterface(file: File): OpenAPI? {
        val parseOptions =
            ParseOptions().apply {
                isResolve = true
                isResolveFully = true
            }

        return OpenAPIV3Parser().readLocation(file.toURI().toString(), null, parseOptions).openAPI
    }

    // endregion

    // region Feature name / class name

    private fun extractFeatureNameFromPath(path: String): String? {
        val prefix = "/features/"
        val idx = path.indexOf(prefix)
        if (idx == -1) return null
        val afterFeatures = path.substring(idx + prefix.length)
        return afterFeatures.split("/").first().replace("{N}", "{}")
    }

    private fun featureNameToClassName(featureName: String): String =
        featureName
            .split(".")
            .joinToString("") { part ->
                if (part == "{}") {
                    "N"
                } else {
                    part.split("{", "}").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                }
            } + "Feature"

    // endregion

    // region OpenAPI → FeatureInterface

    private fun openApiToFeatureInterface(api: OpenAPI): YamlFeatureInterface? {
        val (path, pathItem) =
            api.paths.entries
                .firstOrNull { !it.key.contains("/commands/") } ?: return null
        val getOp = pathItem.get ?: return null

        val featureName = extractFeatureNameFromPath(path) ?: return null
        val className = featureNameToClassName(featureName)

        val responseSchema =
            getOp.responses
                ?.get("200")
                ?.content
                ?.get("application/json")
                ?.schema ?: return null

        val isDeprecated = getOp.deprecated == true
        var removalDate: LocalDate? = null
        val deprecationMessage =
            if (isDeprecated) {
                val deprecationInfo = getOp.extensions?.get("x-deprecation-info") as? Map<*, *>
                val info = deprecationInfo?.get("info")?.toString()
                val removalDateString = deprecationInfo?.get("removal-date")?.toString()
                if (removalDateString != null) {
                    try {
                        removalDate = LocalDate.parse(removalDateString)
                    } catch (e: DateTimeParseException) {
                        logger.warn("Invalid removal-date format in feature '$featureName': $removalDateString. Expected YYYY-MM-DD.")
                    }
                }
                buildString {
                    append(getOp.description ?: "This feature is deprecated.")
                    if (removalDateString != null) append(" Removal date: $removalDateString.")
                    if (info != null) append(" $info")
                }.takeIf { it.isNotBlank() }
            } else {
                null
            }

        return YamlFeatureInterface(
            featureName = featureName,
            className = className,
            properties = extractPropertiesForInterface(responseSchema, featureName),
            commands = extractCommandsForInterface(api, featureName, responseSchema),
            isDeprecated = isDeprecated,
            deprecationMessage = deprecationMessage,
            removalDate = removalDate,
        )
    }

    // endregion

    // region Property type mapping

    private data class ObjectArrayMatcher(
        val requiredKeys: List<String>,
        val excludedKeys: List<String> = emptyList(),
        val typeName: String,
    )

    private data class ObjectMatcher(
        val requiredKeys: List<String>,
        val typeName: String,
    )

    private fun matchArrayObjectType(
        itemProps: Map<String, Schema<*>>?,
        featureName: String,
        propertyName: String,
    ): String? {
        for (matcher in ARRAY_OBJECT_MATCHERS) {
            if (itemProps.containsAll(*matcher.requiredKeys.toTypedArray()) &&
                (matcher.excludedKeys.isEmpty() || !itemProps.containsAny(*matcher.excludedKeys.toTypedArray()))
            ) {
                return matcher.typeName
            }
        }
        if (itemProps.containsAll("value", "unit", "type")) {
            return when (itemProps?.get("type")?.example?.toString()) {
                "string" -> "ListStringValue"
                "number" -> "ListDoubleValue"
                else -> "ListPowerBalanceEntryValue"
            }
        }
        if ((featureName == "rooms.{}" || featureName == "rooms.others.{}") && propertyName == "actors") {
            return "ListRoomActorValue"
        }
        return null
    }

    private fun mapArrayItemsType(
        items: Schema<*>,
        schema: Schema<*>,
        propertyName: String,
        featureName: String,
    ): String =
        when (items.type) {
            "number" -> {
                "ListDoubleValue"
            }

            "string" -> {
                "ListStringValue"
            }

            "object" -> {
                matchArrayObjectType(items.properties, featureName, propertyName)
                    ?: throw UnsupportedPropertyTypeException(
                        featureName = featureName,
                        propertyName = propertyName,
                        type = "array<object>",
                        reason = "Unknown object structure in array",
                    )
            }

            null -> {
                throw UnsupportedPropertyTypeException(
                    featureName = featureName,
                    propertyName = propertyName,
                    type = "array<unknown>",
                    reason = "Array items type is not specified",
                )
            }

            else -> {
                throw UnsupportedPropertyTypeException(
                    featureName = featureName,
                    propertyName = propertyName,
                    type = "array<${items.type}>",
                    reason = "Unsupported array item type. Supported: string, number, object",
                )
            }
        }

    private fun mapObjectType(
        schema: Schema<*>,
        propertyName: String,
        featureName: String,
    ): String {
        val objProps = schema.properties?.get("value")?.properties
        for (matcher in OBJECT_MATCHERS) {
            if (objProps.containsAll(*matcher.requiredKeys.toTypedArray())) {
                return matcher.typeName
            }
        }
        throw UnsupportedPropertyTypeException(
            featureName = featureName,
            propertyName = propertyName,
            type = "object",
            reason = "Unknown object type",
        )
    }

    private fun mapPropertyType(
        type: String,
        schema: Schema<*>,
        propertyName: String,
        featureName: String,
    ): String =
        when (type) {
            "boolean" -> {
                "BooleanValue"
            }

            "number" -> {
                "DoubleValue"
            }

            "string" -> {
                "StringValue"
            }

            "array" -> {
                val items =
                    schema.properties?.get("value")?.items
                        ?: throw UnsupportedPropertyTypeException(
                            featureName = featureName,
                            propertyName = propertyName,
                            type = "array",
                            reason = "No items schema found for array property",
                        )
                mapArrayItemsType(items, schema, propertyName, featureName)
            }

            "object" -> {
                mapObjectType(schema, propertyName, featureName)
            }

            "EnergyMatrix" -> {
                "EnergyMatrixValue"
            }

            "Schedule" -> {
                "ScheduleValue"
            }

            "DeviceList" -> {
                "ListDeviceValue"
            }

            "ElectricalEnergyMatrix" -> {
                "ListElectricalEnergyMatrixValue"
            }

            "ConsolidatorValueList" -> {
                "ListEnergyChargedDeviceValue"
            }

            "legendCo2Values", "legendAqiValues" -> {
                "ListSensorValue"
            }

            else -> {
                throw UnsupportedPropertyTypeException(
                    featureName = featureName,
                    propertyName = propertyName,
                    type = type,
                    reason = "Unsupported property type. Supported: boolean, number, string, array, object",
                )
            }
        }

    // endregion

    // region Schema composition resolution

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

    // region Property & command extraction

    private fun extractPropertiesForInterface(
        responseSchema: Schema<*>,
        featureName: String,
    ): List<YamlPropertyDeclaration> {
        val resolvedResponseSchema = resolveSchemaComposition(responseSchema, featureName)

        val propertiesSchema =
            resolvedResponseSchema.properties?.get("properties") ?: run {
                logger.warn("WARNING: No properties schema found in feature '$featureName'.")
                return emptyList()
            }
        val propertyEntries =
            propertiesSchema.properties ?: run {
                logger.warn("WARNING: No actual properties found in feature '$featureName'.")
                return emptyList()
            }
        val requiredProperties = propertiesSchema.required ?: emptyList()

        return propertyEntries.mapNotNull { (name, propSchema) ->
            val resolvedSchema = resolveSchemaComposition(propSchema, featureName)
            val propFields = resolvedSchema.properties ?: return@mapNotNull null
            val type = propFields["type"]?.example?.toString() ?: "string"

            val propertyType = mapPropertyType(type, resolvedSchema, name, featureName)

            YamlPropertyDeclaration(
                name = name,
                type = propertyType,
                isRequired = name in requiredProperties,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractCommandsForInterface(
        api: OpenAPI,
        featureName: String,
        responseSchema: Schema<*>,
    ): List<YamlCommandDeclaration> {
        val commandsSchema = responseSchema.properties?.get("commands") ?: return emptyList()
        val commandEntries = commandsSchema.properties ?: return emptyList()
        val requiredCommands = commandsSchema.required?.toSet() ?: emptySet()

        return commandEntries.mapNotNull { (propertyName, cmdSchema) ->
            val cmdFields = cmdSchema.properties ?: return@mapNotNull null
            val commandName = cmdFields["name"]?.example?.toString() ?: propertyName

            YamlCommandDeclaration(
                propertyName = propertyName,
                commandName = commandName,
                interfaceName = propertyName.replaceFirstChar { it.uppercaseChar() },
                parameters = extractCommandParameters(cmdFields["params"]),
                isRequired = propertyName in requiredCommands,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractCommandParameters(paramsSchema: Schema<*>?): List<YamlCommandParameter> {
        val paramEntries = paramsSchema?.properties ?: return emptyList()

        return paramEntries.mapNotNull { (name, paramSchema) ->
            val fields = paramSchema.properties ?: return@mapNotNull null
            val type = fields["type"]?.example?.toString() ?: "string"

            val (kotlinType, constraintType) =
                when (type) {
                    "boolean" -> "Boolean" to "BooleanConstraints"
                    "number" -> "Double" to "NumberConstraints"
                    else -> "String" to "StringConstraints"
                }

            YamlCommandParameter(
                name = name,
                type = kotlinType,
                constraintType = constraintType,
            )
        }
    }

    // endregion

    companion object {
        private fun Map<String, *>?.containsAny(vararg keys: String): Boolean = this != null && keys.any { containsKey(it) }

        private fun Map<String, *>?.containsAll(vararg keys: String): Boolean = this != null && keys.all { containsKey(it) }

        private val ARRAY_OBJECT_MATCHERS =
            listOf(
                ObjectArrayMatcher(
                    requiredKeys =
                        listOf(
                            "errorCode",
                            "timestamp",
                            "accessLevel",
                            "priority",
                            "audiences",
                            "busAddress",
                            "busType",
                        ),
                    typeName = "ListDeviceErrorValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("device", "value"),
                    typeName = "ListZigbeeDeviceStatusValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("deviceId", "heatingCircuit"),
                    typeName = "ListRoomActorValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("fingerprint"),
                    typeName = "ListDeviceValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys =
                        listOf(
                            "timestamp",
                            "actor",
                            "status",
                            "event",
                            "circuit",
                            "stateMachine",
                            "additionalInfo",
                        ),
                    typeName = "ListLogBookEntryValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("type", "brand", "model", "id", "ski"),
                    typeName = "ListEebusDeviceValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("type", "id", "ski"),
                    typeName = "ListEebusServicePartnerValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("voltageValue", "cellBalance", "functionStatus", "safetyStatus"),
                    typeName = "ListOperatingDataCellsDetailValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("id", "role", "status", "memberId", "value", "unit"),
                    typeName = "ListEnergyChargedDeviceValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys =
                        listOf(
                            "deviceObjectProperty",
                            "deviceFunction",
                            "softwareVersion",
                            "hardwareVersion",
                            "etn",
                        ),
                    typeName = "ListDeviceInformationValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("timestamp", "errorCode", "accessLevel", "priority"),
                    excludedKeys = listOf("busAddress", "busType", "audiences"),
                    typeName = "ListFuelCellErrorValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("ssid", "signalStrength"),
                    typeName = "ListWifiNetworkValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("timestamp", "errorCode", "status", "count", "priority"),
                    typeName = "ListVentilationMessageValue",
                ),
                ObjectArrayMatcher(
                    requiredKeys = listOf("busType"),
                    typeName = "ListBusTypeValue",
                ),
            )

        private val OBJECT_MATCHERS =
            listOf(
                ObjectMatcher(listOf("hydraulicBalance"), "ObjectOtherRoomConfigurationValue"),
                ObjectMatcher(listOf("logs"), "LogsValue"),
                ObjectMatcher(
                    listOf("busType", "busAddress", "viessmannIdentificationNumber", "productFamily"),
                    "ProductInfoValue",
                ),
                ObjectMatcher(listOf("day", "month", "year"), "FactoryResetInfoValue"),
                ObjectMatcher(listOf("type", "value", "unit"), "Property"),
            )
    }
}
