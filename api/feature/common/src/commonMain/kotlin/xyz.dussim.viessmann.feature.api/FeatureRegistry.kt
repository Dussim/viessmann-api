package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.FeatureMatcher.Companion.ByNameThenStructureImpl
import xyz.dussim.viessmann.feature.api.FeatureMatcher.Companion.ByStructureImpl
import xyz.dussim.viessmann.feature.api.FeatureMatcher.Companion.ByWildcardNameThenStructureImpl
import xyz.dussim.viessmann.feature.api.validation.ValidationRule

/**
 * A registry of [Feature] instances that provides lookup and typed conversion capabilities.
 *
 * Extends [Iterable] to allow iteration over all contained features.
 */
interface FeatureRegistry : Iterable<Feature> {
    /**
     * The number of features in this registry.
     */
    val size: Int

    /**
     * Finds the first feature matching the given [matcher], or returns null if none found.
     */
    fun find(matcher: FeatureMatcher): Feature?

    /**
     * Finds the first feature matching the given [matcher], or throws [NoSuchElementException].
     */
    fun first(matcher: FeatureMatcher): Feature

    /**
     * Returns all features matching the given [matcher].
     */
    fun all(matcher: FeatureMatcher): List<Feature>

    /**
     * Finds and converts the first feature matching [matcher] using [factory], or returns null.
     */
    fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F?

    /**
     * Finds and converts the first feature matching [matcher] using [factory], or throws.
     */
    fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F

    /**
     * Finds and converts all features matching [matcher] using [factory].
     */
    fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): List<F>

    /**
     * Operator shorthand for [firstOf].
     */
    operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F

    /**
     * Looks up a static feature by its descriptor.
     */
    operator fun <F : Feature> get(descriptor: FeatureDescriptor.Static<F>): F

    /**
     * Looks up an indexed feature by its descriptor (wildcard match).
     */
    operator fun <F : Feature> get(descriptor: FeatureDescriptor.Indexed<F>): F

    /**
     * Looks up an indexed feature by its descriptor and specific index.
     */
    operator fun <F : Feature> get(
        descriptor: FeatureDescriptor.Indexed<F>,
        index: Int,
    ): F

    companion object {
        /**
         * Creates a basic [FeatureRegistry] without any caching.
         * Suitable for one-off lookups or small feature lists.
         */
        fun of(features: List<Feature>): FeatureRegistry = BasicFeatureRegistry(features)

        /**
         * Creates a caching [FeatureRegistry] that memoizes typed conversion results.
         *
         * The returned instance is **not thread-safe**. It uses internal mutable caches
         * that are populated lazily on first access. It is designed to be created per API
         * response and used from a single thread or coroutine.
         */
        fun caching(features: List<Feature>): FeatureRegistry = CachingFeatureRegistry(features)

        /**
         * Creates an indexed [FeatureRegistry] that builds jump tables at construction time
         * for O(1) lookups by name and O(k) lookups by wildcard name (where k is the number
         * of features sharing the same wildcard name).
         *
         * Uses internal [FeatureMatcher] indexing metadata to dispatch to the appropriate index.
         * Falls back to linear scan for custom/opaque matchers.
         *
         * Also includes per-feature caching of typed conversion results.
         *
         * The returned instance is **not thread-safe**.
         */
        fun indexed(features: List<Feature>): FeatureRegistry = IndexedFeatureRegistry(features)
    }
}

private val EMPTY_CANDIDATE_RANGE = IntRange.EMPTY

private fun findMatching(
    features: List<Feature>,
    matcher: FeatureMatcher,
): Feature? {
    for (feature in features) {
        if (matcher.matches(feature)) return feature
    }
    return null
}

private fun allMatching(
    features: List<Feature>,
    matcher: FeatureMatcher,
): List<Feature> =
    buildList {
        for (feature in features) {
            if (matcher.matches(feature)) add(feature)
        }
    }

private val STRUCTURE_ACCEPTED = Any()
private val STRUCTURE_REJECTED = Any()
private val NO_MATCH = Any()

/**
 * Per-feature memo of a registry, keyed by identity. A feature class maps to the typed conversion of that feature; a
 * structure rule maps to [STRUCTURE_ACCEPTED] or [STRUCTURE_REJECTED], the outcome of that rule for that feature.
 * Both rely on features being immutable and on rules and factories being deterministic.
 *
 * A feature's map is created on its first write: most features of a response are never looked up.
 */
