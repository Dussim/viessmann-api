package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

fun <F : Feature> NamedFeatureFactory(
    wildcardName: String,
    factory: FeatureFactory<F>,
): NamedFeatureFactory<F> =
    object : NamedFeatureFactory<F>, FeatureFactory<F> by factory {
        override val wildcardName: String = wildcardName
    }

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

fun <F : Feature> FeatureDescriptor(
    matchers: FeatureMatchers,
    factory: NamedFeatureFactory<F>,
    rule: ValidationRule<Feature, ValidationError>,
): FeatureDescriptor<F> =
    when (matchers) {
        is FeatureMatchers.Indexed -> {
            object : FeatureDescriptor.Indexed<F>, NamedFeatureFactory<F> by factory, FeatureMatchers.Indexed by matchers, ValidationRule<Feature, ValidationError> by rule {
            }
        }

        is FeatureMatchers.Static -> {
            object : FeatureDescriptor.Static<F>, NamedFeatureFactory<F> by factory, FeatureMatchers.Static by matchers, ValidationRule<Feature, ValidationError> by rule {
            }
        }
    }

fun <F : Feature> FeatureDescriptor(
    wildcardName: String,
    rule: ValidationRule<Feature, ValidationError>,
    factory: FeatureFactory<F>,
): FeatureDescriptor<F> =
    FeatureDescriptor(
        matchers = FeatureMatchers(wildcardName, rule),
        factory = NamedFeatureFactory(wildcardName, factory),
        rule = rule,
    )
