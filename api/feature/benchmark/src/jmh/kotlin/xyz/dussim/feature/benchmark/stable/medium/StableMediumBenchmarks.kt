package xyz.dussim.feature.benchmark.stable.medium

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
open class SuiteState : StableValidationState(StableSize.MEDIUM)

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@Suppress("unused")
open class StableMediumValidationBenchmark {
    @Benchmark
    fun valid(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.valid
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidFirst(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun invalidMiddle(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidMiddle
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
    fun missingCommand(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.missingCommand
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

    @Benchmark
    fun wrongParamConstraintType(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.wrongParamConstraintType
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun optionalCommandAbsent(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.optionalCommandAbsent
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }

    @Benchmark
    fun optionalCommandMalformed(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.optionalCommandMalformed
        blackhole.consume(fixture.descriptor.structureValidator.validate(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@Suppress("unused")
open class StableMediumFailFastValidationBenchmark {
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
@Fork(value = 3)
@Suppress("unused")
open class StableMediumConstructionBenchmark {
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
@Fork(value = 3)
@Suppress("unused")
open class StableMediumNonThrowingRejectionBenchmark {
    @Benchmark
    fun invalidFirstOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.invalidFirst
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun missingPropertyOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.missingProperty
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun missingCommandOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.missingCommand
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun wrongParamConstraintTypeOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.wrongParamConstraintType
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }

    @Benchmark
    fun optionalCommandMalformedOrNull(
        state: SuiteState,
        blackhole: Blackhole,
    ) {
        val fixture = state.optionalCommandMalformed
        blackhole.consume(fixture.descriptor.getOrNull(fixture.feature))
    }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3)
@Suppress("unused")
open class StableMediumThrowingConstructionBenchmark {
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