private class ConversionCaches(
    size: Int,
) {
    private val caches = arrayOfNulls<MutableMap<Any, Any>>(size)

    operator fun get(
        index: Int,
        key: Any,
    ): Any? = caches[index]?.get(key)

    operator fun set(
        index: Int,
        key: Any,
        value: Any,
    ) {
        val cache = caches[index] ?: IdentityHashMap<Any, Any>().also { caches[index] = it }
        cache[key] = value
    }
}

/**
 * Decides whether the feature at [index] is a candidate for [matcher].
 *
 * Returns [NO_MATCH] when the matcher rejects the feature, `null` when it accepts it and the caller still has to
 * convert it, or the converted feature when the conversion already happened here.
 *
 * Structure matchers created by [FeatureMatcher] are not run on every lookup: the outcome of their rule is memoized
 * per feature. On the first lookup of a feature through a descriptor that converts in a single pass for that rule,
 * the conversion itself decides the match, instead of validating first and then checking everything again while
 * converting. A feature that already is an instance of the feature class still goes through the rule, because
 * converting it is a pass-through that checks nothing.
 */
private fun <F : Feature> matchCandidate(
    feature: Feature,
    index: Int,
    caches: ConversionCaches,
    matcher: FeatureMatcher?,
    factory: FeatureFactory<F>,
): Any? {
    if (matcher == null) return null
    val rule: ValidationRule<Feature, *> =
        when (matcher) {
            is ByStructureImpl -> {
                matcher.rule
            }

            is ByWildcardNameThenStructureImpl -> {
                if (!matcher.wildcardName.matches(feature)) return NO_MATCH
                matcher.structure.rule
            }

            is ByNameThenStructureImpl -> {
                if (!matcher.name.matches(feature)) return NO_MATCH
                matcher.structure.rule
            }

            else -> {
                return if (matcher.matches(feature)) null else NO_MATCH
            }
        }

    val memo = caches[index, rule]
    if (memo != null) return if (memo === STRUCTURE_ACCEPTED) null else NO_MATCH

    val featureClass = factory.featureClass
    if (
        factory is SinglePassDescriptor &&
        factory.convertsInSinglePassFor(rule) &&
        caches[index, featureClass] == null &&
        !featureClass.hasInstance(feature)
    ) {
        val converted = factory.getOrNull(feature)
        if (converted == null) {
            caches[index, rule] = STRUCTURE_REJECTED
            return NO_MATCH
        }
        caches[index, rule] = STRUCTURE_ACCEPTED
        caches[index, featureClass] = converted
        return converted
    }

    val accepted = !rule.validate(feature).isInvalid
    caches[index, rule] = if (accepted) STRUCTURE_ACCEPTED else STRUCTURE_REJECTED
    return if (accepted) null else NO_MATCH
}

@Suppress("UNCHECKED_CAST")
private fun <F : Feature> cachedFindOfHelper(
    features: List<Feature>,
    caches: ConversionCaches,
    range: IntRange,
    matcher: FeatureMatcher?,
    factory: FeatureFactory<F>,
): F? {
    val featureClass = factory.featureClass
    for (i in range) {
        val feature = features[i]
        val match = matchCandidate(feature, i, caches, matcher, factory)
        if (match === NO_MATCH) continue
        if (match != null) return match as F

        val cached = caches[i, featureClass]
        if (cached != null) {
            return cached as F
        }
        val converted = factory.getOrNull(feature) ?: return null
        caches[i, featureClass] = converted
        return converted
    }
    return null
}

@Suppress("UNCHECKED_CAST")
private fun <F : Feature> cachedFirstOfHelper(
    features: List<Feature>,
    caches: ConversionCaches,
    range: IntRange,
    matcher: FeatureMatcher?,
    factory: FeatureFactory<F>,
    failureMatcher: FeatureMatcher,
): F {
    val featureClass = factory.featureClass
    for (i in range) {
        val feature = features[i]
        val match = matchCandidate(feature, i, caches, matcher, factory)
        if (match === NO_MATCH) continue
        if (match != null) return match as F

        val cached = caches[i, featureClass]
        if (cached != null) {
            return cached as F
        }
        val converted = factory.getOrThrow(feature)
        caches[i, featureClass] = converted
        return converted
    }
    throw NoSuchElementException("No feature matching $failureMatcher")
}

