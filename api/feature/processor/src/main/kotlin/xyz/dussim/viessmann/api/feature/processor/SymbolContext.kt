package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.FactoryResetInfoValue
import xyz.dussim.viessmann.feature.api.ListBusTypeValue
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceInformationValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListEebusDeviceValue
import xyz.dussim.viessmann.feature.api.ListEebusDevicesPairedValue
import xyz.dussim.viessmann.feature.api.ListEebusServicePartnerValue
import xyz.dussim.viessmann.feature.api.ListElectricalEnergyMatrixValue
import xyz.dussim.viessmann.feature.api.ListEnergyChargedDeviceValue
import xyz.dussim.viessmann.feature.api.ListFuelCellErrorValue
import xyz.dussim.viessmann.feature.api.ListLogBookEntryValue
import xyz.dussim.viessmann.feature.api.ListOnboardUpdaterLastErrorCodeValue
import xyz.dussim.viessmann.feature.api.ListOperatingDataCellsDetailValue
import xyz.dussim.viessmann.feature.api.ListPowerBalanceEntryValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListSensorValue
import xyz.dussim.viessmann.feature.api.ListSolarlogDeviceValue
import xyz.dussim.viessmann.feature.api.ListSolarlogDevicesPairedValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListSystemMessageEntryValue
import xyz.dussim.viessmann.feature.api.ListVentilationMessageValue
import xyz.dussim.viessmann.feature.api.ListWifiNetworkValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.LogsValue
import xyz.dussim.viessmann.feature.api.NullableBooleanValue
import xyz.dussim.viessmann.feature.api.NullableDoubleValue
import xyz.dussim.viessmann.feature.api.NullableStringValue
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.OfCommand
import xyz.dussim.viessmann.feature.api.ProductInfoValue
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.TestResultValue

