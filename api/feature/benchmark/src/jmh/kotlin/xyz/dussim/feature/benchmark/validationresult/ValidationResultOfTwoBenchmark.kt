@file:Suppress("unused")

package xyz.dussim.feature.benchmark.validationresult

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
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
import xyz.dussim.viessmann.feature.api.validation.ValidationResult.Companion.Invalid
import java.util.concurrent.TimeUnit

private typealias OfFunctionTwo<E> = (
    ValidationResult<E>,
    ValidationResult<E>,
) -> ValidationResult<E>

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
open class ValidationResultOfTwoBenchmark {
    @Param("CURRENT", "ALTERNATIVE")
    lateinit var implementation: Implementation

    @Param(
        "ALL_VALID",
        "ONE_INVALID",
        "FIRST_INVALID",
        "LAST_INVALID",
        "ALL_INVALID",
        "ONE_INVALID_MULTIPLE_ERRORS",
        "MIXED_ONE_AND_MULTIPLE_ERRORS",
    )
    lateinit var scenario: Scenario

    private val valid: ValidationResult<String> = ValidationResult.Valid
    private val invalid1: ValidationResult<String> = Invalid("error1")
    private val invalid2: ValidationResult<String> = Invalid("error2")
    private val invalidMulti: ValidationResult<String> = Invalid(arrayOf("errorA", "errorB", "errorC"))

    private var ofFunction: OfFunctionTwo<String> = { _, _ -> ValidationResult.Valid }

    private var r1: ValidationResult<String> = valid
    private var r2: ValidationResult<String> = valid

    @Setup
    fun setup() {
        ofFunction =
            when (implementation) {
                Implementation.CURRENT -> ::ofCurrentTwo
                Implementation.ALTERNATIVE -> ::ofAlternativeTwo
            }

        when (scenario) {
            Scenario.ALL_VALID -> {
                r1 = valid
                r2 = valid
            }

            Scenario.ONE_INVALID, Scenario.FIRST_INVALID -> {
                r1 = invalid1
                r2 = valid
            }

            Scenario.LAST_INVALID -> {
                r1 = valid
                r2 = invalid2
            }

            Scenario.TWO_INVALID, Scenario.ALL_INVALID -> {
                r1 = invalid1
                r2 = invalid2
            }

            Scenario.ONE_INVALID_MULTIPLE_ERRORS -> {
                r1 = invalidMulti
                r2 = valid
            }

            Scenario.MIXED_ONE_AND_MULTIPLE_ERRORS -> {
                r1 = invalidMulti
                r2 = invalid1
            }
        }
    }

    @Benchmark
    fun benchmark(blackHole: Blackhole) {
        blackHole.consume(ofFunction(r1, r2))
    }
}
