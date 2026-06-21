package xyz.dussim.buildlogic.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.logging.Logger

internal data class ParsedFeatureJson(
    val featureName: String,
    val metadata: FeatureMetadata,
)

@Serializable
internal data class FeatureMetadata(
    val properties: List<FieldMetadata> = emptyList(),
    val commands: List<CommandMetadata> = emptyList(),
)

@Serializable
internal data class CommandMetadata(
    val name: String,
    val required: Boolean = true,
    val parameters: List<FieldMetadata> = emptyList(),
)

@Serializable
internal data class FieldMetadata(
    val name: String,
    val type: String,
    val subtype: String? = null,
    val required: Boolean = true,
    val nullable: Boolean = false,
)

class FeatureInterfaceGenerator(
    private val packageName: String,
    private val logger: Logger,
) {
    private val sharedCommands = mutableMapOf<CommandSignature, ClassName>()
    private val metadataJson =
        Json {
            ignoreUnknownKeys = true
        }

    fun useSharedCommands(commands: Map<CommandSignature, ClassName>) {
        sharedCommands.putAll(commands)
    }

    internal fun parseFeature(feature: JsonObject): ParsedFeatureJson =
        ParsedFeatureJson(
            featureName = canonicalFeatureName(feature["feature"]?.jsonPrimitive?.content ?: ""),
            metadata =
                feature["_metadata"]
                    ?.let { metadataJson.decodeFromJsonElement<FeatureMetadata>(it) }
                    ?: error("Feature '${feature["feature"]?.jsonPrimitive?.content}' is missing _metadata"),
        )

    internal fun getCommandSignatures(feature: ParsedFeatureJson): List<CommandSignature> {
        val metadataCommands = feature.metadata.commands
        return metadataCommands.map { command ->
            commandSignatureFromMetadata(command)
        }
    }

    internal fun generate(feature: ParsedFeatureJson): FileSpec {
        val featureName = feature.featureName
        val className = featureToInterfaceName(featureName)

        val properties =
            feature.metadata
                .properties
                .map { metadata ->
                    val typeName = mapPropertyMetadataToTypeName(featureName, metadata)
                    PropertyModel(metadata.name, if (metadata.required) typeName else typeName.copy(nullable = true))
                }

        val commands =
            feature.metadata
                .commands
                .map { metadata ->
                    CommandModel(
                        name = metadata.name,
                        signature = commandSignatureFromMetadata(metadata),
                        isRequired = metadata.required,
                    )
                }

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
                .replace("{N}", "{}")
                .replace("{}", "N")
                .split(".")
                .joinToString("") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
        return if (name.endsWith("Feature")) name else "${name}Feature"
    }

    private fun canonicalFeatureName(featureName: String): String =
        featureName
            .replace("{}", "{N}")
            .replace(embeddedIndexPattern, "{N}")

    private fun mapPropertyMetadataToTypeName(
        featureName: String,
        metadata: FieldMetadata,
    ): TypeName {
        val type = metadata.type
        val subtype = metadata.subtype
        val nullable = metadata.nullable
        return when (type) {
            "string" -> ClassName("xyz.dussim.viessmann.feature.api", if (nullable) "NullableStringValue" else "StringValue")
            "number", "integer" -> ClassName("xyz.dussim.viessmann.feature.api", if (nullable) "NullableDoubleValue" else "DoubleValue")
            "boolean" -> ClassName("xyz.dussim.viessmann.feature.api", if (nullable) "NullableBooleanValue" else "BooleanValue")
            "array" -> mapArrayMetadataToTypeName(featureName, metadata.name, subtype)
            "object" -> mapObjectMetadataToTypeName(featureName, metadata.name, subtype)
            "Schedule" -> ClassName("xyz.dussim.viessmann.feature.api", "ScheduleValue")
            "DeviceList" -> ClassName("xyz.dussim.viessmann.feature.api", "ListDeviceValue")
            "EnergyMatrix" -> ClassName("xyz.dussim.viessmann.feature.api", "EnergyMatrixValue")
            "ElectricalEnergyMatrix" -> ClassName("xyz.dussim.viessmann.feature.api", "ListElectricalEnergyMatrixValue")
            "ConsolidatorValueList" -> ClassName("xyz.dussim.viessmann.feature.api", "ListEnergyChargedDeviceValue")
            "legendCo2Values", "legendAqiValues" -> ClassName("xyz.dussim.viessmann.feature.api", "ListSensorValue")
            "testResult" -> ClassName("xyz.dussim.viessmann.feature.api", "TestResultValue")
            else -> unsupportedPropertyType(featureName, metadata.name, type, "Unsupported metadata type")
        }
    }

    private fun mapArrayMetadataToTypeName(
        featureName: String,
        propertyName: String,
        subtype: String?,
    ): ClassName =
        when (subtype) {
            "string" -> {
                featureApiClassName("ListStringValue")
            }

            "number", "integer" -> {
                featureApiClassName("ListDoubleValue")
            }

            "boolean" -> {
                featureApiClassName("ListEmptyValue")
            }

            "object" -> {
                unsupportedPropertyType(featureName, propertyName, "array<object>", "Unknown object array subtype in metadata")
            }

            null -> {
                unsupportedPropertyType(featureName, propertyName, "array", "Missing array subtype in metadata")
            }

            else -> {
                ARRAY_SUBTYPE_VALUE_TYPES[subtype]?.let(::featureApiClassName)
                    ?: unsupportedPropertyType(featureName, propertyName, "array:$subtype", "Unsupported array subtype in metadata")
            }
        }

    private fun mapObjectMetadataToTypeName(
        featureName: String,
        propertyName: String,
        subtype: String?,
    ): ClassName =
        when (subtype) {
            null -> featureApiClassName("ObjectOtherRoomConfigurationValue")
            in OBJECT_SUBTYPE_VALUE_TYPES -> featureApiClassName(OBJECT_SUBTYPE_VALUE_TYPES.getValue(subtype))
            else -> unsupportedPropertyType(featureName, propertyName, "object:$subtype", "Unsupported object subtype in metadata")
        }

    private fun commandSignatureFromMetadata(command: CommandMetadata): CommandSignature {
        val parameters =
            command.parameters
                .map { parameter ->
                    val rawType = parameter.normalizedType()
                    ParameterSignature(
                        parameter.name,
                        ParameterSignature.normalizeCommandParameterType(rawType, "", command.name, parameter.name),
                    )
                }
        return CommandSignature(command.name, parameters)
    }

    private fun FieldMetadata.normalizedType(): String =
        if (type == "array" && subtype != null) {
            "array:$subtype"
        } else {
            type
        }

    private fun featureApiClassName(simpleName: String): ClassName = ClassName("xyz.dussim.viessmann.feature.api", simpleName)

    private fun unsupportedPropertyType(
        featureName: String,
        propertyName: String,
        type: String,
        reason: String,
    ): Nothing =
        throw UnsupportedPropertyTypeException(
            featureName = featureName,
            propertyName = propertyName,
            type = type,
            reason = reason,
        )

    private companion object {
        private val embeddedIndexPattern = "(?<!_)0(?=[A-Z])".toRegex()

        private val ARRAY_SUBTYPE_VALUE_TYPES =
            mapOf(
                "deviceError" to "ListDeviceErrorValue",
                "zigbeeDeviceStatus" to "ListZigbeeDeviceStatusValue",
                "roomActor" to "ListRoomActorValue",
                "device" to "ListDeviceValue",
                "logBookEntry" to "ListLogBookEntryValue",
                "onboardUpdaterLastErrorCode" to "ListOnboardUpdaterLastErrorCodeValue",
                "eebusDevice" to "ListEebusDeviceValue",
                "eebusServicePartner" to "ListEebusServicePartnerValue",
                "eebusDevicesPaired" to "ListEebusDevicesPairedValue",
                "solarlogDevicesPaired" to "ListSolarlogDevicesPairedValue",
                "operatingDataCellsDetail" to "ListOperatingDataCellsDetailValue",
                "energyChargedDevice" to "ListEnergyChargedDeviceValue",
                "deviceInformation" to "ListDeviceInformationValue",
                "fuelCellError" to "ListFuelCellErrorValue",
                "wifiNetwork" to "ListWifiNetworkValue",
                "ventilationMessage" to "ListVentilationMessageValue",
                "systemMessageEntry" to "ListSystemMessageEntryValue",
                "busType" to "ListBusTypeValue",
                "solarlogDevice" to "ListSolarlogDeviceValue",
                "powerBalanceEntry" to "ListPowerBalanceEntryValue",
            )

        private val OBJECT_SUBTYPE_VALUE_TYPES =
            mapOf(
                "otherRoomConfiguration" to "ObjectOtherRoomConfigurationValue",
                "logs" to "LogsValue",
                "productInfo" to "ProductInfoValue",
                "factoryResetInfo" to "FactoryResetInfoValue",
                "property" to "Property",
            )
    }
}
