package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureValidationException

private val REQUIRE_COMMAND = MemberName("xyz.dussim.viessmann.feature.api", "requireCommand")
private val REQUIRE_PROPERTY_VALUE = MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValue")
private val REQUIRE_PROPERTY_VALUE_OR_NULL_IF_MISSING =
    MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValueOrNullIfMissing")
private val FIND_PROPERTY_VALUE_OR_NULL = MemberName("xyz.dussim.viessmann.feature.api", "findPropertyValueOrNull")
private val REQUIRE_PROPERTY_VALUE_OR_DEFAULT_COMPAT =
    MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValueOrPromoteEmpty")
private val FIND_PROPERTY_VALUE_OR_DEFAULT_COMPAT =
    MemberName("xyz.dussim.viessmann.feature.api", "findPropertyValueOrPromoteEmpty")

/**
 * Generates constructor accepting a delegate feature.
 * When using abstract class, only delegate parameter is needed.
 * Otherwise, all feature parameters and hashCode are included.
 */
fun constructor(context: SymbolContext): FunSpec =
    FunSpec
        .constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder(DELEGATE, context.baseFeature.delegate)
                .build(),
        ).build()

/**
 * Generates initialization block that validates and assigns properties and commands.
 * Throws FeatureValidationException if validation fails.
 */
fun initBlock(
    context: SymbolContext,
    implName: ClassName,
): CodeBlock {
    if (context.parameterProperties.isEmpty() && context.commandProperties.isEmpty()) {
        return CodeBlock.of("")
    }

    return CodeBlock
        .builder()
        .apply {
            beginControlFlow("try")
            context.parameterProperties.forEach { property ->
                addPropertyInitialization(property)
            }
            context.commandProperties.forEach { property ->
                addCommandInitialization(property)
            }
            nextControlFlow("catch (_: Exception)")
            addValidationException(context, implName)
            endControlFlow()
        }.build()
}

/**
 * Adds property initialization code with validation.
 */
internal fun CodeBlock.Builder.addPropertyInitialization(property: ParameterProperty) {
    val (name, type, _, isListProperty, isEnumProperty) = property
    val combined = propertyHash(name.hashCode(), name.length)
    val isNullable = property.isNullable
    when {
        isEnumProperty && isNullable -> {
            add(
                "%N = properties.%M<%T>(%S, %L)?.let { %T(it) }\n",
                name,
                REQUIRE_PROPERTY_VALUE_OR_NULL_IF_MISSING,
                property.underlyingType,
                name,
                combined,
                type.copy(nullable = false),
            )
        }

        isEnumProperty -> {
            add(
                "%N = %T(properties.%M<%T>(%S, %L))\n",
                name,
                type,
                REQUIRE_PROPERTY_VALUE,
                property.underlyingType,
                name,
                combined,
            )
        }

        isListProperty && isNullable -> {
            add(
                "%1N = properties.%5M<%4T>(%2S, %3L, %4T.EMPTY)\n",
                name,
                name,
                combined,
                type.copy(nullable = false),
                FIND_PROPERTY_VALUE_OR_DEFAULT_COMPAT,
            )
        }

        isListProperty -> {
            add(
                "%1N = properties.%5M<%4T>(%2S, %3L, %4T.EMPTY)\n",
                name,
                name,
                combined,
                type,
                REQUIRE_PROPERTY_VALUE_OR_DEFAULT_COMPAT,
            )
        }

        isNullable -> {
            add(
                "%N = properties.%M<%T>(%S, %L)\n",
                name,
                FIND_PROPERTY_VALUE_OR_NULL,
                type.copy(nullable = false),
                name,
                combined,
            )
        }

        else -> {
            add(
                "%N = properties.%M<%T>(%S, %L)\n",
                name,
                REQUIRE_PROPERTY_VALUE,
                type,
                name,
                combined,
            )
        }
    }
}

/**
 * Adds command initialization code with validation.
 */
internal fun CodeBlock.Builder.addCommandInitialization(property: CommandProperty) {
    val name = property.name
    val propertyHash = propertyHash(name.hashCode(), name.length)
    if (property.isNullable) {
        add(
            "%N = delegate.commands[%S, %L]?.let { %T(it) }\n",
            name,
            name,
            propertyHash,
            property.implType.copy(nullable = false),
        )
    } else {
        add("%N = %T(delegate.commands.%M(%S, %L))\n", name, property.implType, REQUIRE_COMMAND, name, propertyHash)
    }
}

/**
 * Adds FeatureValidationException throw statement.
 */