internal val PROPERTY_TYPE_ADAPTERS: Map<TypeName, TypeAdapterSpec> =
    listOf(
        standardPropertyAdapter<StringValue>("stringPropertyRule"),
        standardPropertyAdapter<BooleanValue>("booleanPropertyRule"),
        standardPropertyAdapter<DoubleValue>("doublePropertyRule"),
        nullableValuePropertyAdapter<NullableStringValue>(
            "nullableStringPropertyRule",
            "requireNullableStringPropertyValue",
            "findNullableStringPropertyValueOrNull",
        ),
        nullableValuePropertyAdapter<NullableBooleanValue>(
            "nullableBooleanPropertyRule",
            "requireNullableBooleanPropertyValue",
            "findNullableBooleanPropertyValueOrNull",
        ),
        nullableValuePropertyAdapter<NullableDoubleValue>(
            "nullableDoublePropertyRule",
            "requireNullableDoublePropertyValue",
            "findNullableDoublePropertyValueOrNull",
        ),
        listPropertyAdapter<ListDoubleValue>("listDoublePropertyRule"),
        listPropertyAdapter<ListStringValue>("listStringPropertyRule"),
        listPropertyAdapter<ListDeviceErrorValue>("listDeviceErrorPropertyRule"),
        listPropertyAdapter<ListZigbeeDeviceStatusValue>("listZigbeeDeviceStatusPropertyRule"),
        listPropertyAdapter<ListRoomActorValue>("listRoomActorPropertyRule"),
        listPropertyAdapter<ListDeviceValue>("listDevicePropertyRule"),
        standardPropertyAdapter<ObjectOtherRoomConfigurationValue>("objectOtherRoomConfigurationPropertyRule"),
        standardPropertyAdapter<ScheduleValue>("schedulePropertyRule"),
        listPropertyAdapter<ListBusTypeValue>("listBusTypePropertyRule"),
        standardPropertyAdapter<EnergyMatrixValue>("energyMatrixPropertyRule"),
        standardPropertyAdapter<LogsValue>("logsPropertyRule"),
        listPropertyAdapter<ListLogBookEntryValue>("listLogBookEntryPropertyRule"),
        listPropertyAdapter<ListOnboardUpdaterLastErrorCodeValue>("listOnboardUpdaterLastErrorCodePropertyRule"),
        standardPropertyAdapter<ProductInfoValue>("productInfoPropertyRule"),
        standardPropertyAdapter<FactoryResetInfoValue>("factoryResetInfoPropertyRule"),
        listPropertyAdapter<ListEebusDeviceValue>("listEebusDevicePropertyRule"),
        listPropertyAdapter<ListEebusDevicesPairedValue>("listEebusDevicesPairedPropertyRule"),
        listPropertyAdapter<ListEebusServicePartnerValue>("listEebusServicePartnerPropertyRule"),
        listPropertyAdapter<ListElectricalEnergyMatrixValue>("listElectricalEnergyMatrixPropertyRule"),
        listPropertyAdapter<ListOperatingDataCellsDetailValue>("listOperatingDataCellsDetailPropertyRule"),
        listPropertyAdapter<ListEnergyChargedDeviceValue>("listEnergyChargedDevicePropertyRule"),
        listPropertyAdapter<ListDeviceInformationValue>("listDeviceInformationPropertyRule"),
        listPropertyAdapter<ListSensorValue>("listSensorPropertyRule"),
        listPropertyAdapter<ListPowerBalanceEntryValue>("listPowerBalanceEntryPropertyRule"),
        listPropertyAdapter<ListFuelCellErrorValue>("listFuelCellErrorPropertyRule"),
        listPropertyAdapter<ListWifiNetworkValue>("listWifiNetworkPropertyRule"),
        listPropertyAdapter<ListVentilationMessageValue>("listVentilationMessagePropertyRule"),
        listPropertyAdapter<ListSystemMessageEntryValue>("listSystemMessageEntryPropertyRule"),
        listPropertyAdapter<ListSolarlogDeviceValue>("listSolarlogDevicePropertyRule"),
        listPropertyAdapter<ListSolarlogDevicesPairedValue>("listSolarlogDevicesPairedPropertyRule"),
        standardPropertyAdapter<TestResultValue>("testResultPropertyRule"),
    ).associateBy(TypeAdapterSpec::runtimeType)

val PROPERTY_VALIDATION_FUNCTIONS = PROPERTY_TYPE_ADAPTERS.mapValues { it.value.validationRule }

/**
 * Marker interface for types that can be converted to KotlinPoet PropertySpec.
 */
interface ConvertibleToPropertySpec {
    fun asPropertySpec(): PropertySpec
}

/**
 * Represents a feature property (not a command).
 * Can be a simple value, list value, or enum value.
 *
 * @property name Property name
 * @property type Property type (may be enum or value type)
 * @property isEnumProperty True if this is an enum property
 */
data class ParameterProperty(
    val name: String,
    val type: TypeName,
    val isEnumProperty: Boolean,
    val underlyingType: TypeName = type.copy(nullable = false),
    val validationFunction: MemberName = PROPERTY_TYPE_ADAPTERS.getValue(type.copy(nullable = false)).validationRule,
) : ConvertibleToPropertySpec {
    companion object {
        fun from(
            property: KSPropertyDeclaration,
            nestedEnumTypes: Set<ClassName>,
            enumValueRegistry: EnumValueRegistry,
        ): ParameterProperty {
            val type = property.type.resolve().toTypeName()
            val propertyDeclaration = property.type.resolve().declaration as KSClassDeclaration
            val nonNullType = type.copy(nullable = false)
            val isEnumProperty = propertyDeclaration.toClassName() in nestedEnumTypes
            return ParameterProperty(
                name = property.simpleName.asString(),
                type = type,
                isEnumProperty = isEnumProperty,
                underlyingType = if (isEnumProperty) enumValueRegistry.valueType(nonNullType) else nonNullType,
                validationFunction =
                    if (isEnumProperty) {
                        enumValueRegistry.validationFunction(nonNullType)
                    } else {
                        PROPERTY_VALIDATION_FUNCTIONS.getValue(nonNullType)
                    },
            )
        }
    }

    val isNullable get() = type.isNullable

    override fun asPropertySpec() = overrideProperty(name, type)
}

