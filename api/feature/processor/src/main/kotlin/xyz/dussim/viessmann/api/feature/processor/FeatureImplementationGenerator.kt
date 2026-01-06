package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Feature
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
 * Generates internal descriptor property for the feature.
 */
context(context: SymbolContext)
fun internalDescriptorProperty(): PropertySpec {
    val descriptorName = generateDescriptorName(context.implName)
    val descriptorType = featureDescriptorType(context.superInterface, context.isIndexed)

    return PropertySpec
        .builder(descriptorName, descriptorType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(PUBLISHED_API_ANNOTATION)
        .initializer(
            buildCodeBlock {
                add("%M(\n", FEATURE_DESCRIPTOR_FACTORY)
                indent()
                add("wildcardName = %S,\n", context.featureName)
                add("rule = %T,\n", context.implName)
                unindent()
                add(") { feature ->\n")
                indent()
                add("feature as? %T ?: %T(\n", context.superInterface, context.implName)
                indent()
                add("delegate = feature as %T,\n", context.baseFeature.delegate)
                context.featureProperties.forEach {
                    add("${it.name} = feature.${it.name},\n")
                }
                add("hashCode = feature.hashCode(),\n")
                unindent()
                add(")\n")
                unindent()
                add("}")
                add(" as %T", descriptorType)
            },
        ).build()
}

/**
 * Generates extension properties for feature companion objects.
 * Includes descriptor property.
 */
context(context: SymbolContext)
fun featureExtensions(): List<PropertySpec> {
    val descriptorName = generateDescriptorName(context.implName)
    val descriptorType = featureDescriptorType(context.superInterface, context.isIndexed)
    return listOf(
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
}

/**
 * Generates complete feature implementation including:
 * - Main implementation class
 * - Factory functions
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
                addProperty(internalDescriptorProperty())
            }.addProperties(featureExtensions())
            .build()
    }
