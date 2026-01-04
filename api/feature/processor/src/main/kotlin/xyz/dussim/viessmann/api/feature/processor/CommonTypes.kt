package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.typeNameOf
import xyz.dussim.viessmann.feature.api.FeatureMatchers
import xyz.dussim.viessmann.feature.api.FeatureUtils
import xyz.dussim.viessmann.feature.api.IndexedFeatureMatchersFactory
import xyz.dussim.viessmann.feature.api.IndexedFeatureUtilsFactory
import xyz.dussim.viessmann.feature.api.validation.ValidationResult

const val VALIDATION_PACKAGE = "xyz.dussim.viessmann.feature.api.validation"
private const val FEATURE_API_PACKAGE = "xyz.dussim.viessmann.feature.api"
const val DELEGATE = "delegate"
const val COMMAND = "command"

// Naming constants
private const val IMPL_SUFFIX = "Impl"
private const val FACTORY_SUFFIX = "Factory"
private const val MATCHERS_SUFFIX = "Matchers"
private const val UTILS_SUFFIX = "Utils"
private const val RULE_SUFFIX = "Rule"
private const val PROPERTY_RULE_SUFFIX = "PropertyRule"
private const val CONSTRAINT_RULE_SUFFIX = "ConstraintRule"

// Common class names
val FEATURE_MATCHERS_CLASS = ClassName(FEATURE_API_PACKAGE, "FeatureMatchers")
val FEATURE_UTILS_CLASS = ClassName(FEATURE_API_PACKAGE, "FeatureUtils")

// Common member names
val validateAll = MemberName(VALIDATION_PACKAGE, "validateAll")
val commandRule = MemberName(VALIDATION_PACKAGE, "commandRule")
val validationResultOf = ValidationResult.Companion::class.member("of")

/**
 * Creates the PublishedApi annotation spec.
 */
val publishedApiAnnotation: AnnotationSpec =
    AnnotationSpec
        .builder(PublishedApi::class)
        .build()

/**
 * Creates a type that is either a lambda (Int) -> T for indexed features,
 * or just T for non-indexed features.
 *
 * @param isIndexed Whether the feature is indexed
 * @param returnType The return type (T)
 * @return LambdaTypeName if indexed, otherwise the returnType directly
 */
fun indexedOrDirectType(
    isIndexed: Boolean,
    returnType: TypeName,
): TypeName =
    if (isIndexed) {
        when (returnType) {
            typeNameOf<FeatureUtils>() -> typeNameOf<IndexedFeatureUtilsFactory>()
            typeNameOf<FeatureMatchers>() -> typeNameOf<IndexedFeatureMatchersFactory>()
            else -> error("Unsupported return type for indexed feature: $returnType")
        }
    } else {
        returnType
    }

/**
 * Generates a factory name from a class name.
 * Example: "MyFeatureImpl" -> "myFeatureFactory"
 */
fun generateFactoryName(className: ClassName): String =
    className.simpleName
        .replaceFirstChar { it.lowercase() }
        .removeSuffix(IMPL_SUFFIX) + FACTORY_SUFFIX

/**
 * Generates a matchers name from a class name.
 * Example: "MyFeatureImpl" -> "myFeatureMatchers"
 */
fun generateMatchersName(className: ClassName): String =
    className.simpleName
        .replaceFirstChar { it.lowercase() }
        .removeSuffix(IMPL_SUFFIX) + MATCHERS_SUFFIX

/**
 * Generates a utils name from a class name.
 * Example: "MyFeatureImpl" -> "myFeatureUtils"
 */
fun generateUtilsName(className: ClassName): String =
    className.simpleName
        .replaceFirstChar { it.lowercase() }
        .removeSuffix(IMPL_SUFFIX) + UTILS_SUFFIX

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

/**
 * Generates a command factory name from a feature and command name.
 * Example: ("myFeature", "setTemperature") -> "myFeatureSetTemperatureFactory"
 */
fun generateCommandFactoryName(
    featureClassName: ClassName,
    commandName: String,
): String {
    val featurePrefix =
        featureClassName.simpleName
            .replaceFirstChar { it.lowercase() }
            .removeSuffix(IMPL_SUFFIX)
    return featurePrefix + commandName.replaceFirstChar { it.uppercase() } + FACTORY_SUFFIX
}
