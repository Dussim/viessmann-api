package xyz.dussim.feature.benchmark.stable.small

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import xyz.dussim.feature.benchmark.stable.StableSize
import xyz.dussim.feature.benchmark.stable.StableValidationState
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
open class SuiteState : StableValidationState(StableSize.SMALL)

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableSmallValidationBenchmark {
    @Benchmark
    fun valid(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.valid
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    // Aggregate validation runs every rule, so error position does not matter here; one property
    // mismatch (invalidFirst) and one command parameter mismatch (invalidLast) cover both error sites.
    @Benchmark
    fun invalidFirst(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidLast(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidLast
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidAll(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidAll
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun missingProperty(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.missingProperty
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun missingRequiredParam(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.missingRequiredParam
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableSmallFailFastValidationBenchmark {
    @Benchmark
    fun valid(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.valid
        blackhole.consume(fixture.descriptor.failFastStructureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidFirst(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        blackhole.consume(fixture.descriptor.failFastStructureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidMiddle(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidMiddle
        blackhole.consume(fixture.descriptor.failFastStructureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidLast(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidLast
        blackhole.consume(fixture.descriptor.failFastStructureValidator.validate(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableSmallConstructionBenchmark {
    @Benchmark
    fun validOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.valid
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun optionalCommandAbsentOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.optionalCommandAbsent
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun validOrThrow(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.valid
        blackhole.consume(fixture.descriptor.getOrThrow(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableSmallNonThrowingRejectionBenchmark {
    // getOrNull rejects through the fail-fast matcher; compare with FailFast.invalidFirst for wrapper overhead.
    @Benchmark
    fun invalidFirstOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class StableSmallThrowingConstructionBenchmark {
    @Benchmark
    fun invalidFirstOrThrowCatching(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        val exception =
            try {
                fixture.descriptor.getOrThrow(fixture.feature)
                null
            } catch (exception: FeatureValidationException) {
                exception
            }
        blackhole.consume(exception)
    }
}
