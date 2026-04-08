package xyz.dussim.viessmann.feature.api

import kotlin.reflect.KClass

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
         * Uses `is` checks on internal [FeatureMatcher] types to dispatch to the appropriate
         * index. Falls back to linear scan for custom/opaque matchers.
         *
         * Also includes per-feature caching of typed conversion results.
         *
         * The returned instance is **not thread-safe**.
         */
        fun indexed(features: List<Feature>): FeatureRegistry = IndexedFeatureRegistry(features)
    }
}

private val CACHE_MISS_MARKER = Any()

@Suppress("UNCHECKED_CAST")
private inline fun <F : Feature> cachedFindOfHelper(
    features: List<Feature>,
    implementations: List<IdentityHashMap<KClass<out Feature>, Any>>,
    featureClass: KClass<F>,
    range: IntRange,
    matches: (Feature) -> Boolean,
    convert: (Feature) -> F?,
): F? {
    for (i in range) {
        val cache = implementations[i]
        val cached = cache[featureClass]
        if (cached != null && cached !== CACHE_MISS_MARKER) {
            return cached as F
        }
        if (cached == null && matches(features[i])) {
            val feature = convert(features[i]) ?: return null
            cache[featureClass] = feature
            return feature
        }
    }
    return null
}

@Suppress("UNCHECKED_CAST")
private inline fun <F : Feature> cachedFirstOfHelper(
    features: List<Feature>,
    implementations: List<IdentityHashMap<KClass<out Feature>, Any>>,
    featureClass: KClass<F>,
    range: IntRange,
    matches: (Feature) -> Boolean,
    convert: (Feature) -> F,
    matcher: FeatureMatcher,
): F {
    for (i in range) {
        val cache = implementations[i]
        val cached = cache[featureClass]
        if (cached != null && cached !== CACHE_MISS_MARKER) {
            return cached as F
        }
        if (cached == null && matches(features[i])) {
            val feature = convert(features[i])
            cache[featureClass] = feature
            return feature
        }
    }
    throw NoSuchElementException("No feature matching $matcher")
}