@Suppress("UNCHECKED_CAST")
private fun <F : Feature> cachedAllOfHelper(
    features: List<Feature>,
    caches: ConversionCaches,
    range: IntRange,
    matcher: FeatureMatcher?,
    factory: FeatureFactory<F>,
): List<F> =
    buildList {
        val featureClass = factory.featureClass
        for (i in range) {
            val feature = features[i]
            val match = matchCandidate(feature, i, caches, matcher, factory)
            if (match === NO_MATCH) continue
            if (match != null) {
                add(match as F)
                continue
            }

            val cached = caches[i, featureClass]
            if (cached != null) {
                add(cached as F)
            } else {
                val converted = factory.getOrThrow(feature)
                caches[i, featureClass] = converted
                add(converted)
            }
        }
    }

/**
 * A basic [FeatureRegistry] implementation without any caching.
 * Every lookup performs a linear scan over the feature list.
 */
private class BasicFeatureRegistry(
    features: List<Feature>,
) : FeatureRegistry {
    private val features: List<Feature> = features.toList()

    override val size: Int get() = features.size

    override fun iterator(): Iterator<Feature> = features.iterator()

    override fun find(matcher: FeatureMatcher): Feature? = findMatching(features, matcher)

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> = allMatching(features, matcher)

    override fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? {
        val feature = find(matcher) ?: return null
        return factory.getOrNull(feature)
    }

    override fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = factory.getOrThrow(first(matcher))

    override fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): List<F> =
        all(matcher).map { feature ->
            factory.getOrThrow(feature)
        }

    override operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = firstOf(factory, matcher)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Static<F>): F = firstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Indexed<F>): F = firstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(
        descriptor: FeatureDescriptor.Indexed<F>,
        index: Int,
    ): F = firstOf(descriptor, descriptor.byNameThenStructure(index))
}

/**
 * A [FeatureRegistry] implementation that sorts features by wildcard name at construction time,
 * enabling range scans for wildcard-based lookups instead of separate index lists.
 *
 * Uses internal [FeatureMatcher] indexing metadata to dispatch to the appropriate index,
 * reducing lookups from O(n) to O(1) for exact name matches and O(k) for wildcard name matches
 * (where k is the number of features sharing the same wildcard name).
 *
 * Also includes per-feature caching of typed conversion results and structure-rule outcomes, keyed
 * by identity, similar to [CachingFeatureRegistry].
 *
 * Falls back to linear scan for custom/opaque matchers (e.g., those created via [andThen]).
 *
 * This class is **not thread-safe**.
 */
