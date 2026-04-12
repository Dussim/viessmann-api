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
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byFailFastStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)
        override val byWildcardNameThenFailFastStructure = FeatureMatcher.byWildcardNameThenFailFastStructure(wildcardName, failFast)

        override fun byName(index: Int): FeatureMatcher = FeatureMatcher.byName(wildcardName.replace("{N}", index.toString()))

        override fun byNameThenStructure(index: Int): FeatureMatcher = FeatureMatcher.byNameThenStructure(wildcardName.replace("{N}", index.toString()), validation)

        override fun byNameThenFailFastStructure(index: Int): FeatureMatcher = FeatureMatcher.byNameThenFailFastStructure(wildcardName.replace("{N}", index.toString()), failFast)
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
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byFailFastStructure = FeatureMatcher.byStructure(failFast)
        override val byWildcardNameThenStructure = FeatureMatcher.byWildcardNameThenStructure(wildcardName, validation)
        override val byWildcardNameThenFailFastStructure = FeatureMatcher.byWildcardNameThenFailFastStructure(wildcardName, failFast)
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

@PublishedApi
internal class FeatureDescriptorIndexedImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    factory: FeatureFactory<F>,
    matchers: FeatureMatchers.Indexed,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Indexed<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Indexed by matchers,
    ValidationRule<Feature, ValidationError> by rule {
    override fun toString(): String = "FeatureDescriptor.Indexed[$wildcardName](${featureClass.simpleName})"
}

@PublishedApi
internal class FeatureDescriptorStaticImpl<F : Feature>(
    override val featureClass: KClass<F>,
    override val wildcardName: String,
    factory: FeatureFactory<F>,
    matchers: FeatureMatchers.Static,
    rule: ValidationRule<Feature, ValidationError>,
) : FeatureDescriptor.Static<F>,
    FeatureFactory<F> by factory,
    FeatureMatchers.Static by matchers,
    ValidationRule<Feature, ValidationError> by rule {
    override fun toString(): String = "FeatureDescriptor.Static[$wildcardName](${featureClass.simpleName})"
}
