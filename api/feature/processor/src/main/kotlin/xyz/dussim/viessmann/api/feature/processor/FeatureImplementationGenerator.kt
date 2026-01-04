package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.FeatureMatcher
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

/**
 * Creates a variable-argument function call CodeBlock.
 * Useful for generating validation result aggregation calls.
 */
fun varArgFunctionCall(
    function: MemberName,
    args: List<CodeBlock>,
) = CodeBlock
    .builder()
    .add("%M(\n", function)
    .indent()
    .apply { args.forEach(::add) }
    .unindent()
    .add(")\n")
    .build()

/**
 * Generates constructor accepting a delegate feature.
 */
context(context: SymbolContext)
fun constructor() =
    FunSpec
        .constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder(DELEGATE, context.baseFeature.delegate)
                .build(),
        ).addParameters(context.featureParametersImpl)
        .build()

/**
 * Generates initialization block that validates and assigns properties and commands.
 * Throws FeatureValidationException if validation fails.
 */
context(context: SymbolContext)
fun initBlock(): CodeBlock {
    if (context.parameterProperties.isEmpty() && context.commandProperties.isEmpty()) {
        return CodeBlock.of("")
    }

    return CodeBlock
        .builder()
        .apply {
            beginControlFlow("try")
            context.parameterProperties.forEachIndexed { index, property ->
                addPropertyInitialization(property)
            }
            context.commandProperties.forEachIndexed { index, property ->
                addCommandInitialization(property)
            }
            nextControlFlow("catch (_: Exception)")
            addValidationException()
            endControlFlow()
        }.build()
}

/**
 * Adds property initialization code with validation.
 */
context(context: SymbolContext)
private fun CodeBlock.Builder.addPropertyInitialization(property: ParameterProperty) {
    val (name, type, _, isListProperty, isEnumProperty) = property
    val combined = combineToLong(name.hashCode(), name.length)
    when {
        isEnumProperty -> {
            add("$name = %T(properties[%S, %L]!!.value as %T)\n", type, name, combined, property.underlyingType)
        }

        isListProperty -> {
            add("$name = properties[%1S, %2L]!!.value as? %3T ?: %3T.EMPTY\n", name, combined, type)
        }

        else -> {
            add("$name = properties[%S, %L]!!.value as %T\n", name, combined, type)
        }
    }
}

/**
 * Adds command initialization code with validation.
 */
context(context: SymbolContext)
private fun CodeBlock.Builder.addCommandInitialization(property: CommandProperty) {
    val (name, type, _) = property
    val combined = combineToLong(name.hashCode(), name.length)
    add("$name = %T.factory(delegate.commands[%S, %L]!!)\n", type, name, combined)
}

/**
 * Adds FeatureValidationException throw statement.
 */
context(context: SymbolContext)
private fun CodeBlock.Builder.addValidationException() {
    add(
        "throw %T(%S, invoke($DELEGATE))\n",
        FeatureValidationException::class.asTypeName(),
        context.name.asString(),
    )
}

/**
 * Generates companion object that implements validation rules for the feature.
 */