private class IndexedFeatureRegistry(
    features: List<Feature>,
) : FeatureRegistry {
    // Sorted by wildcard name so features with the same wildcard are contiguous
    private val sorted: List<Feature> = features.sortedBy { it.wildcardFeature }

    // Jump table: wildcard feature name → range into sorted list
    private val byWildcardNameRange: Map<String, IntRange> =
        buildMap {
            var i = 0
            while (i < sorted.size) {
                val name = sorted[i].wildcardFeature
                val start = i
                while (i < sorted.size && sorted[i].wildcardFeature == name) i++
                put(name, start until i)
            }
        }

    // Jump table: exact feature name → index into sorted list
    private val byExactNameIndex: Map<String, Int> =
        buildMap {
            for (i in sorted.indices) {
                put(sorted[i].feature, i)
            }
        }

    // Per-feature cache for typed conversion results and structure-rule outcomes (indexed into sorted list)
    private val conversions = ConversionCaches(sorted.size)

    override val size: Int get() = sorted.size

    override fun iterator(): Iterator<Feature> = sorted.iterator()

    /**
     * Returns the [IntRange] of indices into [sorted] for the given wildcard name,
     * or null if no features match.
     */
    private fun candidateRange(wildcardName: String): IntRange? = byWildcardNameRange[wildcardName]

    override fun find(matcher: FeatureMatcher): Feature? {
        val indexedMatcher =
            matcher as? NameIndexedFeatureMatcher
                ?: return findMatching(sorted, matcher)
        val candidateMatcher = indexedMatcher.indexedCandidateMatcher

        for (i in candidateRangeForMatcher(indexedMatcher)) {
            val feature = sorted[i]
            if (candidateMatcher == null || candidateMatcher.matches(feature)) return feature
        }
        return null
    }

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> {
        val indexedMatcher =
            matcher as? NameIndexedFeatureMatcher
                ?: return allMatching(sorted, matcher)

        return buildList {
            val candidateMatcher = indexedMatcher.indexedCandidateMatcher
            for (i in candidateRangeForMatcher(indexedMatcher)) {
                val feature = sorted[i]
                if (candidateMatcher == null || candidateMatcher.matches(feature)) add(feature)
            }
        }
    }

    override fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? = cachedFindOf(factory, matcher)

    override fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = cachedFirstOf(factory, matcher)

    override fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): List<F> {
        val indexedMatcher = matcher as? NameIndexedFeatureMatcher
        val candidateMatcher = if (indexedMatcher != null) indexedMatcher.indexedCandidateMatcher else matcher
        val range = if (indexedMatcher != null) candidateRangeForMatcher(indexedMatcher) else sorted.indices
        return cachedAllOfHelper(
            sorted,
            conversions,
            range,
            candidateMatcher,
            factory,
        )
    }

    override operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = firstOf(factory, matcher)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Static<F>): F = cachedFirstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Indexed<F>): F = cachedFirstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(
        descriptor: FeatureDescriptor.Indexed<F>,
        index: Int,
    ): F = cachedFirstOf(descriptor, descriptor.byNameThenStructure(index))

    /**
     * Returns candidate indices from the jump table for matchers that expose name-index metadata.
     */
    private fun candidateRangeForMatcher(matcher: NameIndexedFeatureMatcher): IntRange =
        when (matcher.indexKind) {
            FeatureMatcherNameIndexKind.WILDCARD -> candidateRange(matcher.indexedName) ?: EMPTY_CANDIDATE_RANGE
            FeatureMatcherNameIndexKind.EXACT -> byExactNameIndex[matcher.indexedName]?.let { it..it } ?: EMPTY_CANDIDATE_RANGE
        }

    private fun <F : Feature> cachedFindOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? {
        val indexedMatcher = matcher as? NameIndexedFeatureMatcher
        val candidateMatcher = if (indexedMatcher != null) indexedMatcher.indexedCandidateMatcher else matcher
        val range = if (indexedMatcher != null) candidateRangeForMatcher(indexedMatcher) else sorted.indices
        return cachedFindOfHelper(
            sorted,
            conversions,
            range,
            candidateMatcher,
            factory,
        )
    }

    private fun <F : Feature> cachedFirstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F {
        val indexedMatcher = matcher as? NameIndexedFeatureMatcher
        val candidateMatcher = if (indexedMatcher != null) indexedMatcher.indexedCandidateMatcher else matcher
        val range = if (indexedMatcher != null) candidateRangeForMatcher(indexedMatcher) else sorted.indices
        return cachedFirstOfHelper(
            sorted,
            conversions,
            range,
            candidateMatcher,
            factory,
            matcher,
        )
    }
}

/**
 * A [FeatureRegistry] implementation that caches typed conversion results using per-feature
 * [IdentityHashMap] instances keyed by `KClass`. Structure-rule outcomes are memoized in the same maps.
 *
 * This class is **not thread-safe**. It uses internal mutable caches that are populated lazily
 * on first access. It is designed to be created per API response and used from a single thread
 * or coroutine.
 *
 * Since feature factories return implementations of interfaces, all implementations for the same
 * `KClass` are expected to be equivalent. This is an accepted trade-off for using `KClass` as
 * the cache key.
 */
private class CachingFeatureRegistry(
    features: List<Feature>,
) : FeatureRegistry {
    private val features: List<Feature> = features.toList()
    private val conversions = ConversionCaches(this.features.size)

    override val size: Int get() = features.size

    override fun iterator(): Iterator<Feature> = features.iterator()

    override fun find(matcher: FeatureMatcher): Feature? = findMatching(features, matcher)

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> = allMatching(features, matcher)

    override fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? = cachedFindOf(factory, matcher)

    override fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = cachedFirstOf(factory, matcher)

    override fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): List<F> =
        cachedAllOfHelper(
            features,
            conversions,
            features.indices,
            matcher,
            factory,
        )

    override operator fun <F : Feature> get(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = firstOf(factory, matcher)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Static<F>): F = cachedFirstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(descriptor: FeatureDescriptor.Indexed<F>): F = cachedFirstOf(descriptor, descriptor.byWildcardNameThenStructure)

    override operator fun <F : Feature> get(
        descriptor: FeatureDescriptor.Indexed<F>,
        index: Int,
    ): F = cachedFirstOf(descriptor, descriptor.byNameThenStructure(index))

    private fun <F : Feature> cachedFindOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? =
        cachedFindOfHelper(
            features,
            conversions,
            features.indices,
            matcher,
            factory,
        )

    private fun <F : Feature> cachedFirstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F =
        cachedFirstOfHelper(
            features,
            conversions,
            features.indices,
            matcher,
            factory,
            matcher,
        )
}
