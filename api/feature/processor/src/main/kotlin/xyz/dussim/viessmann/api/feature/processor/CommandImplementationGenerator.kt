package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
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
            context.name.replace("_", ""),
        ).endControlFlow()
        .build()

/**
 * Generates companion object with validation rules for command constraints.
 */
context(context: CommandSymbolContext)
fun companionObject(): TypeSpec {
    val ruleExpressions =
        context
            .constraintsProperties
            .map {
                CodeBlock.of(
                    "%M",
                    context.parentContext.ruleRegistry.register(
                        CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type),
                        listOf(it.name),
                        COMMAND_VALIDATION_RULE_TYPE,
                    ),
                )
            }.plus(
                CodeBlock.of(
                    "%M",
                    context.parentContext.ruleRegistry.register(
                        NUMBER_OF_PARAMETERS_RULE,
                        listOf(context.constraintsProperties.size, context.lowerCaseName.replace("_", "")),
                        COMMAND_VALIDATION_RULE_TYPE,
                    ),
                ),
            )

    val commandRuleProperty =
        overrideProperty(
            "rule",
            FEATURE_VALIDATION_RULE_TYPE,
            CodeBlock.of("%M(%S, this)", COMMAND_RULE, context.lowerCaseName.replace("_", "")),
        )

    return TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(typeNameOf<CommandValidationRule>())
        .addSuperinterface(COMMAND_VALIDATION_RULE_TYPE)
        .addProperty(commandRuleProperty)
        .addFunction(generateValidateFunction(typeNameOf<Command>(), ruleExpressions))
        .build()
}

/**
 * Generates complete command implementation class in a separate file.
 * Creates an internal class that implements multiple command interfaces with validation.
 *
 * @param implName The name of the implementation class
 * @param superInterfaces The list of command interfaces to implement
 * @param context The command context with all necessary information
 * @return FileSpec for the command implementation
 */
fun generateCommandImplementation(
    implName: ClassName,
    superInterfaces: List<TypeName>,
    context: CommandSymbolContext,
): FileSpec {
    val typeSpec =
        context(context) {
            TypeSpec
                .classBuilder(implName)
                .addAnnotation(PUBLISHED_API_ANNOTATION)
                .addModifiers(KModifier.INTERNAL)
                .addSuperinterfaces(superInterfaces)
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

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(typeSpec)
        .build()
}
