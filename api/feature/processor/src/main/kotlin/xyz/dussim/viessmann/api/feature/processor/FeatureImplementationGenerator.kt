@file:OptIn(ViessmannApiInternalExceptionUsage::class)

package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ViessmannApiInternalExceptionUsage
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.propertyHash

private val LIST_EMPTY_VALUE = ClassName("xyz.dussim.viessmann.feature.api", "ListEmptyValue")
private val ZERO_PARAMETER_COMMAND_RULE = validationRule("zeroParameterCommandRule")
private val OPTIONAL_ZERO_PARAMETER_COMMAND_RULE = validationRule("optionalZeroParameterCommandRule")
private val MISSING_COMMAND_RESULT = validationRule("missingCommandResult")
private const val SOURCE_FEATURE = "sourceFeature"
private const val SOURCE_PROPERTIES = "sourceProperties"
private const val SOURCE_COMMANDS = "sourceCommands"
private const val FOUND_VALUE = "found"

/** Name of the generated single-pass factory on feature and command companions. */
internal const val CREATE_OR_NULL = "createOrNull"

/**
 * Generates the private constructor. It receives the source feature plus every property and command value already
 * checked by [createOrNullFunction], so it does no checking of its own.
 */
fun constructor(context: SymbolContext): FunSpec =
    FunSpec
        .constructorBuilder()
        .addModifiers(KModifier.PRIVATE)
        .addParameter(SOURCE_FEATURE, context.baseFeature.delegate)
        .apply {
            context.parameterProperties.forEach { addParameter(it.name, it.type) }
            context.commandProperties.forEach { addParameter(it.name, it.implType) }
        }.build()

/** Property and command overrides initialized from the same-named constructor parameters. */
internal fun constructorProperties(context: SymbolContext): List<PropertySpec> =
    context.parameterProperties.map { overrideProperty(it.name, it.type, CodeBlock.of("%N", it.name)) } +
        context.commandProperties.map { overrideProperty(it.name, it.implType, CodeBlock.of("%N", it.name)) }

/**
 * Generates `createOrNull(sourceFeature)`, which reads and checks every property and command once and returns `null`
 * exactly when the feature's validation rules would report an error. It replaces validating first and constructing
 * afterwards: `getOrNull` calls it directly, and `getOrThrow` throws `FeatureValidationException` on `null`.
 */
internal fun createOrNullFunction(
    context: SymbolContext,
    implName: ClassName,
): FunSpec =
    FunSpec
        .builder(CREATE_OR_NULL)
        .addParameter(SOURCE_FEATURE, typeNameOf<Feature>())
        .returns(implName.copy(nullable = true))
        .addCode(
            buildCodeBlock {
                addDelegateAccessLocals(
                    hasProperties = context.parameterProperties.isNotEmpty(),
                    hasCommands = context.commandProperties.isNotEmpty(),
                )
                addSizeGate(requiredPropertyCount(context.parameterProperties), requiredCommandCount(context.commandProperties))
                context.parameterProperties.forEach { addPropertyExtraction(it) }
                context.commandProperties.forEach { addCommandExtraction(it) }
                val arguments =
                    (listOf(SOURCE_FEATURE) + context.parameterProperties.map { it.name } + context.commandProperties.map { it.name })
                        .map { CodeBlock.of("%N", it) }
                        .joinToCode(", ")
                add("return %T(%L)\n", implName, arguments)
            },
        ).build()

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
 * Rejects features with fewer entries than distinct required names from two map sizes, before any lookup. Most
 * descriptor/feature pairs are mismatches, so skipping it makes every rejection pay for lookups.
 */
internal fun CodeBlock.Builder.addSizeGate(
    minPropertyCount: Int,
    minCommandCount: Int,
) {
    val conditions =
        buildList {
            if (minPropertyCount > 0) add(CodeBlock.of("%N.size < %L", SOURCE_PROPERTIES, minPropertyCount))
            if (minCommandCount > 0) add(CodeBlock.of("%N.size < %L", SOURCE_COMMANDS, minCommandCount))
        }
    if (conditions.isNotEmpty()) {
        add("if (%L) return null\n", conditions.joinToCode(" || "))
    }
}

