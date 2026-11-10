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
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.validation.ValidationError.NumberOfParametersMismatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * kotlinx-benchmark baseline for "validate every real feature through every real descriptor".
 *
 * Per invocation the benchmark body performs
 * `benchmarkDescriptors.size * allFeatures.size` validations (≈ 92 000 at time of
 * writing), so throughput is reported per minute.
 *
 * Every iteration takes 3 seconds (3 warmup and 5 measurement iterations, single fork). It was 5 seconds until
 * November 2026; the shorter iterations cut the CI run, and scores from before the change are not comparable
 * with scores after it.
 *
 * `decodeEverything` decodes the whole dataset once per invocation.
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
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class MassiveValidationBenchmark {
    private lateinit var targets: List<ValidationTarget>
    private lateinit var failFastTargets: List<ValidationTarget>

    private lateinit var json: Json
    private lateinit var allFeaturesJson: String

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

        this.json = json
        allFeaturesJson = resource.readText()
        val wrapper = json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), allFeaturesJson)
        val allFeatures: List<Feature> = wrapper.data
        check(allFeatures.isNotEmpty()) { "all_features.json contains no features" }

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

    /** Decodes the whole of `all_features.json` from text; reading the resource is not measured. */
    @Benchmark
    fun decodeEverything(blackhole: Blackhole) {
        blackhole.consume(json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), allFeaturesJson))
    }
}
