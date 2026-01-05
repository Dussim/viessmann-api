package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.FeatureMatcher
import xyz.dussim.viessmann.feature.api.FeatureValidationException

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
        .addParameter(ParameterSpec("hashCode", INT))
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
    val combined = propertyHash(name.hashCode(), name.length)
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
    val name = property.name
    val propertyHash = propertyHash(name.hashCode(), name.length)
    add("$name = %T(delegate.commands[%S, %L]!!)\n", property.implType, name, propertyHash)
}

/**
 * Adds FeatureValidationException throw statement.
 */
context(context: SymbolContext)
private fun CodeBlock.Builder.addValidationException() {
    add(
        "throw %T(%S, validate($DELEGATE))\n",
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
            typeNameOf<Feature.Device>() -> DEVICE_FEATURE_RULE
            typeNameOf<Feature.Gateway>() -> GATEWAY_FEATURE_RULE
            typeNameOf<Feature.Geofencing>() -> GEOFENCING_FEATURE_RULE
            else -> error("Unreachable")
        }

    val properties =
        listOf(
            ruleProperty(
                "subTypeRule",
                FEATURE_VALIDATION_RULE_TYPE,
                CodeBlock.of("%M(%S)", subTypeValidationMember, context.implName),
            ),
        ).plus(
            context
                .parameterProperties
                .map {
                    ruleProperty(
                        generatePropertyRuleName(it.name),
                        FEATURE_VALIDATION_RULE_TYPE,
                        CodeBlock.of("%M(%S)", it.validationFunction, it.name),
                    )
                },
        ).plus(
            context
                .nestedCommands
                .map {
                    ruleProperty(
                        generatePropertyRuleName(it.lowerCaseName),
                        FEATURE_VALIDATION_RULE_TYPE,
                        CodeBlock.of("${it.implName}.rule"),
                    )
                },
        )

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(FEATURE_VALIDATION_RULE_TYPE)
        .addProperties(properties)
        .addFunction(generateValidateFunction(typeNameOf<Feature>(), properties))
        .build()
}

/**
 * Generates internal factory property for creating feature implementations.
 */