/**
 * Represents a command property on a feature.
 * Commands are executable actions with parameters and constraints.
 */
data class CommandProperty(
    val name: String,
    val apiName: String,
    val implType: TypeName,
    val signature: CommandSignature,
    val isNullable: Boolean,
    val commandContext: CommandSymbolContext? = null,
) : ConvertibleToPropertySpec {
    companion object {
        fun from(
            context: SymbolContext,
            property: KSPropertyDeclaration,
        ): CommandProperty {
            val resolvedType = property.type.resolve()
            val isNullable = resolvedType.isMarkedNullable
            val declaration = resolvedType.makeNotNullable().declaration as KSClassDeclaration
            val commandContext = CommandSymbolContext.from(context, declaration)

            val signature = commandContext.signature
            val implType = commandContext.implType

            return CommandProperty(
                name = property.simpleName.asString(),
                apiName = commandContext.apiName,
                implType = if (isNullable) implType.copy(nullable = true) else implType,
                signature = signature,
                isNullable = isNullable,
                commandContext = commandContext,
            )
        }
    }

    override fun asPropertySpec() = overrideProperty(name, implType)
}

/**
 * Context containing all information needed to generate a feature implementation.
 * Manages properties, commands, enums, and validation rules.
 */
class SymbolContext(
    symbol: KSClassDeclaration,
    val ruleRegistry: RuleRegistry,
    val enumValueRegistry: EnumValueRegistry,
) {
    val originatingFile: KSFile =
        requireNotNull(symbol.containingFile) {
            "Generated feature ${symbol.qualifiedName?.asString()} must originate from a source file"
        }

    @OptIn(KspExperimental::class)
    val featureName = symbol.getAnnotationsByType(GenerateFeatureImplementation::class).first().featureName
    val superInterface = symbol.toClassName()
    val superInterfaceCompanion = superInterface.nestedClass("Companion")
    val implName = ClassName(symbol.packageName.asString(), symbol.simpleName.asString().replace("_", "") + "Impl")

    /**
     * True if the feature name contains a placeholder "{N}" for indexed features.
     */
    val isIndexed = featureName.contains("{N}")

    val baseFeature = BaseFeature.Feature

    @OptIn(KspExperimental::class)
    private val nestedEnumTypes =
        symbol
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAnnotationPresent(FeatureEnum::class) }
            .map { it.toClassName() }
            .toSet()

    val parameterProperties =
        symbol
            .getDeclaredProperties()
            .filterNot { it.type.implementsInterface(OfCommand::class) }
            .map { ParameterProperty.from(it, nestedEnumTypes, enumValueRegistry) }
            .toList()
    val commandProperties =
        symbol
            .getDeclaredProperties()
            .filter { it.type.implementsInterface(OfCommand::class) }
            .map { CommandProperty.from(this, it) }
            .toList()
    val featureSignature =
        FeatureSignature(
            baseFeature = baseFeature,
            properties = parameterProperties.map { it.name to it.type }.sortedBy { it.first },
            commands =
                commandProperties
                    .map { CommandFeatureSignature(it.name, it.apiName, it.signature, it.isNullable) }
                    .sortedBy { it.propertyName },
        )

    val parameterPropertiesImpl = parameterProperties.map { it.asPropertySpec() }
    val commandPropertiesImpl = commandProperties.map { it.asPropertySpec() }
    val nestedCommands = nestedCommands(this)
}

fun nestedCommands(context: SymbolContext): List<CommandSymbolContext> =
    context.commandProperties.map { commandProperty ->
        requireNotNull(commandProperty.commandContext) {
            "Command context was not initialized for ${commandProperty.name}"
        }
    }
