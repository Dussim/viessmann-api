package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.ViessmannApiInternalExceptionUsage
import xyz.dussim.viessmann.feature.api.validation.SingleErrorValidationResultApi
import xyz.dussim.viessmann.feature.api.validation.Valid
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

const val VALIDATION_PACKAGE = "xyz.dussim.viessmann.feature.api.validation"
private const val FEATURE_API_PACKAGE = "xyz.dussim.viessmann.feature.api"
const val DELEGATE = "delegate"
const val COMMAND = "command"

// Naming constants
private const val IMPL_SUFFIX = "Impl"
private const val DESCRIPTOR_SUFFIX = "Descriptor"

val FEATURE_DESCRIPTOR_CLASS = ClassName(FEATURE_API_PACKAGE, "FeatureDescriptor")
val BASE_FEATURE = ClassName(FEATURE_API_PACKAGE, "BaseFeature")

// Common member names
val COMMAND_RULE = MemberName(VALIDATION_PACKAGE, "commandRule")
val EQUALS_IMPL = MemberName(FEATURE_API_PACKAGE, "equalsImpl")
val VALIDATION_RESULT_OF = ValidationResult.Companion::class.member("of")
val VALIDATION_RESULT_OF_SINGLE_ERROR_RESULTS = ValidationResult.Companion::class.member("ofSingleErrorResults")
val STATIC_FEATURE_DESCRIPTOR_FACTORY = MemberName(FEATURE_API_PACKAGE, "staticFeatureDescriptor")
val INDEXED_FEATURE_DESCRIPTOR_FACTORY = MemberName(FEATURE_API_PACKAGE, "indexedFeatureDescriptor")

val FEATURE_VALIDATION_RULE_TYPE = validationRuleType(typeNameOf<Feature>())
val COMMAND_VALIDATION_RULE_TYPE = validationRuleType(typeNameOf<Command>())

/**
 * Creates a validation rule type for a given target type.
 */
fun validationRuleType(targetType: TypeName): TypeName =
    ValidationRule::class
        .asClassName()
        .parameterizedBy(
            targetType,
            typeNameOf<ValidationError>(),
        )

/**
 * Creates a feature factory type for a given interface type.
 */
fun featureFactoryType(interfaceType: TypeName): TypeName =
    FeatureFactory::class
        .asClassName()
        .parameterizedBy(interfaceType)

/**
 * Creates a feature descriptor type for a given interface type.
 * Handles indexed features.
 */
fun featureDescriptorType(
    interfaceType: TypeName,
    isIndexed: Boolean,
): TypeName {
    val base =
        if (isIndexed) {
            FEATURE_DESCRIPTOR_CLASS.nestedClass("Indexed")
        } else {
            FEATURE_DESCRIPTOR_CLASS.nestedClass("Static")
        }
    return base.parameterizedBy(interfaceType)
}

/**
 * Creates the PublishedApi annotation spec.
 */
val PUBLISHED_API_ANNOTATION: AnnotationSpec =
    AnnotationSpec
        .builder(PublishedApi::class)
        .build()

val VIESSMANN_API_INTERNAL_EXCEPTION_USAGE_OPT_IN: AnnotationSpec =
    AnnotationSpec
        .builder(ClassName("kotlin", "OptIn"))
        .addMember("%T::class", ViessmannApiInternalExceptionUsage::class.asTypeName())
        .build()

val SINGLE_ERROR_VALIDATION_RESULT_API_OPT_IN: AnnotationSpec =
    AnnotationSpec
        .builder(ClassName("kotlin", "OptIn"))
        .addMember("%T::class", SingleErrorValidationResultApi::class.asTypeName())
        .build()

val FILE_DEPRECATION_SUPPRESSION: AnnotationSpec =
    AnnotationSpec
        .builder(Suppress::class)
        .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
        .addMember("%S", "DEPRECATION")
        .build()

