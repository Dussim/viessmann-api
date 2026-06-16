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
private val REQUIRE_PARAM = MemberName("xyz.dussim.viessmann.feature.api", "requireParam")
private val REQUIRE_CONSTRAINTS = MemberName("xyz.dussim.viessmann.feature.api", "requireConstraints")

private val ARRAY_CONSTRAINT_CAST_FUNCTIONS: Map<TypeName, MemberName> =
    mapOf(
        typeNameOf<ArrayNumberConstraints>() to MemberName("xyz.dussim.viessmann.feature.api", "toArrayNumberConstraintsOrThrow"),
        typeNameOf<ArrayStringConstraints>() to MemberName("xyz.dussim.viessmann.feature.api", "toArrayStringConstraintsOrThrow"),
        typeNameOf<ArrayBooleanConstraints>() to MemberName("xyz.dussim.viessmann.feature.api", "toArrayBooleanConstraintsOrThrow"),
        typeNameOf<ArrayObjectConstraints>() to MemberName("xyz.dussim.viessmann.feature.api", "toArrayObjectConstraintsOrThrow"),
        typeNameOf<ArrayUnknownConstraints>() to MemberName("xyz.dussim.viessmann.feature.api", "toArrayUnknownConstraintsOrThrow"),
    )

private val CONSTRAINTS_VALIDATION_FUNCTIONS =
    mapOf(
        typeNameOf<StringConstraints>() to validationRule("stringConstraintsRule"),
        typeNameOf<NumberConstraints>() to validationRule("numberConstraintsRule"),
        typeNameOf<BooleanConstraints>() to validationRule("booleanConstraintsRule"),
        typeNameOf<ArrayNumberConstraints>() to validationRule("arrayNumberConstraintsRule"),
        typeNameOf<ArrayStringConstraints>() to validationRule("arrayStringConstraintsRule"),
        typeNameOf<ArrayBooleanConstraints>() to validationRule("arrayBooleanConstraintsRule"),
        typeNameOf<ArrayObjectConstraints>() to validationRule("arrayObjectConstraintsRule"),
        typeNameOf<ArrayUnknownConstraints>() to validationRule("arrayUnknownConstraintsRule"),
        typeNameOf<ObjectConstraints>() to validationRule("objectConstraintsRule"),
        typeNameOf<ScheduleConstraints>() to validationRule("scheduleConstraintsRule"),
        typeNameOf<EnergyMatrixConstraints>() to validationRule("energyMatrixConstraintsRule"),
        typeNameOf<UnknownConstraints>() to validationRule("unknownConstraintsRule"),
    )

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
    val arrayTypeCastFunction = ARRAY_CONSTRAINT_CAST_FUNCTIONS[property.type]

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
private fun ruleExpressions(context: CommandSymbolContext): List<CodeBlock> =
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

/**
 * Generates companion object with validation rules for command constraints.
 */
fun companionObject(context: CommandSymbolContext): TypeSpec {
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
        .addFunction(
            generateValidateFunction(
                typeNameOf<Command>(),
                ruleExpressions(context),
                useSingleErrorResultAggregation = true,
            ),
        ).build()
}

/**
 * Generates fail-fast validation object.
 */
fun failFastObject(
    context: CommandSymbolContext,
    targetType: TypeName,
): TypeSpec =
    TypeSpec
        .objectBuilder("FailFast")
        .addSuperinterface(validationRuleType(targetType))
        .addFunction(generateValidateFunction(targetType, ruleExpressions(context), isFailFast = true))
        .build()

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
            .addType(companionObject(context))
            .addType(failFastObject(context, typeNameOf<Command>()))
            .build()

    return FileSpec
        .builder(implName.packageName, implName.simpleName)
        .addType(typeSpec)
        .build()
}
