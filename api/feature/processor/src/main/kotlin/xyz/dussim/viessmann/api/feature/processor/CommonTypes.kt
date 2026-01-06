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
private const val PROPERTY_RULE_SUFFIX = "PropertyRule"
private const val CONSTRAINT_RULE_SUFFIX = "ConstraintRule"

val FEATURE_DESCRIPTOR_CLASS = ClassName(FEATURE_API_PACKAGE, "FeatureDescriptor")

// Common member names
val COMMAND_RULE = MemberName(VALIDATION_PACKAGE, "commandRule")
val EQUALS_IMPL = MemberName(FEATURE_API_PACKAGE, "equalsImpl")
val VALIDATION_RESULT_OF = ValidationResult.Companion::class.member("of")
val FEATURE_DESCRIPTOR_FACTORY = MemberName(FEATURE_API_PACKAGE, "FeatureDescriptor")

val DEVICE_FEATURE_RULE = MemberName(VALIDATION_PACKAGE, "deviceFeatureRule")
val GATEWAY_FEATURE_RULE = MemberName(VALIDATION_PACKAGE, "gatewayFeatureRule")
val GEOFENCING_FEATURE_RULE = MemberName(VALIDATION_PACKAGE, "geofencingFeatureRule")

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
 */
fun varArgFunctionCall(
    function: MemberName,
    args: List<CodeBlock>,
): CodeBlock =
    CodeBlock
        .builder()
        .add("%M(\n", function)
        .indent()
        .apply { args.forEach(::add) }
        .unindent()
        .add(")\n")
        .build()

/**
 * Creates a private validation rule property.
 */
fun ruleProperty(
    name: String,
    ruleType: TypeName,
    initializer: CodeBlock,
): PropertySpec =
    PropertySpec
        .builder(name, ruleType)
        .addModifiers(KModifier.PRIVATE)
        .initializer(initializer)
        .build()

/**
 * Generates an override validate function that aggregates results from multiple rules.
 */
fun generateValidateFunction(
    targetType: TypeName,
    ruleProperties: List<PropertySpec>,
): FunSpec =
    FunSpec
        .builder("validate")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter(ParameterSpec.builder("value", targetType).build())
        .returns(
            ValidationResult::class
                .asTypeName()
                .parameterizedBy(typeNameOf<ValidationError>()),
        ).addCode(
            CodeBlock
                .builder()
                .add("return ")
                .add(
                    varArgFunctionCall(
                        VALIDATION_RESULT_OF,
                        ruleProperties.map { CodeBlock.of("${it.name}.validate(value),\n") },
                    ),
                ).build(),
        ).build()

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
        .replaceFirstChar { it.lowercase() }
        .removeSuffix(IMPL_SUFFIX) + DESCRIPTOR_SUFFIX

/**
 * Generates a property rule name.
 * Example: "temperature" -> "temperaturePropertyRule"
 */
fun generatePropertyRuleName(propertyName: String): String = propertyName + PROPERTY_RULE_SUFFIX

/**
 * Generates a constraint rule name.
 * Example: "temperature" -> "temperatureConstraintRule"
 */
fun generateConstraintRuleName(propertyName: String): String = propertyName + CONSTRAINT_RULE_SUFFIX