/**
 * Reads one property into a local of the same name, returning `null` from `createOrNull` when the property rule would
 * fail: a required property is missing, or a present property has a type its rule rejects. Each branch accepts exactly
 * what the corresponding rule in `Rules.kt` accepts (see [PropertyAccessorStrategy]).
 */
internal fun CodeBlock.Builder.addPropertyExtraction(property: ParameterProperty) {
    val name = property.name
    val nonNullType = property.type.copy(nullable = false)
    val adapter = property.typeAdapter()
    val lookup = CodeBlock.of("%N[%S, %L]", SOURCE_PROPERTIES, name, propertyHash(name.hashCode(), name.length))
    val isNullable = property.isNullable
    when (adapter.propertyAccessorStrategy) {
        PropertyAccessorStrategy.STANDARD ->
            if (isNullable) {
                add("val %N = %L.let { if (it == null) null else it.value as? %T ?: return null }\n", name, lookup, nonNullType)
            } else {
                add("val %N = %L?.value as? %T ?: return null\n", name, lookup, nonNullType)
            }

        PropertyAccessorStrategy.ENUM ->
            if (isNullable) {
                add(
                    "val %N = %L.let { if (it == null) null else %T(it.value as? %T ?: return null) }\n",
                    name,
                    lookup,
                    nonNullType,
                    property.underlyingType,
                )
            } else {
                add("val %N = %T(%L?.value as? %T ?: return null)\n", name, nonNullType, lookup, property.underlyingType)
            }

        PropertyAccessorStrategy.NULLABLE_VALUE_WRAPPER -> {
            val converter = requireNotNull(adapter.valueConverter) { "Missing value converter for $nonNullType" }
            if (isNullable) {
                add("val %N = %L.let { if (it == null) null else it.value.%M() ?: return null }\n", name, lookup, converter)
            } else {
                add("val %N = %L?.value?.%M() ?: return null\n", name, lookup, converter)
            }
        }

        // An absent optional list keeps today's value: the type's EMPTY instance rather than null.
        PropertyAccessorStrategy.LIST_WITH_EMPTY_PROMOTION -> {
            add("val %N = when (val %N = %L?.value) {\n", name, FOUND_VALUE, lookup)
            indent()
            if (isNullable) add("null -> %T.EMPTY\n", nonNullType) else add("null -> return null\n")
            add("is %T -> %T.EMPTY\n", LIST_EMPTY_VALUE, nonNullType)
            add("is %T -> %N\n", nonNullType, FOUND_VALUE)
            add("else -> return null\n")
            unindent()
            add("}\n")
        }

        null -> error("No accessor strategy for property $name of type $nonNullType")
    }
}

/**
 * Reads one command into a local of the same name through its companion's `createOrNull`, returning `null` from the
 * feature's `createOrNull` when a required command is missing or a present command is malformed.
 */
internal fun CodeBlock.Builder.addCommandExtraction(property: CommandProperty) {
    val apiName = property.apiName
    val lookup = CodeBlock.of("%N[%S, %L]", SOURCE_COMMANDS, apiName, propertyHash(apiName.hashCode(), apiName.length))
    val implType = property.implType.copy(nullable = false)
    if (property.isNullable) {
        add("val %N = %L.let { if (it == null) null else %T.%N(it) ?: return null }\n", property.name, lookup, implType, CREATE_OR_NULL)
    } else {
        add("val %N = %L?.let { %T.%N(it) } ?: return null\n", property.name, lookup, implType, CREATE_OR_NULL)
    }
}

/**
 * Generates rule expressions for the feature.
 */
