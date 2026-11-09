package xyz.dussim.feature.benchmark.stable.registry

import xyz.dussim.feature.benchmark.stable.StableCase
import xyz.dussim.feature.benchmark.stable.StableFixtureRepository
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.feature.benchmark.stable.registry.definitions.StableRegistryIndexedFeature
import xyz.dussim.feature.benchmark.stable.small.definitions.StableSmallFeature
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.feature.benchmark.stable.registry.definitions.descriptor as registryIndexedDescriptor
import xyz.dussim.feature.benchmark.stable.small.definitions.descriptor as smallDescriptor

/**
 * Versioned input for the stable registry benchmarks. It is derived from the manifest-verified stable validation
 * fixtures only, so it does not depend on the OpenAPI input.
 *
 * The feature list holds the valid small, medium and large fixtures followed by [INDEXED_FEATURE_COUNT] copies of the
 * valid small fixture renamed to `benchmark.stable.registry.0` … `benchmark.stable.registry.63`. Changing the counts,
 * the looked-up indices or the order changes the workload; results are comparable only across identical values.
 */
object StableRegistryFixture {
    const val INDEXED_WILDCARD_NAME = "benchmark.stable.registry.{N}"
    const val INDEXED_FEATURE_COUNT = 64

    /** Index inside the range for which indexed descriptors keep their matchers (`0..15`). */
    const val LOOKUP_INDEX = 7

    /** Index beyond that range: the descriptor builds a new matcher on every lookup. */
    const val UNCACHED_MATCHER_LOOKUP_INDEX = 31

    const val STATIC_FEATURE_NAME = "benchmark.stable.small"
    const val INDEXED_LOOKUP_FEATURE_NAME = "benchmark.stable.registry.7"
    const val UNCACHED_MATCHER_LOOKUP_FEATURE_NAME = "benchmark.stable.registry.31"
    const val FIRST_INDEXED_FEATURE_NAME = "benchmark.stable.registry.0"

    val staticDescriptor: FeatureDescriptor.Static<StableSmallFeature> get() = StableSmallFeature.smallDescriptor

    val indexedDescriptor: FeatureDescriptor.Indexed<StableRegistryIndexedFeature> get() = StableRegistryIndexedFeature.registryIndexedDescriptor

    fun features(): List<Feature> {
        val fixtures = StableFixtureRepository.load(verifyManifest = true)
        val staticFeatures = StableSize.entries.map { size -> fixtures[size, StableCase.VALID].feature }
        val indexedTemplate =
            checkNotNull(fixtures[StableSize.SMALL, StableCase.VALID].feature as? ViessmannFeature) {
                "Stable small fixture must be a ViessmannFeature to derive indexed registry features"
            }
        val indexedFeatures =
            List(INDEXED_FEATURE_COUNT) { index ->
                indexedTemplate.copy(
                    feature = INDEXED_WILDCARD_NAME.replace("{N}", index.toString()),
                    wildcardFeature = INDEXED_WILDCARD_NAME,
                    uri = "https://benchmark.invalid/stable/registry/$index",
                )
            }
        return staticFeatures + indexedFeatures
    }
}
