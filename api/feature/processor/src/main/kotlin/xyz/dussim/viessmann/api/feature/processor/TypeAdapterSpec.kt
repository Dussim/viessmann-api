package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.typeNameOf

internal enum class PropertyAccessorStrategy {
    STANDARD,
    LIST_WITH_EMPTY_PROMOTION,
    NULLABLE_VALUE_WRAPPER,
    ENUM,
}

/** Processor-local source of truth for supported runtime-type generation behavior. */
internal data class TypeAdapterSpec(
    val runtimeType: TypeName,
    val validationRule: MemberName,
    val propertyAccessorStrategy: PropertyAccessorStrategy? = null,
    val requiredAccessor: MemberName? = null,
    val optionalAccessor: MemberName? = null,
    val constraintConverter: MemberName? = null,
    val abbreviation: String? = null,
)

internal val REQUIRE_PROPERTY_VALUE = MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValue")
internal val REQUIRE_PROPERTY_VALUE_OR_NULL_IF_MISSING =
    MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValueOrNullIfMissing")

private val FIND_PROPERTY_VALUE_OR_NULL = MemberName("xyz.dussim.viessmann.feature.api", "findPropertyValueOrNull")
private val REQUIRE_PROPERTY_VALUE_OR_PROMOTE_EMPTY =
    MemberName("xyz.dussim.viessmann.feature.api", "requirePropertyValueOrPromoteEmpty")
private val FIND_PROPERTY_VALUE_OR_PROMOTE_EMPTY =
    MemberName("xyz.dussim.viessmann.feature.api", "findPropertyValueOrPromoteEmpty")

internal inline fun <reified T> standardPropertyAdapter(ruleName: String): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.STANDARD,
        requiredAccessor = REQUIRE_PROPERTY_VALUE,
        optionalAccessor = FIND_PROPERTY_VALUE_OR_NULL,
    )

internal inline fun <reified T> listPropertyAdapter(ruleName: String): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.LIST_WITH_EMPTY_PROMOTION,
        requiredAccessor = REQUIRE_PROPERTY_VALUE_OR_PROMOTE_EMPTY,
        optionalAccessor = FIND_PROPERTY_VALUE_OR_PROMOTE_EMPTY,
    )

internal inline fun <reified T> nullableValuePropertyAdapter(
    ruleName: String,
    requiredAccessorName: String,
    optionalAccessorName: String,
): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.NULLABLE_VALUE_WRAPPER,
        requiredAccessor = MemberName("xyz.dussim.viessmann.feature.api", requiredAccessorName),
        optionalAccessor = MemberName("xyz.dussim.viessmann.feature.api", optionalAccessorName),
    )

internal inline fun <reified T> constraintAdapter(
    ruleName: String,
    abbreviation: String,
    converterName: String? = null,
): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        constraintConverter = converterName?.let { MemberName("xyz.dussim.viessmann.feature.api", it) },
        abbreviation = abbreviation,
    )

internal fun ParameterProperty.typeAdapter(): TypeAdapterSpec =
    if (isEnumProperty) {
        TypeAdapterSpec(
            runtimeType = type.copy(nullable = false),
            validationRule = validationFunction,
            propertyAccessorStrategy = PropertyAccessorStrategy.ENUM,
            requiredAccessor = REQUIRE_PROPERTY_VALUE,
            optionalAccessor = REQUIRE_PROPERTY_VALUE_OR_NULL_IF_MISSING,
        )
    } else {
        PROPERTY_TYPE_ADAPTERS.getValue(type.copy(nullable = false))
    }
