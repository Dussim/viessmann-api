package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import kotlin.reflect.KClass

// TODO this should be refactored, first of all exceptions are expensive so it should rather default to getOrNull

/**
 * A factory interface for converting generic Feature instances into specific feature types.
 *
 * This interface provides methods to safely convert a generic [Feature] instance into a specific
 * feature type [F]. It includes both throwing and non-throwing conversion methods, as well as
 * an operator function for convenient usage.
 *
 * @param F The specific feature type that extends [Feature] into which the conversion will be performed
 */
fun interface FeatureFactory<F : Feature> {
    /**
     * Converts the given feature into a specific feature type F or throws an exception if conversion fails.
     *
     * @param feature The feature to convert
     * @return The converted feature of type F
     * @throws Exception if the conversion fails
     */
    fun getOrThrow(feature: Feature): F

    /**
     * Converts the given feature into a specific feature type F or returns null if conversion fails.
     *
     * @param feature The feature to convert
     * @return The converted feature of type F or null if conversion fails
     */
    fun getOrNull(feature: Feature): F? =
        try {
            getOrThrow(feature)
        } catch (_: Exception) {
            null
        }

    /**
     * Operator function that provides a convenient way to convert a feature by calling the factory as a function.
     * Delegates to getOrThrow().
     *
     * @param feature The feature to convert
     * @return The converted feature of type F
     * @throws Exception if the conversion fails
     */
    operator fun invoke(feature: Feature): F = getOrThrow(feature)
}

fun interface FeatureMatcher {
    companion object {
        private class ByStructureImpl(
            private val rule: ValidationRule<Feature, *>,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = !rule.validate(feature).isInvalid
        }

        private class ByNameImpl(
            private val name: String,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = feature.feature == name
        }

        private class ByWildcardNameImpl(
            private val name: String,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = feature.wildcardFeature == name
        }

        fun byName(name: String): FeatureMatcher = ByNameImpl(name)

        fun byWildcardName(name: String): FeatureMatcher = ByWildcardNameImpl(name)

        fun byStructure(rule: ValidationRule<Feature, ValidationError>): FeatureMatcher = ByStructureImpl(rule)
    }

    fun matches(feature: Feature): Boolean
}

infix fun FeatureMatcher.andThen(other: FeatureMatcher): FeatureMatcher = FeatureMatcher { matches(it) && other.matches(it) }

sealed interface FeatureMatcherProvider {
    val structureValidator: ValidationRule<Feature, ValidationError>
    val failFastStructureValidator: ValidationRule<Feature, ValidationError>

    val byWildcardName: FeatureMatcher
    val byStructure: FeatureMatcher
    val byFailFastStructure: FeatureMatcher
    val byWildcardNameThenStructure: FeatureMatcher
    val byWildcardNameThenFailFastStructure: FeatureMatcher
}

sealed interface FeatureMatchers : FeatureMatcherProvider {
    interface Static : FeatureMatchers

    interface Indexed : FeatureMatchers {
        fun byName(index: Int): FeatureMatcher

        fun byNameThenStructure(index: Int): FeatureMatcher

        fun byNameThenFailFastStructure(index: Int): FeatureMatcher
    }
}

sealed interface FeatureDescriptor<F : Feature> :
    FeatureFactory<F>,
    ValidationRule<Feature, ValidationError>,
    FeatureMatcherProvider {
    val featureClass: KClass<F>
    val wildcardName: String

    interface Static<F : Feature> :
        FeatureDescriptor<F>,
        FeatureMatchers.Static

    interface Indexed<F : Feature> :
        FeatureDescriptor<F>,
        FeatureMatchers.Indexed
}

class FeatureResolver(
    private val features: List<Feature>,
) {
    val size: Int
        get() = features.size

    fun find(matcher: FeatureMatcher): Feature? = features.find { matcher.matches(it) }

    fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    fun all(matcher: FeatureMatcher): List<Feature> = features.filter { matcher.matches(it) }

    fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ) = factory.ofNullable(find(matcher))

    fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ) = factory.getOrThrow(first(matcher))

    fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ) = buildList {
        for (feature in features) {
            if (matcher.matches(feature)) {
                add(factory.getOrThrow(feature))
            }
        }
    }

    operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ) = firstOf(factory, matcher)

    operator fun <F : Feature> get(descriptor: FeatureDescriptor.Static<F>) = firstOf(descriptor, descriptor.byWildcardNameThenStructure)

    operator fun <F : Feature> get(descriptor: FeatureDescriptor.Indexed<F>) = firstOf(descriptor, descriptor.byWildcardNameThenStructure)

    operator fun <F : Feature> get(
        descriptor: FeatureDescriptor.Indexed<F>,
        index: Int,
    ) = firstOf(descriptor, descriptor.byNameThenStructure(index))

    private fun <F : Feature> FeatureFactory<F>.ofNullable(feature: Feature?) =
        when (feature) {
            null -> null
            else -> getOrNull(feature)
        }
}
