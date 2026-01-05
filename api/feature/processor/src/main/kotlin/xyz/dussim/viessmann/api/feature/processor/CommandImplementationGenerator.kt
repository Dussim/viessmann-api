package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.CommandValidationException
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.validation.CommandValidationRule

private val NUMBER_OF_PARAMETERS_RULE = validationRule("numberOfParametersRule")

private val CONSTRAINTS_VALIDATION_FUNCTIONS =
    mapOf(
        typeNameOf<StringConstraints>() to validationRule("stringConstraintsRule"),
        typeNameOf<NumberConstraints>() to validationRule("numberConstraintsRule"),
        typeNameOf<BooleanConstraints>() to validationRule("booleanConstraintsRule"),
        typeNameOf<ScheduleConstraints>() to validationRule("scheduleConstraintsRule"),
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
fun constructorProperty() = overrideProperty(COMMAND, typeNameOf<Command>(), COMMAND)

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
                    val combined = propertyHash(it.name.hashCode(), it.name.length)
                    add("${it.name} = command.params[%S, %L]!!.constraints as %T\n", it.name, combined, it.type)
                }
        }.nextControlFlow("catch (_: Exception)")
        .add(
            "throw %T(%S, validate(command))\n",
            CommandValidationException::class.asTypeName(),
            context.superInterface,
        ).endControlFlow()
        .build()

/**
 * Generates companion object with validation rules for command constraints.
 */
context(context: CommandSymbolContext)
fun companionObject(): TypeSpec {
    val properties =
        context
            .constraintsProperties
            .map {
                ruleProperty(
                    generateConstraintRuleName(it.name),
                    COMMAND_VALIDATION_RULE_TYPE,
                    CodeBlock.of("%M(%S)", CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type), it.name),
                )
            }.plus(
                ruleProperty(
                    "numberOfParametersRule",
                    COMMAND_VALIDATION_RULE_TYPE,
                    CodeBlock.of("%M(%L, %S)", NUMBER_OF_PARAMETERS_RULE, context.constraintsProperties.size, context.lowerCaseName),
                ),
            )

    val commandRuleProperty =
        overrideProperty(
            "rule",
            FEATURE_VALIDATION_RULE_TYPE,
            CodeBlock.of("%M(%S, this)", COMMAND_RULE, context.lowerCaseName),
        )

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(typeNameOf<CommandValidationRule>())
        .addSuperinterface(COMMAND_VALIDATION_RULE_TYPE)
        .addProperty(commandRuleProperty)
        .addProperties(properties)
        .addFunction(generateValidateFunction(typeNameOf<Command>(), properties))
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
            .addAnnotation(PUBLISHED_API_ANNOTATION)
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
