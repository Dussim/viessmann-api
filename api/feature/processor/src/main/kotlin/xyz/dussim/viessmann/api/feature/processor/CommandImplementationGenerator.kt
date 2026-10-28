@file:OptIn(ViessmannApiInternalExceptionUsage::class)

package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.ArrayBooleanConstraints
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.ArrayObjectConstraints
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.ArrayUnknownConstraints
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.EnergyMatrixConstraints
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectConstraints
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.UnknownConstraints
import xyz.dussim.viessmann.feature.api.ViessmannApiInternalExceptionUsage
import xyz.dussim.viessmann.feature.api.validation.CommandValidationRule
import xyz.dussim.viessmann.feature.api.validation.propertyHash

private val NUMBER_OF_PARAMETERS_RULE = validationRule("numberOfParametersRule")
private val ONE_PARAMETER_COMMAND_RULE = validationRule("oneParameterCommandRule")
private val ONE_PARAMETER_COMMAND_FAIL_FAST_RULE = validationRule("oneParameterCommandFailFastRule")
private val COMMAND_FAIL_FAST_RULE = validationRule("commandFailFastRule")

internal val CONSTRAINT_TYPE_ADAPTERS: Map<TypeName, TypeAdapterSpec> =
    listOf(
        constraintAdapter<StringConstraints>("stringConstraintsRule", "S"),
        constraintAdapter<NumberConstraints>("numberConstraintsRule", "N"),
        constraintAdapter<BooleanConstraints>("booleanConstraintsRule", "B"),
        constraintAdapter<ArrayNumberConstraints>(
            "arrayNumberConstraintsRule",
            "AN",
            "toArrayNumberConstraintsOrNull",
        ),
        constraintAdapter<ArrayStringConstraints>(
            "arrayStringConstraintsRule",
            "AS",
            "toArrayStringConstraintsOrNull",
        ),
        constraintAdapter<ArrayBooleanConstraints>(
            "arrayBooleanConstraintsRule",
            "AB",
            "toArrayBooleanConstraintsOrNull",
        ),
        constraintAdapter<ArrayObjectConstraints>(
            "arrayObjectConstraintsRule",
            "AO",
            "toArrayObjectConstraintsOrNull",
        ),
        constraintAdapter<ArrayUnknownConstraints>(
            "arrayUnknownConstraintsRule",
            "AU",
            "toArrayUnknownConstraintsOrNull",
        ),
        constraintAdapter<ObjectConstraints>("objectConstraintsRule", "O"),
        constraintAdapter<ScheduleConstraints>("scheduleConstraintsRule", "SC"),
        constraintAdapter<EnergyMatrixConstraints>("energyMatrixConstraintsRule", "EM"),
        constraintAdapter<UnknownConstraints>("unknownConstraintsRule", "U"),
    ).associateBy(TypeAdapterSpec::runtimeType)

internal val CONSTRAINTS_VALIDATION_FUNCTIONS = CONSTRAINT_TYPE_ADAPTERS.mapValues { it.value.validationRule }

/**
 * Generates the private constructor. It receives the command plus every constraint already checked by
 * [createOrNullFunction], so it does no checking of its own.
 */
fun constructor(context: CommandSymbolContext) =
    FunSpec
        .constructorBuilder()
        .addModifiers(KModifier.PRIVATE)
        .addParameter(COMMAND, typeNameOf<Command>())
        .apply { context.constraintsProperties.forEach { addParameter(it.name, it.type) } }
        .build()

/**
 * Generates command property that overrides the base command.
 */
fun constructorProperty(context: CommandSymbolContext) = overrideProperty(COMMAND, typeNameOf<Command>(), CodeBlock.of("%N", COMMAND))

/**
 * Reads one parameter's constraints into a local of the same name, returning `null` from `createOrNull` when the
 * parameter rule would fail: the parameter is missing or its constraints have a type the rule rejects. Array
 * converters also accept `ArrayEmptyConstraints`, like `typedArrayCommandRule`.
 */
internal fun CodeBlock.Builder.addConstraintExtraction(property: ConstraintProperty) {
    val lookup = CodeBlock.of("%N.params[%S, %L]", COMMAND, property.name, propertyHash(property.name.hashCode(), property.name.length))
    val arrayConverter = CONSTRAINT_TYPE_ADAPTERS.getValue(property.type).constraintConverter
    if (arrayConverter != null) {
        add("val %N = %L?.constraints?.%M() ?: return null\n", property.name, lookup, arrayConverter)
    } else {
        add("val %N = %L?.constraints as? %T ?: return null\n", property.name, lookup, property.type)
    }
}

/**
 * Generates `createOrNull(command)`, which returns `null` exactly when the command's validation rules would report an
 * error: a parameter is missing or mistyped, or the parameter count differs from the declared one.
 */