private fun CodeBlock.Builder.addValidationException(
    context: SymbolContext,
    implName: ClassName,
) {
    add(
        "throw %T(%S, validate($DELEGATE), $DELEGATE)\n",
        FeatureValidationException::class.asTypeName(),
        implName.simpleName.replace("_", "").removeSuffix("Impl"),
    )
}

/**
 * Generates rule expressions for the feature.
 */
private fun ruleExpressions(context: SymbolContext): List<CodeBlock> =
    context
        .parameterProperties
        .map {
            CodeBlock.of(
                "%M",
                context.ruleRegistry.register(
                    it.validationFunction,
                    listOf(it.name, !it.isNullable),
                    FEATURE_VALIDATION_RULE_TYPE,
                ),
            )
        } +
        context
            .commandProperties
            .filter { !it.isNullable }
            .map {
                CodeBlock.of("%T.rule", it.implType.copy(nullable = false))
            }

/**
 * Generates companion object that implements validation rules for the feature.
 */
fun companionObject(
    context: SymbolContext,
    implName: ClassName,
): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(FEATURE_VALIDATION_RULE_TYPE)
        .addFunction(generateValidateFunction(typeNameOf<Feature>(), ruleExpressions(context)))
        .build()

/**
 * Generates fail-fast validation object.
 */
private fun failFastObject(
    context: SymbolContext,
    targetType: TypeName,
): TypeSpec =
    TypeSpec
        .objectBuilder("FailFast")
        .addSuperinterface(validationRuleType(targetType))
        .addFunction(generateValidateFunction(targetType, ruleExpressions(context), isFailFast = true))
        .build()

/**
 * Generates extension properties for feature companion objects.
 * Includes descriptor property.
 */
fun featureExtensions(
    context: SymbolContext,
    descriptorName: String,
    descriptorType: TypeName,
): List<PropertySpec> =
    listOf(
        PropertySpec
            .builder("descriptor", descriptorType)
            .receiver(context.superInterfaceCompanion)
            .getter(
                FunSpec
                    .getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %N", descriptorName)
                    .build(),
            ).build(),
    )

/**
 * Generates shared feature implementation class.
 *
 * @param implName The name of the implementation class
 * @param superInterfaces The list of feature interfaces to implement
 * @param context The symbol context for the feature structure
 * @return FileSpec containing the implementation class
 */
fun generateSharedFeatureImplementation(
    implName: ClassName,
    superInterfaces: List<TypeName>,
    context: SymbolContext,
): FileSpec {
    val constructor = constructor(context)
    val properties = context.parameterPropertiesImpl + context.commandPropertiesImpl
    val initBlock = initBlock(context, implName)
    val classImpl =
        TypeSpec
            .classBuilder(implName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(PUBLISHED_API_ANNOTATION)
            .primaryConstructor(constructor)
            .superclass(context.baseFeature.abstractClass)
            .addSuperclassConstructorParameter("$DELEGATE")
            .addSuperinterfaces(superInterfaces.filter { it != context.baseFeature.delegate })
            .addType(companionObject(context, implName))
            .addType(failFastObject(context, typeNameOf<Feature>()))
            .addProperties(properties)
            .apply {
                if (initBlock.isNotEmpty()) {
                    addInitializerBlock(initBlock)
                }
            }.build()

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(classImpl)
        .build()
}

/**
 * Generates feature descriptor and extension properties for a specific feature interface.
 *
 * @param context The symbol context for the feature
 * @param implName The name of the shared implementation class
 * @return List of PropertySpec containing the descriptor and extensions
 */
fun generateFeatureDescriptorAndExtensions(
    context: SymbolContext,
    implName: ClassName,
): List<PropertySpec> {
    val descriptorName = generateDescriptorName(context.superInterface)
    val descriptorType = featureDescriptorType(context.superInterface, context.isIndexed)
    val descriptorFactory = if (context.isIndexed) INDEXED_FEATURE_DESCRIPTOR_FACTORY else STATIC_FEATURE_DESCRIPTOR_FACTORY

    val descriptorProperty =
        PropertySpec
            .builder(descriptorName, descriptorType)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(PUBLISHED_API_ANNOTATION)
            .initializer(
                buildCodeBlock {
                    add("%M(\n", descriptorFactory)
                    indent()
                    add("wildcardName = %S,\n", context.featureName)
                    add("rule = %T,\n", implName)
                    add("failFast = %T.FailFast,\n", implName)
                    unindent()
                    add(") { feature ->\n")
                    indent()
                    add("feature as? %T ?: %T(feature)\n", context.superInterface, implName)
                    unindent()
                    add("}")
                },
            ).build()

    return listOf(descriptorProperty) + featureExtensions(context, descriptorName, descriptorType)
}
