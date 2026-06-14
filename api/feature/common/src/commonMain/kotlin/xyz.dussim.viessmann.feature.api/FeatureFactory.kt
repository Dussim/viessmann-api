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
 * ## Implementation Details
 *
 * All factories for the same feature type [F] must return compatible implementations. This ensures
 * that consumers can rely on consistent behavior regardless of which factory instance they use.
 *
 * If a [Feature] instance already implements the target interface [F], the factory should return
 * it without any modifications. This pass-through behavior prevents unnecessary wrapping or conversion
 * when the feature is already of the correct type.
 *
 * @param F The specific feature type that extends [Feature] into which the conversion will be performed
 */
interface FeatureFactory<F : Feature> {
    /**
     * The [KClass] of the specific feature type [F] this factory produces.
     */
    val featureClass: KClass<F>

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

/**
 * Creates a [FeatureFactory] from a [KClass] and a conversion function.
 */
fun <F : Feature> FeatureFactory(
    featureClass: KClass<F>,
    getOrThrow: (Feature) -> F,
): FeatureFactory<F> =
    object : FeatureFactory<F> {
        override val featureClass: KClass<F> = featureClass

        override fun getOrThrow(feature: Feature): F = getOrThrow(feature)
    }

/**
 * Creates a [FeatureFactory] using reified type parameter to infer [FeatureFactory.featureClass].
 */
inline fun <reified F : Feature> FeatureFactory(noinline getOrThrow: (Feature) -> F): FeatureFactory<F> = FeatureFactory(F::class, getOrThrow)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class IdentityHashMap<K, V>() : MutableMap<K, V> {
    override fun clear()

    override fun put(
        key: K,
        value: V,
    ): V?

    override fun putAll(from: Map<out K, V>)

    override fun remove(key: K): V?

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>

    override fun containsKey(key: K): Boolean

    override fun containsValue(value: V): Boolean

    override fun get(key: K): V?

    override fun isEmpty(): Boolean

    override val size: Int
}

fun interface FeatureMatcher {
    companion object {
        internal class ByStructureImpl(
            internal val rule: ValidationRule<Feature, *>,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = !rule.validate(feature).isInvalid
        }

        internal class ByNameImpl(
            internal val name: String,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = feature.feature == name
        }

        internal class ByWildcardNameImpl(
            internal val name: String,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = feature.wildcardFeature == name
        }

        internal class ByWildcardNameThenStructureImpl(
            internal val wildcardName: ByWildcardNameImpl,
            internal val structure: ByStructureImpl,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = wildcardName.matches(feature) && structure.matches(feature)
        }

        internal class ByWildcardNameThenFailFastStructureImpl(
            internal val wildcardName: ByWildcardNameImpl,
            internal val structure: ByStructureImpl,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = wildcardName.matches(feature) && structure.matches(feature)
        }

        internal class ByNameThenStructureImpl(
            internal val name: ByNameImpl,
            internal val structure: ByStructureImpl,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = name.matches(feature) && structure.matches(feature)
        }

        internal class ByNameThenFailFastStructureImpl(
            internal val name: ByNameImpl,
            internal val structure: ByStructureImpl,
        ) : FeatureMatcher {
            override fun matches(feature: Feature): Boolean = name.matches(feature) && structure.matches(feature)
        }

        fun byName(name: String): FeatureMatcher = ByNameImpl(name)

        fun byWildcardName(name: String): FeatureMatcher = ByWildcardNameImpl(name)

        fun byStructure(rule: ValidationRule<Feature, ValidationError>): FeatureMatcher = ByStructureImpl(rule)

        fun byWildcardNameThenStructure(
            name: String,
            rule: ValidationRule<Feature, ValidationError>,
        ): FeatureMatcher = ByWildcardNameThenStructureImpl(ByWildcardNameImpl(name), ByStructureImpl(rule))

        fun byWildcardNameThenFailFastStructure(
            name: String,
            rule: ValidationRule<Feature, ValidationError>,
        ): FeatureMatcher = ByWildcardNameThenFailFastStructureImpl(ByWildcardNameImpl(name), ByStructureImpl(rule))

        fun byNameThenStructure(
            name: String,
            rule: ValidationRule<Feature, ValidationError>,
        ): FeatureMatcher = ByNameThenStructureImpl(ByNameImpl(name), ByStructureImpl(rule))

        fun byNameThenFailFastStructure(
            name: String,
            rule: ValidationRule<Feature, ValidationError>,
        ): FeatureMatcher = ByNameThenFailFastStructureImpl(ByNameImpl(name), ByStructureImpl(rule))
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
    override val featureClass: KClass<F>
    val wildcardName: String

    interface Static<F : Feature> :
        FeatureDescriptor<F>,
        FeatureMatchers.Static

    interface Indexed<F : Feature> :
        FeatureDescriptor<F>,
        FeatureMatchers.Indexed
}

/**
 * Creates an indexed and caching [FeatureRegistry] from the given list of features.
 *
 * This is a convenience constructor-like function that delegates to [FeatureRegistry.indexed].
 */
@Suppress("FunctionName")
fun FeatureRegistry(features: List<Feature>): FeatureRegistry = FeatureRegistry.indexed(features)

@Deprecated("Use FeatureRegistry", ReplaceWith("FeatureRegistry", "xyz.dussim.viessmann.feature.api.FeatureRegistry"))
@Suppress("FunctionName")
fun FeatureResolver(features: List<Feature>): FeatureRegistry = FeatureRegistry(features)
