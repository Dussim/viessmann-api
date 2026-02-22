package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import kotlin.reflect.KClass

fun Indexed(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Indexed =
    object : FeatureMatchers.Indexed {
        override val structureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = byWildcardName andThen byStructure

        override fun byName(index: Int): FeatureMatcher = FeatureMatcher.byName(wildcardName.replace("{}", index.toString()))

        override fun byNameThenStructure(index: Int): FeatureMatcher = byName(index) andThen byStructure
    }

fun Static(
    wildcardName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers.Static =
    object : FeatureMatchers.Static {
        override val structureValidator = validation

        override val byWildcardName = FeatureMatcher.byWildcardName(wildcardName)
        override val byStructure = FeatureMatcher.byStructure(validation)
        override val byWildcardNameThenStructure = byWildcardName andThen byStructure
    }

fun FeatureMatchers(
    featureName: String,
    validation: ValidationRule<Feature, ValidationError>,
): FeatureMatchers =
    when (featureName.contains("{}")) {
        true -> Indexed(featureName, validation)
        false -> Static(featureName, validation)
    }

inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    matchers: FeatureMatchers,
    factory: FeatureFactory<F>,
    rule: ValidationRule<Feature, ValidationError>,
): FeatureDescriptor<F> =
    when (matchers) {
        is FeatureMatchers.Indexed -> {
            FeatureDescriptorIndexedImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = factory,
                matchers = matchers,
                rule = rule,
            )
        }

        is FeatureMatchers.Static -> {
            FeatureDescriptorStaticImpl(
                featureClass = F::class,
                wildcardName = wildcardName,
                factory = factory,
                matchers = matchers,
                rule = rule,
            )
        }
    }

inline fun <reified F : Feature> FeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    factory: FeatureFactory<F>,
): FeatureDescriptor<F> =
    FeatureDescriptor(
        wildcardName = wildcardName,
        matchers = FeatureMatchers(wildcardName, rule),
        factory = factory,
        rule = rule,
    )

inline fun <reified F : Feature> staticFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    factory: FeatureFactory<F>,
): FeatureDescriptor.Static<F> =
    FeatureDescriptorStaticImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = factory,
        matchers = Static(wildcardName, rule),
        rule = rule,
    )

inline fun <reified F : Feature> indexedFeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    factory: FeatureFactory<F>,
): FeatureDescriptor.Indexed<F> =
    FeatureDescriptorIndexedImpl(
        featureClass = F::class,
        wildcardName = wildcardName,
        factory = factory,
        matchers = Indexed(wildcardName, rule),
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
    override fun toString(): String = "FeatureDescriptor.Indexed[$wildcardName]"
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
    override fun toString(): String = "FeatureDescriptor.Static[$wildcardName]"
}