val DEFAULT_CONSTRAINTS =
    setOf(
        "constraint1",
        "constraint2",
        "constraint3",
        "constraint4",
        "constraint5",
        "constraint6",
        "constraint7",
        "constraint8",
    )

/**
 * Combines two integers into a single long value for efficient map lookups.
 * Used for property/command name hashing.
 */
fun propertyHash(
    high: Int,
    low: Int,
): Long = (high.toLong() shl 32) or (low.toLong() and 0xFFFFFFFFL)

/**
 * Creates a validation rule member name.
 */
fun validationRule(name: String) = MemberName(VALIDATION_PACKAGE, name)

/**
 * Creates a validation rule from a predicate.
 */
fun <T, E> booleanRule(
    errorProvider: (T) -> E,
    predicate: (T) -> Boolean,
): ValidationRule<T, E> =
    ValidationRule { value ->
        if (predicate(value)) Valid() else ValidationResult.of(errorProvider(value))
    }

/**
 * Creates a variable-argument function call CodeBlock.
 * Useful for generating validation result aggregation calls.
 * Automatically chunks into nested calls when args exceed the max overload size (16).
 */
fun varArgFunctionCall(
    function: MemberName,
    args: List<CodeBlock>,
): CodeBlock {
    val maxArgs = 30
    if (args.size <= maxArgs) {
        return CodeBlock
            .builder()
            .add("%M(\n", function)
            .indent()
            .apply { args.forEach(::add) }
            .unindent()
            .add(")\n")
            .build()
    }
    // Chunk into nested calls: of(of(chunk1...), of(chunk2...), ...)
    val chunks = args.chunked(maxArgs)
    val nestedCalls =
        chunks.map { chunk ->
            CodeBlock.of(
                "%L,\n",
                CodeBlock
                    .builder()
                    .add("%M(\n", function)
                    .indent()
                    .apply { chunk.forEach(::add) }
                    .unindent()
                    .add(")")
                    .build(),
            )
        }
    return varArgFunctionCall(function, nestedCalls)
}

/**
 * Generates an override validate function that aggregates results from multiple rules.
 */
fun generateValidateFunction(
    targetType: TypeName,
    ruleExpressions: List<CodeBlock>,
    isFailFast: Boolean = false,
    useSingleErrorResultAggregation: Boolean = false,
): FunSpec {
    val useSingleErrorResultAggregationForFunction =
        useSingleErrorResultAggregation && !isFailFast && ruleExpressions.size in 2..3

    return FunSpec
        .builder("validate")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter(ParameterSpec.builder("value", targetType).build())
        .returns(
            ValidationResult::class
                .asTypeName()
                .parameterizedBy(typeNameOf<ValidationError>()),
        ).apply {
            if (useSingleErrorResultAggregationForFunction) {
                addAnnotation(SINGLE_ERROR_VALIDATION_RESULT_API_OPT_IN)
            }
        }.addCode(
            CodeBlock
                .builder()
                .apply {
                    if (ruleExpressions.isEmpty()) {
                        add("return %M()\n", MemberName(VALIDATION_PACKAGE, "Valid"))
                    } else if (ruleExpressions.size == 1) {
                        add("return %L.validate(value)\n", ruleExpressions.single())
                    } else if (isFailFast) {
                        ruleExpressions.dropLast(1).forEachIndexed { index, expr ->
                            val resultVar = "result$index"
                            add("val %L = %L.validate(value)\n", resultVar, expr)
                            add("if (%L.isInvalid) return %L\n", resultVar, resultVar)
                        }
                        add("return %L.validate(value)\n", ruleExpressions.last())
                    } else {
                        add("return ")
                        add(
                            varArgFunctionCall(
                                if (useSingleErrorResultAggregationForFunction) {
                                    VALIDATION_RESULT_OF_SINGLE_ERROR_RESULTS
                                } else {
                                    VALIDATION_RESULT_OF
                                },
                                ruleExpressions.map { CodeBlock.of("%L.validate(value),\n", it) },
                            ),
                        )
                    }
                }.build(),
        ).build()
}

