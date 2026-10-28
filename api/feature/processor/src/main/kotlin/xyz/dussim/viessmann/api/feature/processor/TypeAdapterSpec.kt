package xyz.dussim.viessmann.api.feature.processor

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * How a generated single-pass factory reads a property. Each strategy accepts exactly what the matching validation
 * rule accepts, so the factory returns `null` exactly when validation reports errors.
 */
internal enum class PropertyAccessorStrategy {
    /** `typedPropertyRule`: the value must be the expected type. */
    STANDARD,

    /** `typedListPropertyRule`: the expected list type, or `ListEmptyValue` promoted to its `EMPTY` instance. */
    LIST_WITH_EMPTY_PROMOTION,

    /** `typedNullablePropertyRule`: the nullable value type, or its non-null type wrapped by [TypeAdapterSpec.valueConverter]. */
    NULLABLE_VALUE_WRAPPER,

    /** A [STANDARD] underlying value wrapped into the feature's enum type. */
    ENUM,
}

/** Processor-local source of truth for supported runtime-type generation behavior. */
internal data class TypeAdapterSpec(
    val runtimeType: TypeName,
    val validationRule: MemberName,
    val propertyAccessorStrategy: PropertyAccessorStrategy? = null,
    val valueConverter: MemberName? = null,
    val constraintConverter: MemberName? = null,
    val abbreviation: String? = null,
)

internal inline fun <reified T> standardPropertyAdapter(ruleName: String): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.STANDARD,
    )

internal inline fun <reified T> listPropertyAdapter(ruleName: String): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.LIST_WITH_EMPTY_PROMOTION,
    )

internal inline fun <reified T> nullableValuePropertyAdapter(
    ruleName: String,
    converterName: String,
): TypeAdapterSpec =
    TypeAdapterSpec(
        runtimeType = typeNameOf<T>(),
        validationRule = validationRule(ruleName),
        propertyAccessorStrategy = PropertyAccessorStrategy.NULLABLE_VALUE_WRAPPER,
        valueConverter = MemberName("xyz.dussim.viessmann.feature.api", converterName),
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
        )
    } else {
        PROPERTY_TYPE_ADAPTERS.getValue(type.copy(nullable = false))
    }
