package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import kotlin.reflect.KClass

fun Indexed(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Indexed =
    object : FeatureMatchers.Indexed {
        override val structureValidator = validation
        override val failFastStructureValidator = failFast

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, failFast)

        override fun byName(index: Int): FeatureMatcher = FeatureMatcher.byName(wildcardName.replace("{N}", index.toString()))

        override fun byNameThenStructure(index: Int): FeatureMatcher = FeatureMatcher.byNameThenStructure(wildcardName.replace("{N}", index.toString()), failFast)
    }

fun Indexed(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Indexed =
    object : FeatureMatchers.Indexed {
        override val structureValidator = validation
        override val failFastStructureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)

        override fun byName(index: Int): FeatureMatcher = FeatureMatcher.byName(wildcardName.replace("{N}", index.toString()))

        override fun byNameThenStructure(index: Int): FeatureMatcher = FeatureMatcher.byNameThenStructure(wildcardName.replace("{N}", index.toString()), validation)
    }

fun Static(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Static =
    object : FeatureMatchers.Static {
        override val structureValidator = validation
        override val failFastStructureValidator = failFast

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, failFast)
    }

fun Static(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Static =
    object : FeatureMatchers.Static {
        override val structureValidator = validation
        override val failFastStructureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)
    }

fun FeatureMatchers(
    featureName: String,
    validation: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
): FeatureMatchers =
    when (featureName.contains("{N}")) {
        true -> Indexed(featureName, validation, failFast)
        false -> Static(featureName, validation, failFast)
    }

inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    matchers: FeatureMatchers,
    noinline factory: (Feature) -> F,
    rule: ValidationRule<Feature, ValidationError>,
): FeatureDescriptor<F> =
    when (matchers) {
        is FeatureMatchers.Indexed -> {
            FeatureDescriptorIndexedImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = FeatureFactory(F::class, factory),
                matchers = matchers,
                rule = rule,
            )
        }

        is FeatureMatchers.Static -> {
            FeatureDescriptorStaticImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = FeatureFactory(F::class, factory),
                matchers = matchers,
                rule = rule,
            )
        }
    }

/**
 * Creates a descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when fail-fast validation must stop after the first invalid rule while regular validation
 * still aggregates all validation results.
 */
inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor<F> =
    FeatureDescriptor(
        wildcardName = wildcardName,
        matchers = FeatureMatchers(wildcardName, rule, failFast),
        factory = factory,
        rule = rule,
    )

/**
 * Creates a static feature descriptor when the same rule is used for regular and fail-fast validation.
 *
 * This avoids generating or passing a separate fail-fast rule for cases where both validation paths have the
 * same behavior, such as descriptors backed by a single validation rule.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Static(wildcardName, rule),
        rule = rule,
    )

/**
 * Creates a static feature descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when regular validation should collect all structure errors but matcher validation should use
 * the supplied fail-fast rule.
 */
inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Static(wildcardName, rule, failFast),
        rule = rule,
    )

/**
 * Creates an indexed feature descriptor when regular validation and fail-fast validation use different rules.
 *
 * Use this overload when regular validation should collect all structure errors but matcher validation should use
 * the supplied fail-fast rule.
 */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    failFast: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Indexed(wildcardName, rule, failFast),
        rule = rule,
    )

/**
 * Creates an indexed feature descriptor when the same rule is used for regular and fail-fast validation.
 *
 * This avoids generating or passing a separate fail-fast rule for cases where both validation paths have the
 * same behavior, such as descriptors backed by a single validation rule.
 */
inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    noinline factory: (Feature) -> F,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = FeatureFactory(F::class, factory),
        matchers = Indexed(wildcardName, rule),
        rule = rule,
    )

@PublishedApi
internal class FeatureDescriptorIndexedImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    private val factory: FeatureFactory<F>,
    private val matchers: FeatureMatchers.Indexed,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Indexed<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Indexed by matchers,
    ValidationRule<Feature, ValidationError> by rule {
    override fun getOrNull(feature: Feature): F? = getOrNullWithoutValidationException(feature, featureClass, factory, matchers.byStructure)

    override fun toString(): String = "FeatureDescriptor.Indexed[$wildcardName](${featureClass.simpleName})"
}

@PublishedApi
internal class FeatureDescriptorStaticImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    private val factory: FeatureFactory<F>,
    private val matchers: FeatureMatchers.Static,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Static<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Static by matchers,
    ValidationRule<Feature, ValidationError> by rule {
    override fun getOrNull(feature: Feature): F? = getOrNullWithoutValidationException(feature, featureClass, factory, matchers.byStructure)

    override fun toString(): String = "FeatureDescriptor.Static[$wildcardName](${featureClass.simpleName})"
}

private fun <F : Feature> getOrNullWithoutValidationException(
    feature: Feature,
    featureClass: KClass<F>,
    factory: FeatureFactory<F>,
    structureMatcher: FeatureMatcher,
): F? {
    if (featureClass.isInstance(feature)) {
        @Suppress("UNCHECKED_CAST")
        return feature as F
    }
    if (!structureMatcher.matches(feature)) return null
    return factory.getOrNull(feature)
}
