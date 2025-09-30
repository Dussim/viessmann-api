package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
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
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.FeatureMatcher
import xyz.dussim.viessmann.feature.api.FeatureMatchers
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import xyz.dussim.viessmann.feature.api.Property
import xyz.dussim.viessmann.feature.api.UnsafeFactoryCreationMethod
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

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

context(context: SymbolContext)
fun constructor() =
    FunSpec
        .constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder(DELEGATE, context.baseFeature.delegate)
                .build(),
        ).build()

context(context: SymbolContext)
fun initBlock(): CodeBlock {
    val parameterPropertiesAssignments =
        context
            .parameterProperties
            .map {
                val (name, type, _, isListProperty, isEnumProperty) = it
                if (isEnumProperty) {
                    CodeBlock.of("$name = %T(%S)\n", type, name)
                } else if (isListProperty) {
                    CodeBlock.of("$name = delegate.properties[%1S]!!.value as? %2T ?: %2T.EMPTY\n", name, type)
                } else {
                    CodeBlock.of("$name = delegate.properties[%S]!!.value as %T\n", name, type)
                }
            }

    val commandsAssignments =
        context
            .commandProperties
            .map { (name, type, _) ->
                CodeBlock.of("$name = %T.factory(delegate.commands[%S]!!)\n", type, name)
            }

    val assignments = commandsAssignments + parameterPropertiesAssignments

    if (assignments.isEmpty()) {
        return CodeBlock.of("")
    }

    return CodeBlock
        .builder()
        .beginControlFlow("try")
        .apply { assignments.forEach(::add) }
        .nextControlFlow("catch (_: Exception)")
        .add(
            "throw %T(%S, invoke($DELEGATE).map { it.toString() })\n",
            FeatureValidationException::class.asTypeName(),
            context.name.asString(),
        ).endControlFlow()
        .build()
}

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

    val propertyType =
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
                    propertyType,
                ).addModifiers(KModifier.PRIVATE)
                .initializer("%M(%S)", subTypeValidationMember, context.implName)
                .build(),
        ) +
            context
                .parameterProperties
                .map {
                    val (name, _, _, _) = it
                    PropertySpec
                        .builder(
                            "${name}PropertyRule",
                            propertyType,
                        ).addModifiers(KModifier.PRIVATE)
                        .initializer("%M(%S)", it.validationFunction, name)
                        .build()
                }

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(
            propertyType,
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
                                properties.map { CodeBlock.of("${it.name}(value),\n") } + commandsValidation,
                            ),
                        ).build(),
                ).build(),
        ).build()
}

context(context: SymbolContext)
fun internalFactoryProperty(): PropertySpec {
    val factoryType =
        FeatureFactory::class
            .asTypeName()
            .parameterizedBy(context.superInterface)

    val factoryName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Factory"

    return PropertySpec
        .builder(factoryName, factoryType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(
            AnnotationSpec
                .builder(PublishedApi::class)
                .build(),
        ).initializer(
            "%T { feature -> feature as? %T ?: %T(feature as %T) }",
            FeatureFactory::class,
            context.superInterface,
            context.implName,
            context.baseFeature.delegate,
        ).build()
}

context(context: SymbolContext)
fun internalMatchersProperty(): PropertySpec {
    val featureMatcherMemberByName = FeatureMatcher.Companion::class.asClassName().member("byName")
    val featureMatcherMemberByValidation = FeatureMatcher.Companion::class.asClassName().member("byValidation")
    val featureMatchersClass = typeNameOf<FeatureMatchers>()

    val matchersName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Matchers"

    val isIndexed = context.featureName.contains("{}")

    val propertyType =
        if (isIndexed) {
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(INT)),
                returnType = featureMatchersClass,
            )
        } else {
            featureMatchersClass
        }

    val initializer =
        if (isIndexed) {
            CodeBlock
                .builder()
                .add("{ index ->\n")
                .indent()
                .add("%T(\n", featureMatchersClass)
                .indent()
                .add("byName = %M(%S.replace(\"{}\", index.toString())),\n", featureMatcherMemberByName, context.featureName)
                .add("byValidation = %M(%T),\n", featureMatcherMemberByValidation, context.implCompanion)
                .unindent()
                .add(")")
                .unindent()
                .add("\n}")
                .build()
        } else {
            CodeBlock
                .builder()
                .add("%T(\n", featureMatchersClass)
                .indent()
                .add("byName = %M(%S),\n", featureMatcherMemberByName, context.featureName)
                .add("byValidation = %M(%T),\n", featureMatcherMemberByValidation, context.implCompanion)
                .unindent()
                .add(")")
                .build()
        }

    return PropertySpec
        .builder(matchersName, propertyType)
        .addModifiers(KModifier.INTERNAL)
        .addAnnotation(
            AnnotationSpec
                .builder(PublishedApi::class)
                .build(),
        ).initializer(initializer)
        .build()
}