/**
 * Creates a CodeBlock that handles both indexed and non-indexed feature cases.
 */
fun indexedOrDirectCodeBlock(
    isIndexed: Boolean,
    block: (isIndexed: Boolean) -> CodeBlock,
): CodeBlock =
    if (isIndexed) {
        CodeBlock
            .builder()
            .add("{ index ->\n")
            .indent()
            .add(block(true))
            .unindent()
            .add("\n}")
            .build()
    } else {
        block(false)
    }

/**
 * Creates an inline extension property with a getter.
 */
fun extensionProperty(
    name: String,
    receiver: TypeName,
    type: TypeName,
    statement: String,
    vararg args: Any,
): PropertySpec =
    PropertySpec
        .builder(name, type)
        .receiver(receiver)
        .getter(
            FunSpec
                .getterBuilder()
                .addModifiers(KModifier.INLINE)
                .addStatement(statement, *args)
                .build(),
        ).build()

/**
 * Creates an override property.
 */
fun overrideProperty(
    name: String,
    type: TypeName,
    initializer: Any? = null,
): PropertySpec =
    PropertySpec
        .builder(name, type)
        .addModifiers(KModifier.OVERRIDE)
        .apply {
            when (initializer) {
                is String -> initializer(initializer)
                is CodeBlock -> initializer(initializer)
            }
        }.build()

/**
 * Generates a descriptor name from a class name.
 * Example: "MyFeatureImpl" -> "myFeatureDescriptor"
 */
fun generateDescriptorName(className: ClassName): String =
    className.simpleName
        .replace("_", "")
        .replaceFirstChar { it.lowercase() }
        .removeSuffix(IMPL_SUFFIX) + DESCRIPTOR_SUFFIX

data class CommandSignature(
    val name: String,
    val parameters: List<Pair<String, TypeName>>,
) {
    val implName: String
        get() = buildImplName(name, parameters)
}

/**
 * Generates a short, stable implementation class name for a command.
 *
 * **Normal format** (when the result fits within [MAX_IMPL_NAME_LENGTH] chars):
 * `{CapitalizedCommandName}{params}Impl`
 *
 * Each parameter contributes `{CapitalizedParamName}{typeAbbrev}`:
 *   - ParamName is the full camelCase parameter name with first letter uppercased.
 *   - typeAbbrev is the constraint type abbreviation:
 *       StringConstraints        → S
 *       NumberConstraints        → N
 *       BooleanConstraints       → B
 *       ArrayStringConstraints   → AS
 *       ArrayNumberConstraints   → AN
 *       ArrayBooleanConstraints  → AB
 *       ArrayObjectConstraints   → AO
 *       ArrayUnknownConstraints  → AU
 *       ArrayEmptyConstraints    → AE
 *       ScheduleConstraints      → SC
 *       EnergyMatrixConstraints  → EM
 *       ObjectConstraints        → O
 *       UnknownConstraints       → U
 *       (anything else)          → X
 *
 * **Hash fallback** (when the normal name would exceed [MAX_IMPL_NAME_LENGTH] = $MAX_IMPL_NAME_LENGTH chars):
 * `{CapitalizedCommandName}_{hash8hex}Impl`
 * where hash8hex is the first 8 hex characters of the SHA-256 of the full
 * (un-truncated) params part, ensuring uniqueness without length blow-up.
 *
 * Examples:
 *   - setTemperature(value: StringConstraints)                         → SetTemperatureValueSImpl
 *   - setCurve(shift: NumberConstraints, slope: NumberConstraints)     → SetCurveShiftNSlopeNImpl
 *   - setUnitSystemAndFormatters(unitSystem:S, dateFormat:S, ×8 …)    → SetUnitSystemAndFormatters_a3f9b2c1Impl
 */
private const val MAX_IMPL_NAME_LENGTH = 80