internal fun createOrNullFunction(
    context: CommandSymbolContext,
    implName: ClassName,
): FunSpec =
    FunSpec
        .builder(CREATE_OR_NULL)
        .addParameter(COMMAND, typeNameOf<Command>())
        .returns(implName.copy(nullable = true))
        .addCode(
            buildCodeBlock {
                add("if (%N.params.size != %L) return null\n", COMMAND, context.constraintsProperties.size)
                context.constraintsProperties.forEach { addConstraintExtraction(it) }
                val arguments = (listOf(COMMAND) + context.constraintsProperties.map { it.name }).map { CodeBlock.of("%N", it) }.joinToCode(", ")
                add("return %T(%L)\n", implName, arguments)
            },
        ).build()

private fun constraintPropertiesImpl(context: CommandSymbolContext): List<PropertySpec> =
    context.constraintsProperties.map { property -> overrideProperty(property.name, property.type, CodeBlock.of("%N", property.name)) }

/**
 * Generates rule expressions for the command.
 */
private fun validationPlan(context: CommandSymbolContext): ValidationPlan {
    val constraintRules =
        context.constraintsProperties.map {
            context.parentContext.ruleRegistry.register(
                CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type),
                listOf(it.name),
                COMMAND_VALIDATION_RULE_TYPE,
            )
        }

    if (constraintRules.size == 1) {
        return ValidationPlan(
            listOf(
                oneParameterValidationRulePlan(
                    ruleRegistry = context.parentContext.ruleRegistry,
                    commandName = context.apiName,
                    parameterRule = constraintRules.single(),
                ),
            ),
        )
    }

    return ValidationPlan(
        constraintRules
            .map { ValidationRulePlan(normalExpression = ruleResult(it)) }
            .plus(
                ValidationRulePlan(
                    normalExpression =
                        ruleResult(
                            context.parentContext.ruleRegistry.register(
                                NUMBER_OF_PARAMETERS_RULE,
                                listOf(context.constraintsProperties.size, context.apiName),
                                COMMAND_VALIDATION_RULE_TYPE,
                            ),
                        ),
                ),
            ),
    )
}

internal fun oneParameterValidationRulePlan(
    ruleRegistry: RuleRegistry,
    commandName: String,
    parameterRule: MemberName,
): ValidationRulePlan =
    ValidationRulePlan(
        normalExpression =
            ruleResult(
                ruleRegistry.register(
                    ONE_PARAMETER_COMMAND_RULE,
                    listOf(commandName, parameterRule),
                    COMMAND_VALIDATION_RULE_TYPE,
                ),
            ),
        failFastExpression =
            ruleResult(
                ruleRegistry.register(
                    ONE_PARAMETER_COMMAND_FAIL_FAST_RULE,
                    listOf(commandName, parameterRule),
                    COMMAND_VALIDATION_RULE_TYPE,
                ),
            ),
    )

/**
 * Generates companion object with validation rules for command constraints.
 */
internal fun companionObject(
    context: CommandSymbolContext,
    validationPlan: ValidationPlan,
    implName: ClassName,
): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(typeNameOf<CommandValidationRule>())
        .addSuperinterface(COMMAND_VALIDATION_RULE_TYPE)
        .addProperty(commandRuleProperty(context))
        .addProperty(commandFailFastRuleProperty(context))
        .addFunction(createOrNullFunction(context, implName))
        .addFunction(
            generateValidateFunction(
                typeNameOf<Command>(),
                validationPlan.normalExpressions,
                useSingleErrorResultAggregation = true,
            ),
        ).addFunction(
            generateValidateFunction(
                functionName = "validateFailFast",
                targetType = typeNameOf<Command>(),
                ruleExpressions = validationPlan.failFastExpressions,
                isFailFast = validationPlan.requiresSeparateFailFast,
            ),
        ).build()

private fun commandRuleProperty(context: CommandSymbolContext): PropertySpec =
    overrideProperty(
        "rule",
        FEATURE_VALIDATION_RULE_TYPE,
        CodeBlock.of("%M(%S, this)", COMMAND_RULE, context.apiName),
    )

private fun commandFailFastRuleProperty(context: CommandSymbolContext): PropertySpec =
    overrideProperty(
        "failFastRule",
        FEATURE_VALIDATION_RULE_TYPE,
        CodeBlock.of("%M(%S, this)", COMMAND_FAIL_FAST_RULE, context.apiName),
    )

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
    val validationPlan = validationPlan(context)
    val typeSpec =
        TypeSpec
            .classBuilder(implName)
            .addAnnotation(PUBLISHED_API_ANNOTATION)
            .addAnnotation(VIESSMANN_API_INTERNAL_EXCEPTION_USAGE_OPT_IN)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterfaces(superInterfaces)
            .primaryConstructor(constructor(context))
            .addProperty(constructorProperty(context))
            .addProperties(context.inheritedConstraintsProperties)
            .addProperties(constraintPropertiesImpl(context))
            .addType(companionObject(context, validationPlan, implName))
            .build()

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(typeSpec)
        .build()
}