context(context: SymbolContext)
fun companionObject(): TypeSpec {
    val subTypeValidationMember =
        when (context.baseFeature.delegate) {
            typeNameOf<Feature.Device>() -> MemberName("xyz.dussim.viessmann.feature.api.validation", "deviceFeatureRule")
            typeNameOf<Feature.Gateway>() -> MemberName("xyz.dussim.viessmann.feature.api.validation", "gatewayFeatureRule")
            typeNameOf<Feature.Geofencing>() -> MemberName("xyz.dussim.viessmann.feature.api.validation", "geofencingFeatureRule")
            else -> error("Unreachable")
        }

    val commandsValidation =
        context
            .nestedCommands
            .map { context ->
                CodeBlock.of("%N(value),\n", context.implName)
            }

    val featureValidationRuleType =
        ValidationRule::class
            .asClassName()
            .parameterizedBy(
                typeNameOf<Feature>(),
                typeNameOf<ValidationError>(),
            )

    val properties =
        listOf(
            PropertySpec
                .builder(
                    "subTypeRule",
                    featureValidationRuleType,
                ).addModifiers(KModifier.PRIVATE)
                .initializer("%M(%S)", subTypeValidationMember, context.implName)
                .build(),
        ) +
            context
                .parameterProperties
                .map {
                    PropertySpec
                        .builder(
                            generatePropertyRuleName(it.name),
                            featureValidationRuleType,
                        ).addModifiers(KModifier.PRIVATE)
                        .initializer("%M(%S)", it.validationFunction, it.name)
                        .build()
                }

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(
            featureValidationRuleType,
        ).addProperties(properties)
        .addFunction(
            FunSpec
                .builder("validate")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec
                        .builder("value", typeNameOf<Feature>())
                        .build(),
                ).returns(
                    ValidationResult::class
                        .asTypeName()
                        .parameterizedBy(
                            typeNameOf<ValidationError>(),
                        ),
                ).addCode(
                    CodeBlock
                        .builder()
                        .add("return ")
                        .add(
                            varArgFunctionCall(
                                validationResultOf,
                                properties.map { CodeBlock.of("${it.name}.validate(value),\n") } + commandsValidation,
                            ),
                        ).build(),
                ).build(),
        ).build()
}

/**
 * Generates internal factory property for creating feature implementations.
 */
