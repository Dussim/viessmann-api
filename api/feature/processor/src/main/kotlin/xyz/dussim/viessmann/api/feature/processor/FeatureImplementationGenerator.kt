@file:OptIn(ViessmannApiInternalExceptionUsage::class)

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
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import xyz.dussim.viessmann.feature.api.GeneratedAccessException
import xyz.dussim.viessmann.feature.api.ViessmannApiInternalExceptionUsage
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

private val REQUIRE_COMMAND = MemberName("xyz.dussim.viessmann.feature.api", "requireCommand")
private val ZERO_PARAMETER_COMMAND_RULE = validationRule("zeroParameterCommandRule")
private val OPTIONAL_ZERO_PARAMETER_COMMAND_RULE = validationRule("optionalZeroParameterCommandRule")
private val OPTIONAL_COMMAND_RULE = validationRule("optionalCommandRule")
private val OPTIONAL_COMMAND_FAIL_FAST_RULE = validationRule("optionalCommandFailFastRule")
private const val SOURCE_FEATURE = "sourceFeature"
private const val FEATURE_TYPE = "featureType"
private const val SOURCE_PROPERTIES = "sourceProperties"
private const val SOURCE_COMMANDS = "sourceCommands"

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
                .builder(SOURCE_FEATURE, context.baseFeature.delegate)
                .build(),
        ).addParameter(FEATURE_TYPE, String::class)
        .build()

/**
 * Generates initialization block that validates and assigns properties and commands.
 * Throws FeatureValidationException if validation fails.
 */
fun initBlock(context: SymbolContext): CodeBlock {
    if (context.parameterProperties.isEmpty() && context.commandProperties.isEmpty()) {
        return CodeBlock.of("")
    }

    return CodeBlock
        .builder()
        .apply {
            beginControlFlow("try")
            addDelegateAccessLocals(
                hasProperties = context.parameterProperties.isNotEmpty(),
                hasCommands = context.commandProperties.isNotEmpty(),
            )
            context.parameterProperties.forEach { property ->
                addPropertyInitialization(property)
            }
            context.commandProperties.forEach { property ->
                addCommandInitialization(property)
            }
            nextControlFlow("catch (_: %T)", GeneratedAccessException::class.asTypeName())
            addValidationException()
            endControlFlow()
        }.build()
}

internal fun CodeBlock.Builder.addDelegateAccessLocals(
    hasProperties: Boolean,
    hasCommands: Boolean,
) {
    if (hasProperties) {
        addStatement("val %N = %N.properties", SOURCE_PROPERTIES, SOURCE_FEATURE)
    }
    if (hasCommands) {
        addStatement("val %N = %N.commands", SOURCE_COMMANDS, SOURCE_FEATURE)
    }
}

/**
 * Adds property initialization code with validation.
 */
internal fun CodeBlock.Builder.addPropertyInitialization(property: ParameterProperty) {
    val name = property.name
    val type = property.type
    val isEnumProperty = property.isEnumProperty
    val combined = propertyHash(name.hashCode(), name.length)
    val isNullable = property.isNullable
    val nonNullType = type.copy(nullable = false)
    val adapter = property.typeAdapter()
    val accessor = requireNotNull(if (isNullable) adapter.optionalAccessor else adapter.requiredAccessor)
    when {
        adapter.propertyAccessorStrategy == PropertyAccessorStrategy.NULLABLE_VALUE_WRAPPER -> {
            add("this.%N = %N.%M(%S, %L)\n", name, SOURCE_PROPERTIES, accessor, name, combined)
        }

        isEnumProperty && isNullable -> {
            add(
                "this.%N = %N.%M(%S, %L, %T::class)?.let { %T(it) }\n",
                name,
                SOURCE_PROPERTIES,
                accessor,
                name,
                combined,
                property.underlyingType,
                type.copy(nullable = false),
            )
        }

        isEnumProperty -> {
            add(
                "this.%N = %T(%N.%M(%S, %L, %T::class))\n",
                name,
                type,
                SOURCE_PROPERTIES,
                accessor,
                name,
                combined,
                property.underlyingType,
            )
        }

        adapter.propertyAccessorStrategy == PropertyAccessorStrategy.LIST_WITH_EMPTY_PROMOTION -> {
            add(
                "this.%1N = %6N.%5M(%2S, %3L, %4T::class, %4T.EMPTY)\n",
                name,
                name,
                combined,
                nonNullType,
                accessor,
                SOURCE_PROPERTIES,
            )
        }

        else -> {
            add(
                "this.%N = %N.%M(%S, %L, %T::class)\n",
                name,
                SOURCE_PROPERTIES,
                accessor,
                name,
                combined,
                nonNullType,
            )
        }
    }
}

/**
 * Adds command initialization code with validation.
 */
internal fun CodeBlock.Builder.addCommandInitialization(property: CommandProperty) {
    val name = property.name
    val apiName = property.apiName
    val propertyHash = propertyHash(apiName.hashCode(), apiName.length)
    if (property.isNullable) {
        add(
            "this.%N = %N[%S, %L]?.let { %T(it) }\n",
            name,
            SOURCE_COMMANDS,
            apiName,
            propertyHash,
            property.implType.copy(nullable = false),
        )
    } else {
        add("this.%N = %T(%N.%M(%S, %L))\n", name, property.implType, SOURCE_COMMANDS, REQUIRE_COMMAND, apiName, propertyHash)
    }
}

