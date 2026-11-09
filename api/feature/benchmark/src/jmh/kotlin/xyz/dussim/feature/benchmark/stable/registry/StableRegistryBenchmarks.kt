package xyz.dussim.feature.benchmark.stable.registry

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureMatcher
import xyz.dussim.viessmann.feature.api.FeatureRegistry
import java.util.concurrent.TimeUnit

/**
 * Indexed registry over [StableRegistryFixture.features] with the typed-conversion cache already warm for every
 * measured lookup. Setup fails when a lookup does not resolve to the expected feature: a lookup that throws or
 * misses is a broken benchmark, not a valid measurement.
 */
@State(Scope.Benchmark)
open class StableRegistryState {
    val staticDescriptor = StableRegistryFixture.staticDescriptor
    val indexedDescriptor = StableRegistryFixture.indexedDescriptor

    // A field rather than a constant so the JIT cannot fold the index into the lookup.
    var index: Int = StableRegistryFixture.LOOKUP_INDEX

    lateinit var features: List<Feature>
    lateinit var registry: FeatureRegistry
    lateinit var prebuiltIndexedMatcher: FeatureMatcher

    @Setup
    fun setup() {
        features = StableRegistryFixture.features()
        registry = FeatureRegistry.indexed(features)
        prebuiltIndexedMatcher = indexedDescriptor.byNameThenStructure(index)

        // Verifies the fixture and warms the per-feature conversion cache used by the measured lookups.
        expectFeature(StableRegistryFixture.STATIC_FEATURE_NAME, registry[staticDescriptor])
        expectFeature(StableRegistryFixture.FIRST_INDEXED_FEATURE_NAME, registry[indexedDescriptor])
        expectFeature(StableRegistryFixture.INDEXED_LOOKUP_FEATURE_NAME, registry[indexedDescriptor, index])
        expectFeature(StableRegistryFixture.INDEXED_LOOKUP_FEATURE_NAME, registry.firstOf(indexedDescriptor, prebuiltIndexedMatcher))
    }

    private fun expectFeature(
        expectedName: String,
        actual: Feature,
    ) {
        check(actual.feature == expectedName) { "Expected registry lookup to return '$expectedName' but got '${actual.feature}'" }
    }
}

/**
 * Repeated typed lookups on a warm indexed registry: the path a consumer hits every time it reads a feature.
 *
 * The static and indexed features have identical structure, so read the results as differences:
 * - `indexedGet` minus `indexedGetWithPrebuiltMatcher` is the per-call cost of building the indexed matcher
 *   (also measured alone by `indexedMatcher`);
 * - `indexedGetWithPrebuiltMatcher` against `staticGet` compares the exact-name and wildcard-name jump tables;
 * - `gc.alloc.rate.norm` from the GC profiler gives the bytes allocated per lookup.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableRegistryLookupBenchmark {
    @Benchmark
    fun staticGet(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(state.registry[state.staticDescriptor])
    }

    @Benchmark
    fun indexedWildcardGet(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(state.registry[state.indexedDescriptor])
    }

    @Benchmark
    fun indexedGet(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(state.registry[state.indexedDescriptor, state.index])
    }

    @Benchmark
    fun indexedGetWithPrebuiltMatcher(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(state.registry.firstOf(state.indexedDescriptor, state.prebuiltIndexedMatcher))
    }

    @Benchmark
    fun indexedMatcher(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(state.indexedDescriptor.byNameThenStructure(state.index))
    }
}

/**
 * Construction of an indexed registry over the stable registry fixture: sorting, both jump tables and the
 * per-feature conversion caches. Paid once per API response before any lookup.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableRegistryConstructionBenchmark {
    @Benchmark
    fun buildIndexed(
        state: StableRegistryState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(FeatureRegistry.indexed(state.features))
    }
}
