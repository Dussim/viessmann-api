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

/**
 * Generates constructor for command implementation.
 */
context(context: CommandSymbolContext)
fun constructor() =
    FunSpec
        .constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder(COMMAND, typeNameOf<Command>())
                .build(),
        ).build()

/**
 * Generates command property that overrides the base command.
 */
context(context: CommandSymbolContext)
fun constructorProperty() =
    PropertySpec
        .builder(COMMAND, typeNameOf<Command>())
        .addModifiers(KModifier.OVERRIDE)
        .initializer(COMMAND)
        .build()

/**
 * Generates init block that extracts and validates command constraints.
 * Throws CommandValidationException if constraints are invalid.
 */
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
            "throw %T(%S, validate(command).map { it.toString() })\n",
            CommandValidationException::class.asTypeName(),
            context.superInterface,
        ).endControlFlow()
        .build()

/**
 * Generates function to validate command from feature context.
 */
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
                    "return %M.validate(feature)",
                    commandRule,
                ),
        ).build()

/**
 * Generates companion object with validation rules for command constraints.
 */
context(context: CommandSymbolContext)
fun companionObject(): TypeSpec {
    val commandValidationRuleType =
        ValidationRule::class
            .asClassName()
            .parameterizedBy(
                typeNameOf<Command>(),
                typeNameOf<ValidationError>(),
            )

    val properties =
        context
            .constraintsProperties
            .map {
                PropertySpec
                    .builder(
                        generateConstraintRuleName(it.name),
                        commandValidationRuleType,
                    ).addModifiers(KModifier.PRIVATE)
                    .initializer(CodeBlock.of("%M(%S)", CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type), it.name))
                    .build()
            }.plus(
                PropertySpec
                    .builder(
                        "numberOfParametersRule",
                        commandValidationRuleType,
                    ).addModifiers(KModifier.PRIVATE)
                    .initializer(CodeBlock.of("%M(%L, commandName)", NUMBER_OF_PARAMETERS_RULE, context.constraintsProperties.size))
                    .build(),
            )

    val commandRuleProperty =
        PropertySpec
            .builder(
                "rule",
                ValidationRule::class
                    .asClassName()
                    .parameterizedBy(
                        typeNameOf<Feature>(),
                        typeNameOf<ValidationError>(),
                    ),
            ).addModifiers(KModifier.OVERRIDE)
            .initializer(CodeBlock.of("%M(%S, this)", commandRule, context.lowerCaseName))
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
                            properties.map { CodeBlock.of("${it.name}.validate(value),\n") },
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
        ).addProperty(commandRuleProperty)
        .addProperty(
            PropertySpec
                .builder("commandName", typeNameOf<String>())
                .addModifiers(KModifier.OVERRIDE)
                .initializer(CodeBlock.of("%S", context.lowerCaseName))
                .build(),
        ).addProperties(properties)
        .addFunction(validateFunction)
//        .addFunction(validateFromFeatureContextFunction())
        .build()
}

/**
 * Generates complete command implementation class.
 * Creates an internal class that implements the command interface with validation.
 *
 * @param context The command context with all necessary information
 * @return TypeSpec for the command implementation class
 */
fun generateCommandImplementation(context: CommandSymbolContext) =
    context(context) {
        TypeSpec
            .classBuilder(context.implName)
            .addAnnotation(publishedApiAnnotation)
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
