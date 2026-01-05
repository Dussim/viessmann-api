package xyz.dussim.viessmann.api.feature.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListDeviceValue
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListPropertyValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.OfCommand
import xyz.dussim.viessmann.feature.api.Property
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringValue
import kotlin.time.Instant

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
    )

private val SUPERINTERFACE_PROPERTIES =
    mapOf(
        "feature" to typeNameOf<String>(),
        "wildcardFeature" to typeNameOf<String>(),
        "isEnabled" to typeNameOf<Boolean>(),
        "isReady" to typeNameOf<Boolean>(),
        "apiVersion" to typeNameOf<Int>(),
        "timestamp" to typeNameOf<Instant>(),
        "uri" to typeNameOf<String>(),
        "properties" to EfficientStringKeyMap::class.asClassName().parameterizedBy(typeNameOf<Property>()),
        "commands" to EfficientStringKeyMap::class.asClassName().parameterizedBy(typeNameOf<Command>()),
        "deviceId" to typeNameOf<String?>(),
        "gatewayId" to typeNameOf<String?>(),
        "isActive" to typeNameOf<Boolean?>(),
    )

private val DEVICE_PROPERTIES =
    SUPERINTERFACE_PROPERTIES +
        mapOf(
            "deviceId" to typeNameOf<String>(),
            "gatewayId" to typeNameOf<String>(),
        )

private val GATEWAY_PROPERTIES =
    SUPERINTERFACE_PROPERTIES +
        mapOf(
            "gatewayId" to typeNameOf<String>(),
        )

private val GEOFENCING_PROPERTIES =
    SUPERINTERFACE_PROPERTIES +
        mapOf(
            "isActive" to typeNameOf<Boolean>(),
        )

enum class BaseFeature(
    val superInterfaceProperties: Map<String, TypeName>,
    val delegate: TypeName,
) {
    Feature(SUPERINTERFACE_PROPERTIES, typeNameOf<Feature>()),
    Device(DEVICE_PROPERTIES, typeNameOf<Feature.Device>()),
    Gateway(GATEWAY_PROPERTIES, typeNameOf<Feature.Gateway>()),
    Geofencing(GEOFENCING_PROPERTIES, typeNameOf<Feature.Geofencing>()),
}

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
 * Represents a property inherited from the superinterface (e.g., Feature.Device).
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
                SymbolContext.ENUM_VALUES_TO_TYPE.getValue(type)
            } else {
                type
            }

    val validationFunction by lazy {
        if (isEnumProperty) {
            SymbolContext.ENUM_VALUES_TO_VALIDATION_RULE.getValue(type)
        } else {
            PROPERTY_VALIDATION_FUNCTIONS.getValue(type)
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
    val command: KSClassDeclaration,
) : ConvertibleToPropertySpec {
    companion object {
        context(context: SymbolContext)
        fun from(property: KSPropertyDeclaration): CommandProperty {
            val type = property.type.resolve().toTypeName()
            val superType = type.toString().substringAfterLast(".")
            val implType = context.implName.nestedClass("${superType}Impl")
            return CommandProperty(
                name = property.simpleName.asString(),
                type = type,
                implType = implType,
                command = property.parentDeclaration as KSClassDeclaration,
            )
        }
    }

    override fun asPropertySpec() = overrideProperty(name, type)
}

/**
 * Context containing all information needed to generate a feature implementation.
 * Manages properties, commands, enums, and validation rules.
 */
data class SymbolContext(
    val symbol: KSClassDeclaration,
) {
    @OptIn(KspExperimental::class)
    val featureName = symbol.getAnnotationsByType(GenerateFeatureImplementation::class).first().featureName
    val name = symbol.simpleName
    val superInterface = symbol.toClassName()
    val superInterfaceCompanion = superInterface.nestedClass("Companion")
    val implName = ClassName(symbol.packageName.asString(), symbol.simpleName.asString() + "Impl")
    val implCompanion = implName.nestedClass("Companion")

    /**
     * True if the feature name contains a placeholder "{}" for indexed features.
     */
    val isIndexed by lazy { featureName.contains("{}") }

    val baseFeature by lazy {
        symbol.superTypes.firstNotNullOf {
            when (it.toTypeName()) {
                typeNameOf<Feature>() -> BaseFeature.Feature
                typeNameOf<Feature.Device>() -> BaseFeature.Device
                typeNameOf<Feature.Gateway>() -> BaseFeature.Gateway
                typeNameOf<Feature.Geofencing>() -> BaseFeature.Geofencing
                else -> error("Unreachable")
            }
        }
    }

    val featureProperties by lazy { featureProperties() }
    val parameterProperties by lazy { parameterProperties(nestedEnums) }
    val commandProperties by lazy { commandProperties() }

    val featurePropertiesImpl by lazy { featureProperties.map { it.asPropertySpec() } }
    val parameterPropertiesImpl by lazy { parameterProperties.map { it.asPropertySpec() } }
    val commandPropertiesImpl by lazy { commandProperties.map { it.asPropertySpec() } }

    val featureParametersImpl by lazy { featureProperties.map { it.asParameterSpec() } }

    val allPropertiesImpl by lazy { featurePropertiesImpl + parameterPropertiesImpl + commandPropertiesImpl }

    val nestedCommands by lazy { nestedCommands() }

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

context(context: SymbolContext)
fun featureProperties(): List<SuperInterfaceProperty> =
    context
        .baseFeature
        .superInterfaceProperties
        .map(SuperInterfaceProperty::from)

context(context: SymbolContext)
fun parameterProperties(nestedEnums: List<EnumSymbolContext>): List<ParameterProperty> =
    context
        .symbol
        .getDeclaredProperties()
        .filterNot { it.type.implementsInterface(OfCommand::class) }
        .map { ParameterProperty.from(it, nestedEnums) }
        .toList()

context(context: SymbolContext)
fun commandProperties(): List<CommandProperty> =
    context
        .symbol
        .getDeclaredProperties()
        .filter { it.type.implementsInterface(OfCommand::class) }
        .map { CommandProperty.from(it) }
        .toList()

context(context: SymbolContext)
fun nestedCommands(): List<CommandSymbolContext> =
    context
        .symbol
        .declarations
        .filterIsInstance<KSClassDeclaration>()
        .filter { it.classKind == ClassKind.INTERFACE }
        .filter { it.superTypes.any { superType -> superType.implementsInterface(OfCommand::class) } }
        .map { CommandSymbolContext(context, it) }
        .toList()
