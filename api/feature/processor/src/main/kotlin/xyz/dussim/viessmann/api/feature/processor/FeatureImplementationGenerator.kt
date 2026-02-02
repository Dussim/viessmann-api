package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
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
 * When using abstract class, only delegate parameter is needed.
 * Otherwise, all feature parameters and hashCode are included.
 */
context(context: SymbolContext)
fun constructor(): FunSpec {
    val builder =
        FunSpec
            .constructorBuilder()
            .addParameter(
                ParameterSpec
                    .builder(DELEGATE, context.baseFeature.delegate)
                    .build(),
            )

    if (context.baseFeature.abstractClass == null) {
        builder.addParameters(context.featureParametersImpl)
        builder.addParameter(ParameterSpec("hashCode", INT))
    }

    return builder.build()
}

/**
 * Generates initialization block that validates and assigns properties and commands.
 * Throws FeatureValidationException if validation fails.
 */
context(context: SymbolContext)
fun initBlock(implName: ClassName): CodeBlock {
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
            addValidationException(implName)
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
private fun CodeBlock.Builder.addValidationException(implName: ClassName) {
    add(
        "throw %T(%S, validate($DELEGATE))\n",
        FeatureValidationException::class.asTypeName(),
        implName.simpleName.replace("_", "").removeSuffix("Impl"),
    )
}

/**
 * Generates companion object that implements validation rules for the feature.
 */
context(context: SymbolContext)
fun companionObject(implName: ClassName): TypeSpec {
    val subTypeValidationMember =
        when (context.baseFeature.delegate) {
            typeNameOf<Feature.Device>() -> DEVICE_FEATURE_RULE
            typeNameOf<Feature.Gateway>() -> GATEWAY_FEATURE_RULE
            typeNameOf<Feature.Geofencing>() -> GEOFENCING_FEATURE_RULE
            else -> error("Unreachable")
        }

    val ruleExpressions =
        listOf(
            CodeBlock.of(
                "%M",
                context.ruleRegistry.register(subTypeValidationMember, emptyList(), FEATURE_VALIDATION_RULE_TYPE),
            ),
        ).plus(
            context
                .parameterProperties
                .map {
                    CodeBlock.of(
                        "%M",
                        context.ruleRegistry.register(it.validationFunction, listOf(it.name), FEATURE_VALIDATION_RULE_TYPE),
                    )
                },
        ).plus(
            context
                .nestedCommands
                .map {
                    CodeBlock.of("%T.rule", it.implType)
                },
        )

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(FEATURE_VALIDATION_RULE_TYPE)
        .addFunction(generateValidateFunction(typeNameOf<Feature>(), ruleExpressions))
        .build()
}

/**
 * Generates extension properties for feature companion objects.
 * Includes descriptor property.
 */
context(context: SymbolContext)
fun featureExtensions(
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
) = context(context) {
    val constructor = constructor()
    val properties = context.parameterPropertiesImpl + context.commandPropertiesImpl
    val initBlock = initBlock(implName)
    val abstractClass = context.baseFeature.abstractClass

    val classImpl =
        TypeSpec
            .classBuilder(implName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(PUBLISHED_API_ANNOTATION)
            .primaryConstructor(constructor)
            .apply {
                if (abstractClass != null) {
                    superclass(abstractClass)
                    addSuperclassConstructorParameter("$DELEGATE")
                    addSuperinterfaces(superInterfaces.filter { it != context.baseFeature.delegate })
                } else {
                    addSuperinterfaces(superInterfaces)
                    addProperties(context.featurePropertiesImpl)
                    addProperty(PropertySpec.builder("hashCode", INT, KModifier.PRIVATE).initializer("hashCode").build())
                    addFunction(
                        FunSpec
                            .builder("equals")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("other", Any::class.asClassName().copy(nullable = true))
                            .returns(Boolean::class)
                            .addStatement("return %M(other)", EQUALS_IMPL)
                            .build(),
                    )
                    addFunction(
                        FunSpec
                            .builder("hashCode")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(Int::class)
                            .addStatement("return hashCode")
                            .build(),
                    )
                }
            }.addType(companionObject(implName))
            .addProperties(properties)
            .apply {
                if (initBlock.isNotEmpty()) {
                    addInitializerBlock(initBlock)
                }
            }.build()

    FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(classImpl)
        .build()
}

/**
 * Generates feature descriptor and extension property for a specific feature interface.
 *
 * @param context The symbol context for the feature
 * @param implName The name of the shared implementation class
 * @return FileSpec containing the descriptor and extensions
 */
fun generateFeatureDescriptorAndExtensions(
    context: SymbolContext,
    implName: ClassName,
) = context(context) {
    val descriptorName = generateDescriptorName(context.superInterface)
    val descriptorType = featureDescriptorType(context.superInterface, context.isIndexed)
    val abstractClass = context.baseFeature.abstractClass

    val descriptorProperty =
        PropertySpec
            .builder(descriptorName, descriptorType)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(PUBLISHED_API_ANNOTATION)
            .initializer(
                buildCodeBlock {
                    add("%M(\n", FEATURE_DESCRIPTOR_FACTORY)
                    indent()
                    add("wildcardName = %S,\n", context.featureName)
                    add("rule = %T,\n", implName)
                    unindent()
                    add(") { feature ->\n")
                    indent()
                    if (abstractClass != null) {
                        add("feature as? %T ?: %T(feature as %T)\n", context.superInterface, implName, context.baseFeature.delegate)
                    } else {
                        add("feature as? %T ?: %T(\n", context.superInterface, implName)
                        indent()
                        add("delegate = feature as %T,\n", context.baseFeature.delegate)
                        context.featureProperties.forEach {
                            add("${it.name} = feature.${it.name},\n")
                        }
                        add("hashCode = feature.hashCode(),\n")
                        unindent()
                        add(")\n")
                    }
                    unindent()
                    add("}")
                    add(" as %T", descriptorType)
                },
            ).build()

    FileSpec
        .builder(context.implName.packageName, context.implName.simpleName)
        .addProperty(descriptorProperty)
        .addProperties(featureExtensions(descriptorName, descriptorType))
        .build()
}
