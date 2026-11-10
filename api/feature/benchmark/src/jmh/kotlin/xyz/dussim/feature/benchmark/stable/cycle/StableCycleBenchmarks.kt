package xyz.dussim.feature.benchmark.stable.cycle

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import xyz.dussim.viessmann.feature.api.Feature
import java.util.concurrent.TimeUnit

/**
 * Decodes [response] and runs a cycle over it once, failing unless the response has the step's expected number of
 * features and the cycle resolves exactly what the step expects. A cycle that resolves something else is a broken
 * benchmark, not a valid measurement.
 */
private fun verifiedFeatures(
    step: StableCycleStep,
    response: String,
): List<Feature> {
    val features = StableCycleFixture.decode(response)
    check(features.size == step.expectedFeatureCount) {
        "Stable cycle step '${step.id}' decoded ${features.size} features, expected ${step.expectedFeatureCount}"
    }
    val outcome = StableCycleFixture.consume(features)
    check(outcome == step.expected) { "Stable cycle step '${step.id}' resolved $outcome, expected ${step.expected}" }
    return features
}

/** One response of the stable cycle, selected by [step], as text and already decoded. */
@State(Scope.Benchmark)
open class StableCycleStepState {
    @Param(
        "base",
        "values-changed",
        "features-added",
        "features-removed",
        "features-disabled",
        "features-enabled",
        "structure-changed",
    )
    var step: String = "base"

    lateinit var response: String
    lateinit var features: List<Feature>

    @Setup
    fun setup() {
        val cycleStep = StableCycleStep.fromId(step)
        response = StableCycleFixture.responses().getValue(cycleStep)
        features = verifiedFeatures(cycleStep, response)
    }
}

/** The base response of the stable cycle, as text and already decoded. */
@State(Scope.Benchmark)
open class StableCycleBaseState {
    lateinit var response: String
    lateinit var features: List<Feature>

    @Setup
    fun setup() {
        response = StableCycleFixture.responses().getValue(StableCycleStep.BASE)
        features = verifiedFeatures(StableCycleStep.BASE, response)
    }
}

/** Every response of the stable cycle, in step order. */
@State(Scope.Benchmark)
open class StableCycleSequenceState {
    lateinit var responses: List<String>

    @Setup
    fun setup() {
        val byStep = StableCycleFixture.responses()
        responses =
            StableCycleStep.entries.map { step ->
                val response = byStep.getValue(step)
                verifiedFeatures(step, response)
                response
            }
    }
}

/**
 * Handling one response:
 * - `fullCycle`: response text to features, then a new registry and the typed lookups of one cycle, as a consumer
 *   runs them for every polled response; measured per kind of response;
 * - `decode`: response text to features;
 * - `lookup`: already decoded features to a new registry and the typed lookups of one cycle.
 *
 * `decode` and `lookup` split `fullCycle` to show where the time goes. They run on the base response only: the
 * split barely differs between kinds of response, and measuring it for each tripled the suite's run time.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableCycleStageBenchmark {
    @Benchmark
    fun decode(
        state: StableCycleBaseState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(StableCycleFixture.decode(state.response))
    }

    @Benchmark
    fun lookup(
        state: StableCycleBaseState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(StableCycleFixture.consume(state.features))
    }

    @Benchmark
    fun fullCycle(
        state: StableCycleStepState,
        blackhole: Blackhole,
    ) {
        blackhole.consume(StableCycleFixture.consume(StableCycleFixture.decode(state.response)))
    }
}

/**
 * A device polled through every step in order: one invocation decodes and consumes all seven responses, the way a
 * long-running consumer sees a device change over time.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableCyclePollSequenceBenchmark {
    @Benchmark
    fun pollSequence(
        state: StableCycleSequenceState,
        blackhole: Blackhole,
    ) {
        val responses = state.responses
        for (i in responses.indices) {
            blackhole.consume(StableCycleFixture.consume(StableCycleFixture.decode(responses[i])))
        }
    }
}
