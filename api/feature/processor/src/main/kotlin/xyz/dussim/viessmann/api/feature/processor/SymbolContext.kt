package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
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
import xyz.dussim.viessmann.feature.api.ListEebusServicePartnerValue
import xyz.dussim.viessmann.feature.api.ListElectricalEnergyMatrixValue
import xyz.dussim.viessmann.feature.api.ListEnergyChargedDeviceValue
import xyz.dussim.viessmann.feature.api.ListFuelCellErrorValue
import xyz.dussim.viessmann.feature.api.ListLogBookEntryValue
import xyz.dussim.viessmann.feature.api.ListOperatingDataCellsDetailValue
import xyz.dussim.viessmann.feature.api.ListPowerBalanceEntryValue
import xyz.dussim.viessmann.feature.api.ListPropertyValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListSensorValue
import xyz.dussim.viessmann.feature.api.ListSolarlogDeviceValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListVentilationMessageValue
import xyz.dussim.viessmann.feature.api.ListWifiNetworkValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.LogsValue
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.OfCommand
import xyz.dussim.viessmann.feature.api.ProductInfoValue
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringValue
import xyz.dussim.viessmann.feature.api.TestResultValue

val PROPERTY_VALIDATION_FUNCTIONS =
    mapOf(
        typeNameOf<StringValue>() to validationRule("stringPropertyRule"),
        typeNameOf<BooleanValue>() to validationRule("booleanPropertyRule"),
        typeNameOf<DoubleValue>() to validationRule("doublePropertyRule"),
        typeNameOf<ListDoubleValue>() to validationRule("listDoublePropertyRule"),
        typeNameOf<ListStringValue>() to validationRule("listStringPropertyRule"),
        typeNameOf<ListDeviceErrorValue>() to validationRule("listDeviceErrorPropertyRule"),
        typeNameOf<ListZigbeeDeviceStatusValue>() to validationRule("listZigbeeDeviceStatusPropertyRule"),
        typeNameOf<ListRoomActorValue>() to validationRule("listRoomActorPropertyRule"),
        typeNameOf<ListDeviceValue>() to validationRule("listDevicePropertyRule"),
        typeNameOf<ObjectOtherRoomConfigurationValue>() to validationRule("objectOtherRoomConfigurationPropertyRule"),
        typeNameOf<ScheduleValue>() to validationRule("schedulePropertyRule"),
        typeNameOf<ListBusTypeValue>() to validationRule("listBusTypePropertyRule"),
        typeNameOf<EnergyMatrixValue>() to validationRule("energyMatrixPropertyRule"),
        typeNameOf<LogsValue>() to validationRule("logsPropertyRule"),
        typeNameOf<ListLogBookEntryValue>() to validationRule("listLogBookEntryPropertyRule"),
        typeNameOf<ProductInfoValue>() to validationRule("productInfoPropertyRule"),
        typeNameOf<FactoryResetInfoValue>() to validationRule("factoryResetInfoPropertyRule"),
        typeNameOf<ListEebusDeviceValue>() to validationRule("listEebusDevicePropertyRule"),
        typeNameOf<ListEebusServicePartnerValue>() to validationRule("listEebusServicePartnerPropertyRule"),
        typeNameOf<ListElectricalEnergyMatrixValue>() to validationRule("listElectricalEnergyMatrixPropertyRule"),
        typeNameOf<ListOperatingDataCellsDetailValue>() to validationRule("listOperatingDataCellsDetailPropertyRule"),
        typeNameOf<ListEnergyChargedDeviceValue>() to validationRule("listEnergyChargedDevicePropertyRule"),
        typeNameOf<ListDeviceInformationValue>() to validationRule("listDeviceInformationPropertyRule"),
        typeNameOf<ListSensorValue>() to validationRule("listSensorPropertyRule"),
        typeNameOf<ListPowerBalanceEntryValue>() to validationRule("listPowerBalanceEntryPropertyRule"),
        typeNameOf<ListFuelCellErrorValue>() to validationRule("listFuelCellErrorPropertyRule"),
        typeNameOf<ListWifiNetworkValue>() to validationRule("listWifiNetworkPropertyRule"),
        typeNameOf<ListVentilationMessageValue>() to validationRule("listVentilationMessagePropertyRule"),
        typeNameOf<ListSolarlogDeviceValue>() to validationRule("listSolarlogDevicePropertyRule"),
        typeNameOf<TestResultValue>() to validationRule("testResultPropertyRule"),
    )

