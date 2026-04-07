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
        factory: FeatureDescriptor<F>,
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

/**
 * A basic [FeatureRegistry] implementation without any caching.
 * Every lookup performs a linear scan over the feature list.
 */
private class BasicFeatureRegistry(
    private val features: List<Feature>,
) : FeatureRegistry {
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
        factory: FeatureDescriptor<F>,
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
    companion object {
        private val MARKER = Any()
    }

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

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> {
                candidateView(matcher.wildcardName.name).find(matcher.structure::matches)
            }

            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> {
                candidateView(matcher.wildcardName.name).find(matcher.structure::matches)
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl -> {
                val idx = byExactNameIndex[matcher.name.name] ?: return null
                sorted[idx].takeIf { matcher.structure.matches(it) }
            }

            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> {
                val idx = byExactNameIndex[matcher.name.name] ?: return null
                sorted[idx].takeIf { matcher.structure.matches(it) }
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

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> {
                candidateView(matcher.wildcardName.name).filter(matcher.structure::matches)
            }

            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> {
                candidateView(matcher.wildcardName.name).filter(matcher.structure::matches)
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl -> {
                val idx = byExactNameIndex[matcher.name.name]
                if (idx != null && matcher.structure.matches(sorted[idx])) listOf(sorted[idx]) else emptyList()
            }

            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> {
                val idx = byExactNameIndex[matcher.name.name]
                if (idx != null && matcher.structure.matches(sorted[idx])) listOf(sorted[idx]) else emptyList()
            }

            is FeatureMatcher.Companion.ByStructureImpl -> {
                sorted.filter(matcher::matches)
            }

            else -> {
                sorted.filter(matcher::matches)
            }
        }

    override fun <F : Feature> findOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F? = find(matcher)?.let(factory::getOrNull)

    override fun <F : Feature> firstOf(
        factory: FeatureFactory<F>,
        matcher: FeatureMatcher,
    ): F = factory.getOrThrow(first(matcher))

    override fun <F : Feature> allOf(
        factory: FeatureDescriptor<F>,
        matcher: FeatureMatcher,
    ): List<F> {
        val featureClass = factory.featureClass
        val range = candidateRangeForMatcher(matcher)
        return if (range != null) {
            buildList {
                for (i in range) {
                    val cache = implementations[i]
                    val cached = cache[featureClass]
                    if (cached != null && cached !== MARKER) {
                        @Suppress("UNCHECKED_CAST")
                        add(cached as F)
                    } else if (cached == null) {
                        if (matchesWithoutNameCheck(matcher, sorted[i])) {
                            val feature = factory.getOrThrow(sorted[i])
                            cache[featureClass] = feature
                            add(feature)
                        } else {
                            cache[featureClass] = MARKER
                        }
                    }
                }
            }
        } else {
            buildList {
                for (i in sorted.indices) {
                    val cache = implementations[i]
                    val cached = cache[featureClass]
                    if (cached != null && cached !== MARKER) {
                        @Suppress("UNCHECKED_CAST")
                        add(cached as F)
                    } else if (cached == null) {
                        if (matcher.matches(sorted[i])) {
                            val feature = factory.getOrThrow(sorted[i])
                            cache[featureClass] = feature
                            add(feature)
                        } else {
                            cache[featureClass] = MARKER
                        }
                    }
                }
            }
        }
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

            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> {
                candidateRange(matcher.wildcardName.name)
            }

            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> {
                candidateRange(matcher.wildcardName.name)
            }

            is FeatureMatcher.Companion.ByNameImpl -> {
                byExactNameIndex[matcher.name]?.let { it..it }
            }

            is FeatureMatcher.Companion.ByNameThenStructureImpl -> {
                byExactNameIndex[matcher.name.name]?.let { it..it }
            }

            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> {
                byExactNameIndex[matcher.name.name]?.let { it..it }
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
    ): Boolean =
        when (matcher) {
            is FeatureMatcher.Companion.ByWildcardNameThenStructureImpl -> matcher.structure.matches(feature)
            is FeatureMatcher.Companion.ByWildcardNameThenFailFastStructureImpl -> matcher.structure.matches(feature)
            is FeatureMatcher.Companion.ByNameThenStructureImpl -> matcher.structure.matches(feature)
            is FeatureMatcher.Companion.ByNameThenFailFastStructureImpl -> matcher.structure.matches(feature)
            else -> matcher.matches(feature)
        }

    @Suppress("UNCHECKED_CAST")
    private fun <F : Feature> cachedFirstOf(
        descriptor: FeatureDescriptor<F>,
        matcher: FeatureMatcher,
    ): F {
        val featureClass = descriptor.featureClass
        val range = candidateRangeForMatcher(matcher)
        if (range != null) {
            for (i in range) {
                val cache = implementations[i]
                val cached = cache[featureClass]
                if (cached != null && cached !== MARKER) {
                    return cached as F
                }
                if (cached == null && matchesWithoutNameCheck(matcher, sorted[i])) {
                    val feature = descriptor.getOrThrow(sorted[i])
                    cache[featureClass] = feature
                    return feature
                }
            }
        } else {
            for (i in sorted.indices) {
                val cache = implementations[i]
                val cached = cache[featureClass]
                if (cached != null && cached !== MARKER) {
                    return cached as F
                }
                if (cached == null && matcher.matches(sorted[i])) {
                    val feature = descriptor.getOrThrow(sorted[i])
                    cache[featureClass] = feature
                    return feature
                }
            }
        }
        throw NoSuchElementException("No feature matching ${descriptor.wildcardName}")
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
    private val features: List<Feature>,
) : FeatureRegistry {
    companion object {
        private val MARKER = Any()
    }

    private val implementations = List(features.size) { IdentityHashMap<KClass<out Feature>, Any>() }

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
        factory: FeatureDescriptor<F>,
        matcher: FeatureMatcher,
    ): List<F> =
        buildList {
            val featureClass = factory.featureClass
            for (i in features.indices) {
                val implementations = implementations[i]
                val implementation = implementations[featureClass]
                if (implementation != null && implementation !== MARKER) {
                    @Suppress("UNCHECKED_CAST")
                    add(implementation as F)
                } else if (implementation == null) {
                    if (matcher.matches(features[i])) {
                        val feature = factory.getOrThrow(features[i])
                        implementations[featureClass] = feature
                        add(feature)
                    } else {
                        implementations[featureClass] = MARKER
                    }
                }
            }
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

    @Suppress("UNCHECKED_CAST")
    private fun <F : Feature> cachedFirstOf(
        descriptor: FeatureDescriptor<F>,
        matcher: FeatureMatcher,
    ): F {
        val featureClass = descriptor.featureClass
        for (i in features.indices) {
            val cache = implementations[i]
            val cached = cache[featureClass]
            if (cached != null && cached !== MARKER) {
                return cached as F
            }
            if (cached == null && matcher.matches(features[i])) {
                val feature = descriptor.getOrThrow(features[i])
                cache[featureClass] = feature
                return feature
            }
        }
        throw NoSuchElementException("No feature matching ${descriptor.wildcardName}")
    }
}