/**
 * Adds FeatureValidationException throw statement.
 */
private fun CodeBlock.Builder.addValidationException() {
    add(
        "throw %T(%N, %N, Companion)\n",
        FeatureValidationException::class.asTypeName(),
        FEATURE_TYPE,
        SOURCE_FEATURE,
    )
}

/**
 * Generates rule expressions for the feature.
 */
private fun validationPlan(context: SymbolContext): ValidationPlan =
    ValidationPlan(
        context
            .parameterProperties
            .map {
                ValidationRulePlan(
                    normalExpression =
                        CodeBlock.of(
                            "%M",
                            context.ruleRegistry.register(
                                it.validationFunction,
                                listOf(it.name, !it.isNullable),
                                FEATURE_VALIDATION_RULE_TYPE,
                            ),
                        ),
                )
            } +
            context
                .commandProperties
                .map {
                    if (it.signature.parameters.isEmpty()) {
                        ValidationRulePlan(
                            normalExpression =
                                CodeBlock.of(
                                    "%M",
                                    context.ruleRegistry.register(
                                        if (it.isNullable) OPTIONAL_ZERO_PARAMETER_COMMAND_RULE else ZERO_PARAMETER_COMMAND_RULE,
                                        listOf(it.apiName),
                                        FEATURE_VALIDATION_RULE_TYPE,
                                    ),
                                ),
                        )
                    } else {
                        ValidationRulePlan(
                            normalExpression =
                                if (it.isNullable) {
                                    optionalCommandRuleExpression(it)
                                } else {
                                    commandRuleExpression(it)
                                },
                            failFastExpression =
                                if (it.isNullable) {
                                    optionalCommandRuleExpression(it, isFailFast = true)
                                } else {
                                    commandRuleExpression(it, isFailFast = true)
                                },
                        )
                    }
                },
    )

internal fun commandRuleExpression(
    property: CommandProperty,
    isFailFast: Boolean = false,
): CodeBlock =
    if (isFailFast) {
        CodeBlock.of("%T.failFastRule", property.implType.copy(nullable = false))
    } else {
        CodeBlock.of("%T.rule", property.implType.copy(nullable = false))
    }

internal fun optionalCommandRuleExpression(
    property: CommandProperty,
    isFailFast: Boolean = false,
): CodeBlock =
    if (isFailFast) {
        CodeBlock.of(
            "%M(%S, %T)",
            OPTIONAL_COMMAND_FAIL_FAST_RULE,
            property.apiName,
            property.implType.copy(nullable = false),
        )
    } else {
        CodeBlock.of("%M(%S, %T)", OPTIONAL_COMMAND_RULE, property.apiName, property.implType.copy(nullable = false))
    }

/**
 * Generates companion object that implements validation rules for the feature.
 */
internal fun companionObject(
    context: SymbolContext,
    validationPlan: ValidationPlan,
): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(GENERATED_FEATURE_VALIDATION_RULE_TYPE)
        .addFunction(
            generateValidateFunction(
                typeNameOf<Feature>(),
                validationPlan.normalExpressions,
                useSingleErrorResultAggregation = context.hasOnlySingleErrorRules(),
            ),
        ).addFunction(
            generateValidateFunction(
                functionName = "validateFailFast",
                targetType = typeNameOf<Feature>(),
                ruleExpressions = validationPlan.failFastExpressions,
                isFailFast = validationPlan.requiresSeparateFailFast,
            ),
        ).build()

private fun SymbolContext.hasOnlySingleErrorRules(): Boolean =
    commandProperties.none { command ->
        !command.isNullable && command.signature.parameters.isNotEmpty()
    }

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
    val initBlock = initBlock(context)
    val validationPlan = validationPlan(context)
    val classImpl =
        TypeSpec
            .classBuilder(implName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(VIESSMANN_API_INTERNAL_EXCEPTION_USAGE_OPT_IN)
            .primaryConstructor(constructor)
            .superclass(context.baseFeature.abstractClass)
            .addSuperclassConstructorParameter(SOURCE_FEATURE)
            .addSuperinterfaces(superInterfaces.filter { it != context.baseFeature.delegate })
            .addType(companionObject(context, validationPlan))
            .addProperties(properties)
            .apply {
                if (initBlock.isNotEmpty()) {
                    addInitializerBlock(initBlock)
                }
            }.build()

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addAnnotation(FILE_DEPRECATION_SUPPRESSION)
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
    val validationPlan = validationPlan(context)

    val descriptorProperty =
        PropertySpec
            .builder(descriptorName, descriptorType)
            .addModifiers(KModifier.INTERNAL)
            .initializer(
                buildCodeBlock {
                    add("%M(\n", descriptorFactory)
                    indent()
                    add("wildcardName = %S,\n", context.featureName)
                    add("rule = %T,\n", implName)
                    if (validationPlan.requiresSeparateFailFast) {
                        add("failFast = %T(%T::validateFailFast),\n", ValidationRule::class, implName)
                    }
                    unindent()
                    add(") { feature ->\n")
                    indent()
                    add("feature as? %T ?: %T(feature, %S)\n", context.superInterface, implName, context.superInterface.canonicalName)
                    unindent()
                    add("}")
                },
            ).build()

    return listOf(descriptorProperty) + featureExtensions(context, descriptorName, descriptorType)
}