/**
 * Marker interface for types that can be converted to KotlinPoet PropertySpec.
 */
interface ConvertibleToPropertySpec {
    fun asPropertySpec(): PropertySpec
}

/**
 * Marker interface for types that can be converted to KotlinPoet ParameterSpec.
 */
interface ConvertibleToParameterSpec {
    fun asParameterSpec(): ParameterSpec
}

/**
 * Represents a property inherited from the Feature superinterface.
 * These properties are delegated to the underlying feature instance.
 */
data class SuperInterfaceProperty(
    val name: String,
    val type: TypeName,
) : ConvertibleToPropertySpec,
    ConvertibleToParameterSpec {
    companion object {
        fun from(entry: Map.Entry<String, TypeName>) =
            SuperInterfaceProperty(
                entry.key,
                entry.value,
            )
    }

    override fun asPropertySpec() = overrideProperty(name, type, name)

    override fun asParameterSpec() =
        ParameterSpec
            .builder(name, type)
            .build()
}

/**
 * Represents a feature property (not a command).
 * Can be a simple value, list value, or enum value.
 *
 * @property name Property name
 * @property type Property type (may be enum or value type)
 * @property property Parent class declaration
 * @property isListProperty True if this is a list property
 * @property isEnumProperty True if this is an enum property
 */
data class ParameterProperty(
    val name: String,
    val type: TypeName,
    val property: KSClassDeclaration,
    val isListProperty: Boolean,
    val isEnumProperty: Boolean,
) : ConvertibleToPropertySpec {
    companion object {
        fun from(
            property: KSPropertyDeclaration,
            nestedEnums: List<EnumSymbolContext>,
        ): ParameterProperty {
            val type = property.type.resolve().toTypeName()
            val propertyDeclaration = property.type.resolve().declaration as KSClassDeclaration
            return ParameterProperty(
                name = property.simpleName.asString(),
                type = type,
                property = property.parentDeclaration as KSClassDeclaration,
                isListProperty = property.type.implementsInterface(ListPropertyValue::class),
                isEnumProperty = nestedEnums.any { it.symbol == propertyDeclaration },
            )
        }
    }

    val underlyingType
        get() =
            if (isEnumProperty) {
                SymbolContext.ENUM_VALUES_TO_TYPE.getValue(type.copy(nullable = false))
            } else {
                type.copy(nullable = false)
            }

    val isNullable get() = type.isNullable

    val validationFunction by lazy {
        val nonNullType = type.copy(nullable = false)
        if (isEnumProperty) {
            SymbolContext.ENUM_VALUES_TO_VALIDATION_RULE.getValue(nonNullType)
        } else {
            PROPERTY_VALIDATION_FUNCTIONS.getValue(nonNullType)
        }
    }

    override fun asPropertySpec() = overrideProperty(name, type)
}

/**
 * Represents a command property on a feature.
 * Commands are executable actions with parameters and constraints.
 */
data class CommandProperty(
    val name: String,
    val type: TypeName,
    val implType: TypeName,
    val signature: CommandSignature,
    val command: KSClassDeclaration,
    val isNullable: Boolean,
) : ConvertibleToPropertySpec {
    companion object {
        fun from(
            context: SymbolContext,
            property: KSPropertyDeclaration,
        ): CommandProperty {
            val resolvedType = property.type.resolve()
            val type = resolvedType.toTypeName()
            val isNullable = resolvedType.isMarkedNullable
            val declaration = resolvedType.makeNotNullable().declaration as KSClassDeclaration
            val commandContext = CommandSymbolContext(context, declaration)

            val signature = commandContext.signature
            val implType = commandContext.implType

            return CommandProperty(
                name = property.simpleName.asString(),
                type = type,
                implType = if (isNullable) implType.copy(nullable = true) else implType,
                signature = signature,
                command = property.parentDeclaration as KSClassDeclaration,
                isNullable = isNullable,
            )
        }
    }

    override fun asPropertySpec() = overrideProperty(name, implType)
}