context(context: SymbolContext)
fun internalUtilsProperty(): PropertySpec {
    val featureUtilsClass = ClassName("xyz.dussim.viessmann.feature.api", "FeatureUtils")
    val featureMatchersClass = typeNameOf<FeatureMatchers>()

    val utilsName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Utils"
    val factoryName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Factory"
    val matchersName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Matchers"

    val isIndexed = context.featureName.contains("{}")

    val propertyType =
        if (isIndexed) {
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(INT)),
                returnType = featureUtilsClass,
            )
        } else {
            featureUtilsClass
        }

    val initializer =
        if (isIndexed) {
            CodeBlock
                .builder()
                .add("{ index ->\n")
                .indent()
                .add("%T(\n", featureUtilsClass)
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
                .add("%T(\n", featureUtilsClass)
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
        .addAnnotation(
            AnnotationSpec
                .builder(PublishedApi::class)
                .build(),
        ).initializer(initializer)
        .build()
}

context(context: SymbolContext)
fun internalCommandFactoryProperties(): List<PropertySpec> =
    context
        .nestedCommands
        .map { commandContext ->
            val featurePrefix =
                context.implName.simpleName
                    .replaceFirstChar { it.lowercase() }
                    .removeSuffix("Impl")
            val commandName = commandContext.implType.simpleName.replaceFirstChar { it.lowercase() }
            val factoryName = featurePrefix + commandName.replaceFirstChar { it.uppercase() } + "Factory"
            val factoryType =
                LambdaTypeName.get(
                    parameters = listOf(ParameterSpec.unnamed(typeNameOf<Command>())),
                    returnType = commandContext.superInterface,
                )

            PropertySpec
                .builder(factoryName, factoryType)
                .addModifiers(KModifier.INTERNAL)
                .addAnnotation(
                    AnnotationSpec
                        .builder(PublishedApi::class)
                        .build(),
                ).initializer("%L", commandContext.implType.constructorReference())
                .build()
        }

context(context: SymbolContext)
fun commandExtensions(): List<PropertySpec> =
    context
        .nestedCommands
        .map { commandContext ->
            val featurePrefix =
                context.implName.simpleName
                    .replaceFirstChar { it.lowercase() }
                    .removeSuffix("Impl")
            val commandName = commandContext.implType.simpleName.replaceFirstChar { it.lowercase() }
            val factoryName = featurePrefix + commandName.replaceFirstChar { it.uppercase() } + "Factory"
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

context(context: SymbolContext)
fun featureExtensions(): List<PropertySpec> {
    val baseType =
        FeatureFactory::class
            .asTypeName()
            .parameterizedBy(context.superInterface)

    val factoryName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Factory"

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

    val matchersName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Matchers"
    val featureMatchersClass = ClassName("xyz.dussim.viessmann.feature.api", "FeatureMatchers")

    val isIndexed = context.featureName.contains("{}")

    val matchersType =
        if (isIndexed) {
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(INT)),
                returnType = featureMatchersClass,
            )
        } else {
            featureMatchersClass
        }

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

    val utilsName = context.implName.simpleName.replaceFirstChar { it.lowercase() } + "Utils"
    val featureUtilsClass = ClassName("xyz.dussim.viessmann.feature.api", "FeatureUtils")

    val utilsType =
        if (isIndexed) {
            LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(INT)),
                returnType = featureUtilsClass,
            )
        } else {
            featureUtilsClass
        }

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

@OptIn(DelicateKotlinPoetApi::class)
fun generateFeatureImplementation(context: SymbolContext) =
    context(context) {
        val constructor = constructor()
        val properties = context.allPropertiesImpl
        val initBlock = initBlock()

        val classImpl =
            TypeSpec
                .classBuilder(context.implName)
                .addModifiers(KModifier.INTERNAL)
                .addAnnotation(
                    AnnotationSpec
                        .builder(PublishedApi::class)
                        .build(),
                ).primaryConstructor(constructor)
                .addSuperinterface(context.symbol.toClassName())
                .addAnnotation(
                    AnnotationSpec
                        .builder(ClassName("kotlin", "OptIn"))
                        .addMember("markerClass = [%T::class]", UnsafeFactoryCreationMethod::class.asClassName())
                        .build(),
                ).addTypes(
                    context
                        .nestedCommands
                        .map(::generateCommandImplementation),
                ).addType(companionObject())
                .addProperties(properties)
                .apply {
                    if (initBlock.isNotEmpty()) {
                        addInitializerBlock(initBlock)
                    }
                }.build()

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
