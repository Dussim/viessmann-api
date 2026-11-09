package xyz.dussim.feature.benchmark.featurevalidation.average

import kotlinx.serialization.json.Json
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
import xyz.dussim.viessmann.api.features.generated.Descriptors
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.FeatureRegistry
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * kotlinx-benchmark baseline for "validate every real feature through every real descriptor".
 *
 * Per invocation the benchmark body performs
 * `benchmarkDescriptors.size * allFeatures.size` validations (≈ 92 000 at time of
 * writing), so the average-time numbers land in milliseconds. The timing profile
 * (1 warmup + 3 measurement iterations × 2s each, single fork) is intentionally
 * aggressive — the goal is a fast, stable baseline to iterate against.
 *
 * The `registry*` benchmarks reuse the same dataset for typed lookups through one warm indexed
 * `FeatureRegistry` holding every feature; per invocation they perform one lookup per resolvable descriptor.
 *
 * To run just this benchmark:
 *   ./gradlew :api:feature:benchmark:jmh
 *
 * Resource loading expects `all_features.json` on the benchmark runtime classpath.
 * That file is produced by `generateFeatureJsonsFromYaml` into
 * `build/generated/feature-jsons/all_features.json` and is wired to the
 * jvmBenchmarks source set's resource directory in `build.gradle.kts`.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MINUTES)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class MassiveValidationBenchmark {
    private lateinit var targets: List<ValidationTarget>
    private lateinit var failFastTargets: List<ValidationTarget>

    private lateinit var registry: FeatureRegistry
    private lateinit var registryDescriptors: List<FeatureDescriptor<Feature>>
    private lateinit var registryIndexedDescriptors: List<FeatureDescriptor.Indexed<Feature>>

    // Every indexed feature in all_features.json uses index 0. A field rather than a constant so the JIT cannot
    // fold the index into the lookup.
    private var registryIndex: Int = 0

    @Setup
    fun setup() {
        val json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
                useAlternativeNames = false
            }

        val resource =
            this::class.java.classLoader.getResource("all_features.json")
                ?: error(
                    "all_features.json not found on benchmark classpath. Make sure it is present in resources directory.",
                )

        val wrapper = json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), resource.readText())
        val allFeatures: List<Feature> = wrapper.data

        targets =
            Descriptors.all.map { descriptor ->
                ValidationTarget(
                    validator = descriptor.structureValidator,
                    features = allFeatures,
                )
            }

        failFastTargets =
            Descriptors.all.map { descriptor ->
                ValidationTarget(
                    validator = descriptor.failFastStructureValidator,
                    features = allFeatures,
                )
            }

        setupRegistry(allFeatures)
    }

    /**
     * Keeps only descriptors whose lookup resolves, so the measured loops never throw, and warms the registry's
     * conversion cache for them. Which descriptors resolve depends only on `all_features.json`.
     */
    @Suppress("UNCHECKED_CAST")
    private fun setupRegistry(allFeatures: List<Feature>) {
        registry = FeatureRegistry.indexed(allFeatures)

        val descriptors = Descriptors.all.map { it as FeatureDescriptor<Feature> }
        registryDescriptors =
            descriptors.filter { descriptor ->
                registry.findOf(descriptor, descriptor.byWildcardNameThenStructure) != null
            }

        registryIndexedDescriptors =
            descriptors
                .mapNotNull { it as? FeatureDescriptor.Indexed<Feature> }
                .filter { descriptor ->
                    registry.findOf(descriptor, descriptor.byNameThenStructure(registryIndex)) != null
                }

        check(registryDescriptors.isNotEmpty()) { "No descriptor resolves against all_features.json through the registry" }
        check(registryIndexedDescriptors.isNotEmpty()) {
            "No indexed descriptor resolves at index $registryIndex against all_features.json through the registry"
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    @Benchmark
    fun validateEverything(blackhole: Blackhole) {
        for (i in this.targets.indices) {
            val target = this.targets[i]
            val validator = target.validator
            val features = target.features
            for (j in features.indices) {
                blackhole.consume(validator.validate(features[j]))
            }
        }
    }

    @Benchmark
    fun failFastValidateEverything(blackhole: Blackhole) {
        for (i in failFastTargets.indices) {
            val target = failFastTargets[i]
            val validator = target.validator
            val features = target.features
            for (j in features.indices) {
                blackhole.consume(validator.validate(features[j]))
            }
        }
    }

    /**
     * Typed lookup of every resolvable descriptor on a warm indexed registry over all features: the wildcard-name
     * path behind `registry[descriptor]`.
     */
    @Benchmark
    fun registryGetEverything(blackhole: Blackhole) {
        val registry = this.registry
        val descriptors = this.registryDescriptors
        for (i in descriptors.indices) {
            val descriptor = descriptors[i]
            blackhole.consume(registry.firstOf(descriptor, descriptor.byWildcardNameThenStructure))
        }
    }

    /**
     * Typed lookup of every resolvable indexed descriptor by index on the same registry: the exact-name path behind
     * `registry[descriptor, index]`, including building the indexed matcher on every call.
     */
    @Benchmark
    fun registryGetEverythingByIndex(blackhole: Blackhole) {
        val registry = this.registry
        val descriptors = this.registryIndexedDescriptors
        val index = this.registryIndex
        for (i in descriptors.indices) {
            blackhole.consume(registry[descriptors[i], index])
        }
    }
}
