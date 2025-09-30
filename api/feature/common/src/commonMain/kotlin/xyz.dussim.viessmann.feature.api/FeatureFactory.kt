package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.invoke
import kotlin.jvm.JvmRecord

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
        private class ByValidationImpl(
            private val rule: ValidationRule<Feature, *>,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = !rule(feature).isInvalid
        }

        private class ByNameImpl(
            private val name: String,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = feature.feature == name
        }

        fun byName(name: String) = FeatureMatcher { ByNameImpl(name).matches(it) }

        fun byValidation(rule: ValidationRule<Feature, *>): FeatureMatcher = ByValidationImpl(rule)

        fun default(
            name: String,
            rule: ValidationRule<Feature, *>,
        ) = byName(name) andThen byValidation(rule)
    }

    fun matches(feature: Feature): Boolean
}

infix fun FeatureMatcher.andThen(other: FeatureMatcher): FeatureMatcher = FeatureMatcher { matches(it) && other.matches(it) }

@JvmRecord
data class FeatureMatchers(
    val byName: FeatureMatcher,
    val byValidation: FeatureMatcher,
)

@JvmRecord
data class FeatureUtils(
    val factory: FeatureFactory<*>,
    val matchers: FeatureMatchers,
    val validation: ValidationRule<Feature, *>,
)

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

    operator fun get(matcher: FeatureMatcher) = first(matcher)

    operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ) = firstOf(factory, matcher)

    private fun <F : Feature> FeatureFactory<F>.ofNullable(feature: Feature?) =
        when (feature) {
            null -> null
            else -> getOrNull(feature)
        }
}
