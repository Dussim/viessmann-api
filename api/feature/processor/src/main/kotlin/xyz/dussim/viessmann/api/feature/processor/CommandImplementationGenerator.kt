@file:OptIn(ViessmannApiInternalExceptionUsage::class)

package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
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

private val NUMBER_OF_PARAMETERS_RULE = validationRule("numberOfParametersRule")
private val COMMAND_FAIL_FAST_RULE = validationRule("commandFailFastRule")
private val REQUIRE_PARAM = MemberName("xyz.dussim.viessmann.feature.api", "requireParam")
private val REQUIRE_CONSTRAINTS = MemberName("xyz.dussim.viessmann.feature.api", "requireConstraints")

internal val CONSTRAINT_TYPE_ADAPTERS: Map<TypeName, TypeAdapterSpec> =
    listOf(
        constraintAdapter<StringConstraints>("stringConstraintsRule", "S"),
        constraintAdapter<NumberConstraints>("numberConstraintsRule", "N"),
        constraintAdapter<BooleanConstraints>("booleanConstraintsRule", "B"),
        constraintAdapter<ArrayNumberConstraints>(
            "arrayNumberConstraintsRule",
            "AN",
            "toArrayNumberConstraintsOrThrow",
        ),
        constraintAdapter<ArrayStringConstraints>(
            "arrayStringConstraintsRule",
            "AS",
            "toArrayStringConstraintsOrThrow",
        ),
        constraintAdapter<ArrayBooleanConstraints>(
            "arrayBooleanConstraintsRule",
            "AB",
            "toArrayBooleanConstraintsOrThrow",
        ),
        constraintAdapter<ArrayObjectConstraints>(
            "arrayObjectConstraintsRule",
            "AO",
            "toArrayObjectConstraintsOrThrow",
        ),
        constraintAdapter<ArrayUnknownConstraints>(
            "arrayUnknownConstraintsRule",
            "AU",
            "toArrayUnknownConstraintsOrThrow",
        ),
        constraintAdapter<ObjectConstraints>("objectConstraintsRule", "O"),
        constraintAdapter<ScheduleConstraints>("scheduleConstraintsRule", "SC"),
        constraintAdapter<EnergyMatrixConstraints>("energyMatrixConstraintsRule", "EM"),
        constraintAdapter<UnknownConstraints>("unknownConstraintsRule", "U"),
    ).associateBy(TypeAdapterSpec::runtimeType)

internal val CONSTRAINTS_VALIDATION_FUNCTIONS = CONSTRAINT_TYPE_ADAPTERS.mapValues { it.value.validationRule }

/**
 * Generates constructor for command implementation.
 */
fun constructor(context: CommandSymbolContext) =
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
fun constructorProperty(context: CommandSymbolContext) = overrideProperty(COMMAND, typeNameOf<Command>(), COMMAND)

/**
 * Generates property initializer that extracts command constraints.
 * Throws GeneratedAccessException if constraints are invalid.
 */
internal fun constraintPropertyInitializer(property: ConstraintProperty): CodeBlock {
    val combined = propertyHash(property.name.hashCode(), property.name.length)
    val arrayTypeCastFunction = CONSTRAINT_TYPE_ADAPTERS.getValue(property.type).constraintConverter

    return if (arrayTypeCastFunction != null) {
        CodeBlock.of(
            "command.params.%M(%S, %L).constraints.%M()",
            REQUIRE_PARAM,
            property.name,
            combined,
            arrayTypeCastFunction,
        )
    } else {
        CodeBlock.of(
            "command.params.%M(%S, %L).constraints.%M<%T>()",
            REQUIRE_PARAM,
            property.name,
            combined,
            REQUIRE_CONSTRAINTS,
            property.type,
        )
    }
}

private fun constraintPropertiesImpl(context: CommandSymbolContext): List<PropertySpec> =
    context.constraintsProperties.map { property ->
        PropertySpec
            .builder(property.name, property.type)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(constraintPropertyInitializer(property))
            .build()
    }

/**
 * Generates rule expressions for the command.
 */
private fun validationPlan(context: CommandSymbolContext): ValidationPlan =
    ValidationPlan(
        context
            .constraintsProperties
            .map {
                ValidationRulePlan(
                    normalExpression =
                        CodeBlock.of(
                            "%M",
                            context.parentContext.ruleRegistry.register(
                                CONSTRAINTS_VALIDATION_FUNCTIONS.getValue(it.type),
                                listOf(it.name),
                                COMMAND_VALIDATION_RULE_TYPE,
                            ),
                        ),
                )
            }.plus(
                ValidationRulePlan(
                    normalExpression =
                        CodeBlock.of(
                            "%M",
                            context.parentContext.ruleRegistry.register(
                                NUMBER_OF_PARAMETERS_RULE,
                                listOf(context.constraintsProperties.size, context.apiName),
                                COMMAND_VALIDATION_RULE_TYPE,
                            ),
                        ),
                ),
            ),
    )

/**
 * Generates companion object with validation rules for command constraints.
 */
internal fun companionObject(
    context: CommandSymbolContext,
    validationPlan: ValidationPlan,
): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(typeNameOf<CommandValidationRule>())
        .addSuperinterface(COMMAND_VALIDATION_RULE_TYPE)
        .addProperty(commandRuleProperty(context))
        .addProperty(commandFailFastRuleProperty(context))
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
            .addType(companionObject(context, validationPlan))
            .build()

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(typeSpec)
        .build()
}