private fun buildImplName(
    name: String,
    parameters: List<Pair<String, TypeName>>,
): String {
    val capitalizedName = name.replaceFirstChar { it.uppercase() }
    if (parameters.isEmpty()) return "${capitalizedName}Impl"

    val typeAbbrevs =
        mapOf(
            "StringConstraints" to "S",
            "NumberConstraints" to "N",
            "BooleanConstraints" to "B",
            "ArrayStringConstraints" to "AS",
            "ArrayNumberConstraints" to "AN",
            "ArrayBooleanConstraints" to "AB",
            "ArrayObjectConstraints" to "AO",
            "ArrayUnknownConstraints" to "AU",
            "ArrayEmptyConstraints" to "AE",
            "ScheduleConstraints" to "SC",
            "EnergyMatrixConstraints" to "EM",
            "ObjectConstraints" to "O",
            "UnknownConstraints" to "U",
        )

    val paramsPart =
        parameters.joinToString("") { (pName, pType) ->
            val simpleName = pType.toString().substringAfterLast(".").removeSuffix("?")
            val typeAbbrev = typeAbbrevs[simpleName] ?: "X"
            pName.replaceFirstChar { it.uppercase() } + typeAbbrev
        }

    val fullName = "${capitalizedName}${paramsPart}Impl"
    if (fullName.length <= MAX_IMPL_NAME_LENGTH) return fullName

    // Hash fallback: SHA-256 of paramsPart, take first 8 hex chars
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hash =
        digest
            .digest(paramsPart.toByteArray())
            .take(4)
            .joinToString("") { "%02x".format(it) }
    return "${capitalizedName}_${hash}Impl"
}

data class RuleSignature(
    val function: MemberName,
    val args: List<Any>,
    val targetType: TypeName,
) {
    fun generateName(): String {
        val functionPart = function.simpleName
        val argsPart =
            args.joinToString("_") { arg ->
                when (arg) {
                    is ClassName -> arg.simpleName
                    is MemberName -> arg.simpleName
                    else -> arg.toString().replace(Regex("[^a-zA-Z0-9]"), "_")
                }
            }
        val name =
            if (argsPart.isBlank()) {
                functionPart
            } else {
                "${argsPart}_$functionPart"
            }
        val sanitized = if (name.first().isDigit()) "rule_$name" else name
        return sanitized.replaceFirstChar { it.lowercase() }.replace("__", "_")
    }
}

class RuleRegistry(
    val rulesPackage: String,
) {
    private val rules = mutableMapOf<RuleSignature, MemberName>()

    fun register(
        function: MemberName,
        args: List<Any>,
        targetType: TypeName,
    ): MemberName {
        val signature = RuleSignature(function, args, targetType)
        return rules.getOrPut(signature) {
            MemberName(rulesPackage, signature.generateName())
        }
    }

    fun getAllRules(): Map<RuleSignature, MemberName> = rules
}

enum class BaseFeature(
    val delegate: TypeName,
    val abstractClass: ClassName,
) {
    Feature(typeNameOf<Feature>(), BASE_FEATURE),
}

data class FeatureSignature(
    val baseFeature: BaseFeature,
    val properties: List<Pair<String, TypeName>>,
    val commands: List<Triple<String, CommandSignature, Boolean>>,
) {
    val implName: String
        get() {
            val basePart = baseFeature.name
            val propsPart =
                properties.joinToString("") { (name, type) ->
                    name.replaceFirstChar { it.uppercase() } +
                        type
                            .toString()
                            .substringAfterLast(".")
                            .removeSuffix("?")
                            .replaceFirstChar { it.uppercase() }
                }
            val cmdsPart =
                commands.joinToString("") { (name, sig, isNullable) ->
                    name.replaceFirstChar { it.uppercase() } +
                        sig.implName.removeSuffix("Impl") +
                        if (isNullable) "Opt" else ""
                }
            val rawName = "Feat${basePart}${propsPart}$cmdsPart"
            val hash =
                this
                    .hashCode()
                    .toUInt()
                    .toString(36)
                    .uppercase()
            return "${rawName.take(40)}${hash}Impl"
        }
}