@Suppress("UNCHECKED_CAST")
private inline fun <F : Feature> cachedAllOfHelper(
    features: List<Feature>,
    implementations: List<IdentityHashMap<KClass<out Feature>, Any>>,
    featureClass: KClass<F>,
    range: IntRange,
    matches: (Feature) -> Boolean,
    convert: (Feature) -> F,
): List<F> =
    buildList {
        for (i in range) {
            val cache = implementations[i]
            val cached = cache[featureClass]
            if (cached != null && cached !== CACHE_MISS_MARKER) {
                add(cached as F)
            } else if (cached == null) {
                if (matches(features[i])) {
                    val feature = convert(features[i])
                    cache[featureClass] = feature
                    add(feature)
                } else {
                    cache[featureClass] = CACHE_MISS_MARKER
                }
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

    override fun find(matcher: FeatureMatcher): Feature? = features.find(matcher::matches)

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> = features.filter(matcher::matches)

    override fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? = find(matcher)?.let(factory::getOrNull)

    override fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = factory.getOrThrow(first(matcher))

    override fun <F : Feature> allOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): List<F> = all(matcher).map(factory::getOrThrow)

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
 * enabling zero-copy [List.subList] views for wildcard-based lookups instead of separate index lists.
 *
 * Uses `is` checks on internal [FeatureMatcher] types to dispatch to the appropriate index,
 * reducing lookups from O(n) to O(1) for exact name matches and O(k) for wildcard name matches
 * (where k is the number of features sharing the same wildcard name).
 *
 * Also includes per-feature caching of typed conversion results using [IdentityHashMap] keyed
 * by [KClass], similar to [CachingFeatureRegistry].
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

    // Jump table: wildcard feature name → range into sorted list (zero-copy subList views)
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

    // Per-feature cache for typed conversion results (indexed into sorted list)
    private val implementations = List(sorted.size) { IdentityHashMap<KClass<out Feature>, Any>() }

    override val size: Int get() = sorted.size

    override fun iterator(): Iterator<Feature> = sorted.iterator()

    /**
     * Returns a zero-copy [List.subList] view of features matching the given wildcard name,
     * or an empty list if no features match.
     */
    private fun candidateView(wildcardName: String): List<Feature> {
        val range = byWildcardNameRange[wildcardName] ?: return emptyList()
        return sorted.subList(range.first, range.last + 1)
    }

    /**
     * Returns the [IntRange] of indices into [sorted] for the given wildcard name,
     * or null if no features match.
     */
    private fun candidateRange(wildcardName: String): IntRange? = byWildcardNameRange[wildcardName]

    override fun find(matcher: FeatureMatcher): Feature? =
        when (matcher) {
            is FeatureMatcher.Companion.ByNameImpl -> {
                byExactNameIndex[matcher.name]?.let { sorted[it] }
            }

            is FeatureMatcher.Companion.ByWildcardNameImpl -> {
                candidateView(matcher.name).firstOrNull()
            }

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl,
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl,
            -> {
                candidateView(matcher.wildcardNameOrNull()!!).find(matcher::matches)
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl,
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl,
            -> {
                val idx = byExactNameIndex[matcher.exactNameOrNull()!!] ?: return null
                sorted[idx].takeIf(matcher::matches)
            }

            else -> {
                sorted.find(matcher::matches)
            }
        }

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> =
        when (matcher) {
            is FeatureMatcher.Companion.ByNameImpl -> {
                val idx = byExactNameIndex[matcher.name]
                if (idx != null) listOf(sorted[idx]) else emptyList()
            }

            is FeatureMatcher.Companion.ByWildcardNameImpl -> {
                candidateView(matcher.name).toList()
            }

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl,
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl,
            -> {
                candidateView(matcher.wildcardNameOrNull()!!).filter(matcher::matches)
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl,
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl,
            -> {
                val idx = byExactNameIndex[matcher.exactNameOrNull()!!]
                if (idx != null && matcher.matches(sorted[idx])) listOf(sorted[idx]) else emptyList()
            }

            else -> {
                sorted.filter(matcher::matches)
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
        val range = candidateRangeForMatcher(matcher)
        return cachedAllOfHelper(
            sorted,
            implementations,
            factory.featureClass,
            range ?: sorted.indices,
            if (range != null) { f -> matchesWithoutNameCheck(matcher, f) } else matcher::matches,
            factory::getOrThrow,
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
     * Returns the [IntRange] of candidate indices from the jump table if the matcher type
     * is recognized, or null to signal a full linear scan is needed.
     */
    private fun candidateRangeForMatcher(matcher: FeatureMatcher): IntRange? =
        when (matcher) {
            is FeatureMatcher.Companion.ByWildcardNameImpl -> {
                candidateRange(matcher.name)
            }

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl,
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl,
            -> {
                candidateRange(matcher.wildcardNameOrNull()!!)
            }

            is FeatureMatcher.Companion.ByNameImpl -> {
                byExactNameIndex[matcher.name]?.let { it..it }
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl,
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl,
            -> {
                byExactNameIndex[matcher.exactNameOrNull()!!]?.let { it..it }
            }

            else -> {
                null
            }
        }

    /**
     * For combined matchers where the name part was already used to narrow candidates,
     * only apply the structure check.
     */
    private fun matchesWithoutNameCheck(
        matcher: FeatureMatcher,
        feature: Feature,
    ): Boolean = matcher.structureOrNull()?.matches(feature) ?: matcher.matches(feature)

    private fun <F : Feature> cachedFindOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? {
        val range = candidateRangeForMatcher(matcher)
        return cachedFindOfHelper(
            sorted,
            implementations,
            factory.featureClass,
            range ?: sorted.indices,
            if (range != null) { f -> matchesWithoutNameCheck(matcher, f) } else matcher::matches,
            factory::getOrNull,
        )
    }

    private fun <F : Feature> cachedFirstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F {
        val range = candidateRangeForMatcher(matcher)
        return cachedFirstOfHelper(
            sorted,
            implementations,
            factory.featureClass,
            range ?: sorted.indices,
            if (range != null) { f -> matchesWithoutNameCheck(matcher, f) } else matcher::matches,
            factory::getOrThrow,
            matcher,
        )
    }

    private fun FeatureMatcher.wildcardNameOrNull(): String? =
        when (this) {
            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> wildcardName.name
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> wildcardName.name
            else -> null
        }

    private fun FeatureMatcher.exactNameOrNull(): String? =
        when (this) {
            is FeatureMatcher.Companion.ByNameThenStructureImpl -> name.name
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> name.name
            else -> null
        }

    private fun FeatureMatcher.structureOrNull(): FeatureMatcher.Companion.ByStructureImpl? =
        when (this) {
            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> structure
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> structure
            is FeatureMatcher.Companion.ByNameThenStructureImpl -> structure
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> structure
            else -> null
        }
}

/**
 * A [FeatureRegistry] implementation that caches typed conversion results using per-feature
 * [IdentityHashMap] instances keyed by [KClass].
 *
 * This class is **not thread-safe**. It uses internal mutable caches that are populated lazily
 * on first access. It is designed to be created per API response and used from a single thread
 * or coroutine.
 *
 * Since feature factories return implementations of interfaces, all implementations for the same
 * [KClass] are expected to be equivalent. This is an accepted trade-off for using [KClass] as
 * the cache key.
 */
private class CachingFeatureRegistry(
    features: List<Feature>,
) : FeatureRegistry {
    private val features: List<Feature> = features.toList()
    private val implementations = List(this.features.size) { IdentityHashMap<KClass<out Feature>, Any>() }

    override val size: Int get() = features.size

    override fun iterator(): Iterator<Feature> = features.iterator()

    override fun find(matcher: FeatureMatcher): Feature? = features.find(matcher::matches)

    override fun first(matcher: FeatureMatcher): Feature = find(matcher) ?: throw NoSuchElementException("No feature matching $matcher")

    override fun all(matcher: FeatureMatcher): List<Feature> = features.filter(matcher::matches)

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
            implementations,
            factory.featureClass,
            features.indices,
            matcher::matches,
            factory::getOrThrow,
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
            implementations,
            factory.featureClass,
            features.indices,
            matcher::matches,
            factory::getOrNull,
        )

    private fun <F : Feature> cachedFirstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F =
        cachedFirstOfHelper(
            features,
            implementations,
            factory.featureClass,
            features.indices,
            matcher::matches,
            factory::getOrThrow,
            matcher,
        )
}