context(context: SymbolContext)
fun internalFactoryProperty(): PropertySpec {
    val factoryType =
        FeatureFactory::class
            .asTypeName()
            .parameterizedBy(context.superInterface)

    val factoryName = generateFactoryName(context.implName)

    return PropertySpec
        .builder(factoryName, factoryType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(publishedApiAnnotation)
        .initializer(
            "%T { feature -> feature as? %T ?: %T(feature as %T) }",
            FeatureFactory::class,
            context.superInterface,
            context.implName,
            context.baseFeature.delegate,
        ).build()
}

/**
 * Generates internal matchers property for matching features by name and validation.
 */
context(context: SymbolContext)
fun internalMatchersProperty(): PropertySpec {
    val featureMatcherMemberByName = FeatureMatcher.Companion::class.asClassName().member("byName")
    val featureMatcherMemberByValidation = FeatureMatcher.Companion::class.asClassName().member("byValidation")

    val matchersName = generateMatchersName(context.implName)
    val propertyType = indexedOrDirectType(context.isIndexed, FEATURE_MATCHERS_CLASS)

    val initializer =
        if (context.isIndexed) {
            CodeBlock
                .builder()
                .add("{ index ->\n")
                .indent()
                .add("%T(\n", FEATURE_MATCHERS_CLASS)
                .indent()
                .add("byName = %M(%S.replace(\"{}\", index.toString())),\n", featureMatcherMemberByName, context.featureName)
                .add("byWildcardName = %M(%S),\n", featureMatcherMemberByName, context.featureName)
                .add("byValidation = %M(%T),\n", featureMatcherMemberByValidation, context.implCompanion)
                .unindent()
                .add(")")
                .unindent()
                .add("\n}")
                .build()
        } else {
            CodeBlock
                .builder()
                .add("%T(\n", FEATURE_MATCHERS_CLASS)
                .indent()
                .add("byName = %M(%S),\n", featureMatcherMemberByName, context.featureName)
                .add("byWildcardName = %M(%S),\n", featureMatcherMemberByName, context.featureName)
                .add("byValidation = %M(%T),\n", featureMatcherMemberByValidation, context.implCompanion)
                .unindent()
                .add(")")
                .build()
        }

    return PropertySpec
        .builder(matchersName, propertyType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(publishedApiAnnotation)
        .initializer(initializer)
        .build()
}

/**
 * Generates internal utils property combining factory, matchers, and validation.
 */
context(context: SymbolContext)
fun internalUtilsProperty(): PropertySpec {
    val utilsName = generateUtilsName(context.implName)
    val factoryName = generateFactoryName(context.implName)
    val matchersName = generateMatchersName(context.implName)

    val propertyType = indexedOrDirectType(context.isIndexed, FEATURE_UTILS_CLASS)

    val initializer =
        if (context.isIndexed) {
            CodeBlock
                .builder()
                .add("{ index ->\n")
                .indent()
                .add("%T(\n", FEATURE_UTILS_CLASS)
                .indent()
                .add("factory = %N,\n", factoryName)
                .add("matchers = %N(index),\n", matchersName)
                .add("validation = %T,\n", context.implCompanion)
                .unindent()
                .add(")")
                .unindent()
                .add("\n}")
                .build()
        } else {
            CodeBlock
                .builder()
                .add("%T(\n", FEATURE_UTILS_CLASS)
                .indent()
                .add("factory = %N,\n", factoryName)
                .add("matchers = %N,\n", matchersName)
                .add("validation = %T,\n", context.implCompanion)
                .unindent()
                .add(")")
                .build()
        }

    return PropertySpec
        .builder(utilsName, propertyType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(publishedApiAnnotation)
        .initializer(initializer)
        .build()
}

/**
 * Generates internal command factory properties for each nested command.
 */
context(context: SymbolContext)
fun internalCommandFactoryProperties(): List<PropertySpec> =
    context
        .nestedCommands
        .map { commandContext ->
            val commandName = commandContext.implType.simpleName.replaceFirstChar { it.lowercase() }
            val factoryName = generateCommandFactoryName(context.implName, commandName)
            val factoryType =
                LambdaTypeName.get(
                    parameters = listOf(ParameterSpec.unnamed(typeNameOf<Command>())),
                    returnType = commandContext.superInterface,
                )

            PropertySpec
                .builder(factoryName, factoryType)
                .addModifiers(KModifier.INTERNAL)
                .addAnnotation(publishedApiAnnotation)
                .initializer("%L", commandContext.implType.constructorReference())
                .build()
        }

/**
 * Generates extension properties for command companion objects (factory and validationRule).
 */
context(context: SymbolContext)
fun commandExtensions(): List<PropertySpec> =
    context
        .nestedCommands
        .map { commandContext ->
            val commandName = commandContext.implType.simpleName.replaceFirstChar { it.lowercase() }
            val factoryName = generateCommandFactoryName(context.implName, commandName)
            val factoryType =
                LambdaTypeName.get(
                    parameters = listOf(ParameterSpec.unnamed(typeNameOf<Command>())),
                    returnType = commandContext.superInterface,
                )

            listOf(
                PropertySpec
                    .builder("factory", factoryType)
                    .receiver(commandContext.superInterface.nestedClass("Companion"))
                    .getter(
                        FunSpec
                            .getterBuilder()
                            .addModifiers(KModifier.INLINE)
                            .addStatement("return %N", factoryName)
                            .build(),
                    ).build(),
                PropertySpec
                    .builder(
                        "validationRule",
                        ValidationRule::class
                            .asClassName()
                            .parameterizedBy(
                                typeNameOf<Command>(),
                                typeNameOf<ValidationError>(),
                            ),
                    ).receiver(commandContext.superInterface.nestedClass("Companion"))
                    .getter(
                        FunSpec
                            .getterBuilder()
                            .addModifiers(KModifier.INLINE)
                            .addStatement("return %T ", commandContext.implType)
                            .build(),
                    ).build(),
            )
        }.flatten()

/**
 * Generates extension properties for feature companion objects.
 * Includes factory, validationRule, featureName, matchers, and utils properties.
 */
context(context: SymbolContext)
fun featureExtensions(): List<PropertySpec> {
    val baseType =
        FeatureFactory::class
            .asTypeName()
            .parameterizedBy(context.superInterface)

    val factoryName = generateFactoryName(context.implName)

    val funSpec =
        FunSpec
            .getterBuilder()
            .addStatement("return %N", factoryName)
            .addModifiers(KModifier.INLINE)
            .build()

    val factoryProperty =
        PropertySpec
            .builder(
                "factory",
                baseType,
            ).receiver(context.superInterfaceCompanion)
            .getter(funSpec)
            .build()

    val validationRuleProperty =
        PropertySpec
            .builder(
                "validationRule",
                ValidationRule::class
                    .asClassName()
                    .parameterizedBy(
                        typeNameOf<Feature>(),
                        typeNameOf<ValidationError>(),
                    ),
            ).receiver(context.superInterfaceCompanion)
            .getter(
                FunSpec
                    .getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %T", context.implCompanion)
                    .build(),
            ).build()

    val baseProperties = listOf(factoryProperty, validationRuleProperty)

    val matchersName = generateMatchersName(context.implName)
    val matchersType = indexedOrDirectType(context.isIndexed, FEATURE_MATCHERS_CLASS)

    val featureNameProperty =
        PropertySpec
            .builder("featureName", String::class)
            .receiver(context.superInterfaceCompanion)
            .getter(
                FunSpec
                    .getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %S", context.featureName)
                    .build(),
            ).build()

    val matchersProperty =
        PropertySpec
            .builder("matchers", matchersType)
            .receiver(context.superInterfaceCompanion)
            .getter(
                FunSpec
                    .getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %N", matchersName)
                    .build(),
            ).build()

    val utilsName = generateUtilsName(context.implName)
    val utilsType = indexedOrDirectType(context.isIndexed, FEATURE_UTILS_CLASS)

    val utilsProperty =
        PropertySpec
            .builder("utils", utilsType)
            .receiver(context.superInterfaceCompanion)
            .getter(
                FunSpec
                    .getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %N", utilsName)
                    .build(),
            ).build()

    return baseProperties + listOf(featureNameProperty, matchersProperty, utilsProperty)
}

/**
 * Generates complete feature implementation including:
 * - Main implementation class
 * - Factory functions
 * - Matchers for feature lookup
 * - Utils combining factory, matchers, and validation
 * - Command implementations
 * - Extension properties
 *
 * @param context The symbol context with all feature information
 * @return FileSpec containing the complete feature implementation
 */
@OptIn(DelicateKotlinPoetApi::class)
fun generateFeatureImplementation(context: SymbolContext) =
    context(context) {
        val constructor = constructor()
        val properties = context.allPropertiesImpl
        val initBlock = initBlock()

        val classImpl =
            TypeSpec
                .classBuilder(context.implName)
                .addModifiers(KModifier.INTERNAL, KModifier.DATA)
                .addAnnotation(publishedApiAnnotation)
                .primaryConstructor(constructor)
                .addSuperinterface(context.symbol.toClassName())
                .addTypes(
                    context
                        .nestedCommands
                        .map(::generateCommandImplementation),
                ).addType(companionObject())
                .addProperty(
                    PropertySpec
                        .builder("delegate", context.baseFeature.delegate)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("delegate")
                        .build(),
                ).addProperties(properties)
                .apply {
                    if (initBlock.isNotEmpty()) {
                        addInitializerBlock(initBlock)
                    }
                }.addFunction(
                    FunSpec
                        .builder("equals")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("other", Any::class.asClassName().copy(nullable = true))
                        .returns(Boolean::class)
                        .addStatement("if (other === this) return true")
                        .addStatement("if (other !is %T) return false", typeNameOf<Feature>())
                        .addStatement(
                            """
                            return feature == other.feature &&
                                   wildcardFeature == other.wildcardFeature &&
                                   isEnabled == other.isEnabled &&
                                   isReady == other.isReady &&
                                   apiVersion == other.apiVersion &&
                                   timestamp == other.timestamp &&
                                   uri == other.uri &&
                                   properties == other.properties &&
                                   commands == other.commands &&
                                   deviceId == other.deviceId &&
                                   gatewayId == other.gatewayId &&
                                   isActive == other.isActive
                            """.trimIndent(),
                        ).build(),
                ).addFunction(
                    FunSpec
                        .builder("hashCode")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Int::class)
                        .addStatement("return delegate.hashCode()")
                        .build(),
                ).build()

        FileSpec
            .builder(context.implName)
            .addType(classImpl)
            .apply {
                addProperty(internalFactoryProperty())
                addProperty(internalMatchersProperty())
                addProperty(internalUtilsProperty())
            }.addProperties(internalCommandFactoryProperties())
            .addProperties(commandExtensions())
            .addProperties(featureExtensions())
            .addImport("xyz.dussim.viessmann.feature.api.validation", "invoke")
            .build()
    }

/**
 * Combines two integers into a single long value for efficient map lookups.
 * Used for property/command name hashing.
 */
internal fun combineToLong(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)