/**
 * Context containing all information needed to generate a feature implementation.
 * Manages properties, commands, enums, and validation rules.
 */
data class SymbolContext(
    val symbol: KSClassDeclaration,
    val ruleRegistry: RuleRegistry,
) {
    @OptIn(KspExperimental::class)
    val featureName = symbol.getAnnotationsByType(GenerateFeatureImplementation::class).first().featureName
    val name = symbol.simpleName
    val superInterface = symbol.toClassName()
    val superInterfaceCompanion = superInterface.nestedClass("Companion")
    val implName = ClassName(symbol.packageName.asString(), symbol.simpleName.asString().replace("_", "") + "Impl")
    val implCompanion = implName.nestedClass("Companion")

    /**
     * True if the feature name contains a placeholder "{N}" for indexed features.
     */
    val isIndexed by lazy { featureName.contains("{N}") }

    val featureSignature by lazy {
        FeatureSignature(
            baseFeature = baseFeature,
            properties = parameterProperties.map { it.name to it.type }.sortedBy { it.first },
            commands = commandProperties.map { Triple(it.name, it.signature, it.isNullable) }.sortedBy { it.first },
        )
    }

    val baseFeature = BaseFeature.Feature

    val featureProperties by lazy { featureProperties(this) }
    val parameterProperties by lazy { parameterProperties(this, nestedEnums) }
    val commandProperties by lazy { commandProperties(this) }

    val featurePropertiesImpl by lazy { featureProperties.map { it.asPropertySpec() } }
    val parameterPropertiesImpl by lazy { parameterProperties.map { it.asPropertySpec() } }
    val commandPropertiesImpl by lazy { commandProperties.map { it.asPropertySpec() } }

    val featureParametersImpl by lazy { featureProperties.map { it.asParameterSpec() } }

    val allPropertiesImpl by lazy { featurePropertiesImpl + parameterPropertiesImpl + commandPropertiesImpl }

    val nestedCommands by lazy { nestedCommands(this) }

    @OptIn(KspExperimental::class)
    val nestedEnums by lazy {
        symbol
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAnnotationPresent(FeatureEnum::class) }
            .map { EnumSymbolContext(this, it) }
            .toList()
    }

    companion object {
        val ENUM_VALUES_TO_VALIDATION_RULE = mutableMapOf<TypeName, MemberName>()
        val ENUM_VALUES_TO_TYPE = mutableMapOf<TypeName, TypeName>()
    }
}

/**
 * Context for feature enum types.
 * Feature enums are custom enum types used as property values.
 */
data class EnumSymbolContext(
    val parentContext: SymbolContext,
    val symbol: KSClassDeclaration,
)

fun featureProperties(context: SymbolContext): List<SuperInterfaceProperty> =
    context
        .baseFeature
        .superInterfaceProperties
        .map(SuperInterfaceProperty::from)

fun parameterProperties(
    context: SymbolContext,
    nestedEnums: List<EnumSymbolContext>,
): List<ParameterProperty> =
    context
        .symbol
        .getDeclaredProperties()
        .filterNot { it.type.implementsInterface(OfCommand::class) }
        .map { ParameterProperty.from(it, nestedEnums) }
        .toList()

fun commandProperties(context: SymbolContext): List<CommandProperty> =
    context
        .symbol
        .getDeclaredProperties()
        .filter { it.type.implementsInterface(OfCommand::class) }
        .map { CommandProperty.from(context, it) }
        .toList()

fun nestedCommands(context: SymbolContext): List<CommandSymbolContext> =
    context
        .symbol
        .getDeclaredProperties()
        .filter { it.type.implementsInterface(OfCommand::class) }
        .map {
            CommandSymbolContext(
                context,
                it.type
                    .resolve()
                    .makeNotNullable()
                    .declaration as KSClassDeclaration,
            )
        }.toList()