context(context: SymbolContext)
fun internalFactoryProperty(): PropertySpec {
    val factoryType = featureFactoryType(context.superInterface)

    val factoryName = generateFactoryName(context.implName)

    return PropertySpec
        .builder(factoryName, factoryType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(PUBLISHED_API_ANNOTATION)
        .initializer(
            buildCodeBlock {
                add("%T { feature ->\n", FeatureFactory::class)
                indent()
                add("feature as? %T ?: %T(\n", context.superInterface, context.implName)
                indent()
                add("delegate = feature as %T,\n", context.baseFeature.delegate)
                context.featureProperties.forEach {
                    add("${it.name} = feature.${it.name},\n")
                }
                add("hashCode = feature.hashCode(),\n")
                add(")\n")
                unindent()
                unindent()
                add("}")
            },
        ).build()
}

/**
 * Generates internal matchers property for matching features by name and validation.
 */
context(context: SymbolContext)
fun internalMatchersProperty(): PropertySpec {
    val featureMatcherMemberByName = FeatureMatcher.Companion::class.asClassName().member("byName")
    val featureMatcherMemberByWildcardName = FeatureMatcher.Companion::class.asClassName().member("byWildcardName")
    val featureMatcherMemberByValidation = FeatureMatcher.Companion::class.asClassName().member("byValidation")

    val matchersName = generateMatchersName(context.implName)
    val propertyType = indexedOrDirectType(context.isIndexed, FEATURE_MATCHERS_CLASS)

    val initializer =
        indexedOrDirectCodeBlock(context.isIndexed) { isIndexed ->
            CodeBlock
                .builder()
                .add("%T(\n", FEATURE_MATCHERS_CLASS)
                .indent()
                .apply {
                    if (isIndexed) {
                        add("byName = %M(%S.replace(\"{}\", index.toString())),\n", featureMatcherMemberByName, context.featureName)
                    } else {
                        add("byName = %M(%S),\n", featureMatcherMemberByName, context.featureName)
                    }
                }.add("byWildcardName = %M(%S),\n", featureMatcherMemberByWildcardName, context.featureName)
                .add("byValidation = %M(%T),\n", featureMatcherMemberByValidation, context.implCompanion)
                .unindent()
                .add(")")
                .build()
        }

    return PropertySpec
        .builder(matchersName, propertyType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(PUBLISHED_API_ANNOTATION)
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
        indexedOrDirectCodeBlock(context.isIndexed) { isIndexed ->
            CodeBlock
                .builder()
                .add("%T(\n", FEATURE_UTILS_CLASS)
                .indent()
                .add("factory = %N,\n", factoryName)
                .apply {
                    if (isIndexed) {
                        add("matchers = %N(index),\n", matchersName)
                    } else {
                        add("matchers = %N,\n", matchersName)
                    }
                }.add("validation = %T,\n", context.implCompanion)
                .unindent()
                .add(")")
                .build()
        }

    return PropertySpec
        .builder(utilsName, propertyType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(PUBLISHED_API_ANNOTATION)
        .initializer(initializer)
        .build()
}

/**
 * Generates extension properties for feature companion objects.
 * Includes factory, validationRule, featureName, matchers, and utils properties.
 */
context(context: SymbolContext)
fun featureExtensions(): List<PropertySpec> {
    val superCompanion = context.superInterfaceCompanion

    val factoryProperty =
        extensionProperty(
            "factory",
            superCompanion,
            featureFactoryType(context.superInterface),
            "return %N",
            generateFactoryName(context.implName),
        )

    val validationRuleProperty =
        extensionProperty(
            "validationRule",
            superCompanion,
            FEATURE_VALIDATION_RULE_TYPE,
            "return %T",
            context.implCompanion,
        )

    val featureNameProperty =
        extensionProperty(
            "featureName",
            superCompanion,
            typeNameOf<String>(),
            "return %S",
            context.featureName,
        )

    val matchersProperty =
        extensionProperty(
            "matchers",
            superCompanion,
            indexedOrDirectType(context.isIndexed, FEATURE_MATCHERS_CLASS),
            "return %N",
            generateMatchersName(context.implName),
        )

    val utilsProperty =
        extensionProperty(
            "utils",
            superCompanion,
            indexedOrDirectType(context.isIndexed, FEATURE_UTILS_CLASS),
            "return %N",
            generateUtilsName(context.implName),
        )

    return listOf(factoryProperty, validationRuleProperty, featureNameProperty, matchersProperty, utilsProperty)
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
fun generateFeatureImplementation(context: SymbolContext) =
    context(context) {
        val constructor = constructor()
        val properties = context.allPropertiesImpl
        val initBlock = initBlock()

        val classImpl =
            TypeSpec
                .classBuilder(context.implName)
                .addModifiers(KModifier.INTERNAL)
                .addAnnotation(PUBLISHED_API_ANNOTATION)
                .primaryConstructor(constructor)
                .addSuperinterface(context.symbol.toClassName())
                .addTypes(
                    context
                        .nestedCommands
                        .map(::generateCommandImplementation),
                ).addType(companionObject())
                .addProperties(properties)
                .addProperty(PropertySpec.builder("hashCode", INT, KModifier.PRIVATE).initializer("hashCode").build())
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
                        .addStatement("return %M(other)", EQUALS_IMPL)
                        .build(),
                ).addFunction(
                    FunSpec
                        .builder("hashCode")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Int::class)
                        .addStatement("return hashCode")
                        .build(),
                ).build()

        FileSpec
            .builder(context.implName)
            .addType(classImpl)
            .apply {
                addProperty(internalFactoryProperty())
                addProperty(internalMatchersProperty())
                addProperty(internalUtilsProperty())
            }.addProperties(featureExtensions())
            .build()
    }