private fun validationPlan(context: SymbolContext): ValidationPlan {
    val rules =
        context
            .parameterProperties
            .map {
                ValidationRulePlan(
                    normalExpression =
                        ruleResult(
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
                                ruleResult(
                                    context.ruleRegistry.register(
                                        if (it.isNullable) OPTIONAL_ZERO_PARAMETER_COMMAND_RULE else ZERO_PARAMETER_COMMAND_RULE,
                                        listOf(it.apiName),
                                        FEATURE_VALIDATION_RULE_TYPE,
                                    ),
                                ),
                        )
                    } else {
                        ValidationRulePlan(
                            normalExpression = commandResultExpression(it, context.ruleRegistry),
                            failFastExpression = commandResultExpression(it, context.ruleRegistry, isFailFast = true),
                        )
                    }
                }
    return ValidationPlan(rules)
}

/**
 * Looks the command up and calls its companion validator directly. Going through a shared wrapper rule such as
 * `commandRule` would route every command through one call site whose receiver is any command companion, which the
 * JIT cannot inline; a direct companion call is a call on a known final class.
 *
 * `let` with an explicit `null` check keeps the result a non-null `ValidationResult`: `?.let { } ?: ...` would type
 * it as nullable and box the value class, whose `null` payload already means valid.
 */
internal fun commandResultExpression(
    property: CommandProperty,
    ruleRegistry: RuleRegistry,
    isFailFast: Boolean = false,
): CodeBlock {
    val apiName = property.apiName
    val absentResult =
        if (property.isNullable) {
            CodeBlock.of("%T.Valid", ValidationResult::class)
        } else {
            CodeBlock.of("%M", ruleRegistry.register(MISSING_COMMAND_RESULT, listOf(apiName), VALIDATION_RESULT_TYPE))
        }
    return CodeBlock.of(
        "value.commands[%S, %L].let { if (it == null) %L else %T.%N(it) }",
        apiName,
        propertyHash(apiName.hashCode(), apiName.length),
        absentResult,
        property.implType.copy(nullable = false),
        if (isFailFast) "validateFailFast" else "validate",
    )
}

/**
 * Generates companion object that implements validation rules for the feature.
 */
internal fun companionObject(
    context: SymbolContext,
    validationPlan: ValidationPlan,
    implName: ClassName,
): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(GENERATED_FEATURE_VALIDATION_RULE_TYPE)
        .addFunction(createOrNullFunction(context, implName))
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

/**
 * Distinct names of properties whose rule fails when absent; any feature passing validation has at least this many.
 * Counting names rather than declarations keeps the bound sound if two declarations share an API name.
 */
internal fun requiredPropertyCount(properties: List<ParameterProperty>): Int = properties.filterNot { it.isNullable }.mapTo(HashSet()) { it.name }.size

/** Distinct API names of non-optional commands; see [requiredPropertyCount]. */
internal fun requiredCommandCount(commands: List<CommandProperty>): Int = commands.filterNot { it.isNullable }.mapTo(HashSet()) { it.apiName }.size

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
    val validationPlan = validationPlan(context)
    val classImpl =
        TypeSpec
            .classBuilder(implName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(VIESSMANN_API_INTERNAL_EXCEPTION_USAGE_OPT_IN)
            .primaryConstructor(constructor(context))
            .superclass(context.baseFeature.abstractClass)
            .addSuperclassConstructorParameter(SOURCE_FEATURE)
            .addSuperinterfaces(superInterfaces.filter { it != context.baseFeature.delegate })
            .addType(companionObject(context, validationPlan, implName))
            .addProperties(constructorProperties(context))
            .build()

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
                    add("featureType = %S,\n", context.superInterface.canonicalName)
                    unindent()
                    add(") { feature ->\n")
                    indent()
                    add("feature as? %T ?: %T.%N(feature)\n", context.superInterface, implName, CREATE_OR_NULL)
                    unindent()
                    add("}")
                },
            ).build()

    return listOf(descriptorProperty) + featureExtensions(context, descriptorName, descriptorType)
}
