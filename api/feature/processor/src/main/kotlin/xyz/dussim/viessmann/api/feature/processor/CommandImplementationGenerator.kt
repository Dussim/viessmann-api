package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.CommandValidationException
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.validation.CommandValidationRule
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

private val NUMBER_OF_PARAMETERS_RULE = MemberName(VALIDATION_PACKAGE, "numberOfParametersRule")

private val CONSTRAINTS_VALIDATION_FUNCTIONS =
    mapOf(
        typeNameOf<StringConstraints>() to MemberName(VALIDATION_PACKAGE, "stringConstraintsRule"),
        typeNameOf<NumberConstraints>() to MemberName(VALIDATION_PACKAGE, "numberConstraintsRule"),
        typeNameOf<BooleanConstraints>() to MemberName(VALIDATION_PACKAGE, "booleanConstraintsRule"),
        typeNameOf<ScheduleConstraints>() to MemberName(VALIDATION_PACKAGE, "scheduleConstraintsRule"),
    )

context(context: CommandSymbolContext)
fun constructor() =
    FunSpec
        .constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder(COMMAND, typeNameOf<Command>())
                .build(),
        ).build()

context(context: CommandSymbolContext)
fun constructorProperty() =
    PropertySpec
        .builder(COMMAND, typeNameOf<Command>())
        .addModifiers(KModifier.OVERRIDE)
        .initializer(COMMAND)
        .build()

context(context: CommandSymbolContext)
fun initBlock() =
    CodeBlock
        .builder()
        .beginControlFlow("try")
        .apply {
            context.constraintsProperties
                .forEach {
                    val combined = combineToLong(it.name.hashCode(), it.name.length)
                    add("${it.name} = command.params[%S, %L]!!.constraints as %T\n", it.name, combined, it.type)
                }
        }.nextControlFlow("catch (_: Exception)")
        .add(
            "throw %T(%S, invoke(command).map { it.toString() })\n",
            CommandValidationException::class.asTypeName(),
            context.superInterface,
        ).endControlFlow()
        .build()

context(context: CommandSymbolContext)
fun validateFromFeatureContextFunction() =
    FunSpec
        .builder("validateFromFeatureContext")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter(
            ParameterSpec
                .builder("feature", typeNameOf<Feature>())
                .build(),
        ).returns(
            ValidationResult::class
                .asTypeName()
                .parameterizedBy(
                    typeNameOf<ValidationError>(),
                ),
        ).addCode(
            CodeBlock
                .of(
                    "return %M(feature)",
                    commandRule,
                ),
        ).build()

context(context: CommandSymbolContext)
fun companionObject(): TypeSpec {
    val properties =
        context
            .constraintsProperties
            .map {
                PropertySpec
                    .builder(
                        "${it.name}ConstraintRule",
                        ValidationRule::class
                            .asClassName()
                            .parameterizedBy(
                                typeNameOf<Command>(),
                                typeNameOf<ValidationError>(),
                            ),
                    ).addModifiers(KModifier.PRIVATE)
                    .initializer(CodeBlock.of("%M(%S)", CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type), it.name))
                    .build()
            }.plus(
                PropertySpec
                    .builder(
                        "numberOfParametersRule",
                        ValidationRule::class
                            .asClassName()
                            .parameterizedBy(
                                typeNameOf<Command>(),
                                typeNameOf<ValidationError>(),
                            ),
                    ).addModifiers(KModifier.PRIVATE)
                    .initializer(CodeBlock.of("%M(%L, commandName)", NUMBER_OF_PARAMETERS_RULE, context.constraintsProperties.size))
                    .build(),
            )

    val commandRuleProperty =
        PropertySpec
            .builder(
                "commandRule",
                ValidationRule::class
                    .asClassName()
                    .parameterizedBy(
                        typeNameOf<Feature>(),
                        typeNameOf<ValidationError>(),
                    ),
            ).addModifiers(KModifier.PRIVATE)
            .initializer(CodeBlock.of("%M(commandName, this)", commandRule))
            .build()

    val validateFunction =
        FunSpec
            .builder("validate")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(
                ParameterSpec
                    .builder("value", typeNameOf<Command>())
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
                            properties.map { CodeBlock.of("${it.name}(value),\n") },
                        ),
                    ).build(),
            ).build()

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(typeNameOf<CommandValidationRule>())
        .addSuperinterface(
            ValidationRule::class
                .asTypeName()
                .parameterizedBy(
                    typeNameOf<Command>(),
                    typeNameOf<ValidationError>(),
                ),
        ).addProperty(
            PropertySpec
                .builder("commandName", typeNameOf<String>())
                .addModifiers(KModifier.OVERRIDE)
                .initializer(CodeBlock.of("%S", context.name.replaceFirstChar(Char::lowercaseChar)))
                .build(),
        ).addProperties(properties)
        .addProperty(commandRuleProperty)
        .addFunction(validateFunction)
        .addFunction(validateFromFeatureContextFunction())
        .build()
}

fun generateCommandImplementation(context: CommandSymbolContext) =
    context(context) {
        TypeSpec
            .classBuilder(context.implName)
            .addAnnotation(PublishedApi::class)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(context.superInterface)
            .primaryConstructor(constructor())
            .addProperty(constructorProperty())
            .addProperties(context.allPropertiesImpl)
            .apply {
                if (context.constraintsProperties.isNotEmpty()) {
                    addInitializerBlock(initBlock())
                }
            }.addType(companionObject())
            .build()
    }
