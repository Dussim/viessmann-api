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

enum class Implementation {
    OLD,
    CURRENT,
}

enum class Scenario {
    ALL_VALID,
    ONE_INVALID,
    FIRST_INVALID,
    LAST_INVALID,
    TWO_INVALID,
    ALL_INVALID,
    ONE_INVALID_MULTIPLE_ERRORS,
    MIXED_ONE_AND_MULTIPLE_ERRORS,
}

typealias OfFunction<E> = (
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
) -> ValidationResult<E>

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
open class ValidationResultOfSevenBenchmark {
    @Param(
        "OLD",
        "CURRENT",
    )
    lateinit var implementation: Implementation

    @Param(
        "ALL_VALID",
        "ONE_INVALID",
        "FIRST_INVALID",
        "LAST_INVALID",
        "TWO_INVALID",
        "ALL_INVALID",
        "ONE_INVALID_MULTIPLE_ERRORS",
        "MIXED_ONE_AND_MULTIPLE_ERRORS",
    )
    lateinit var scenario: Scenario

    private val valid: ValidationResult<String> = ValidationResult.Valid
    private val invalid1: ValidationResult<String> = Invalid("error1")
    private val invalid2: ValidationResult<String> = Invalid("error2")
    private val invalid3: ValidationResult<String> = Invalid("error3")
    private val invalid4: ValidationResult<String> = Invalid("error4")
    private val invalid5: ValidationResult<String> = Invalid("error5")
    private val invalid6: ValidationResult<String> = Invalid("error6")
    private val invalid7: ValidationResult<String> = Invalid("error7")
    private val invalidMulti: ValidationResult<String> = Invalid(arrayOf("errorA", "errorB", "errorC"))

    private var ofFunction: OfFunction<String> = { _, _, _, _, _, _, _ -> ValidationResult.Valid }

    private var r1: ValidationResult<String> = valid
    private var r2: ValidationResult<String> = valid
    private var r3: ValidationResult<String> = valid
    private var r4: ValidationResult<String> = valid
    private var r5: ValidationResult<String> = valid
    private var r6: ValidationResult<String> = valid
    private var r7: ValidationResult<String> = valid

    @Setup
    fun setup() {
        ofFunction =
            when (implementation) {
                Implementation.OLD -> ::ofOldSeven
                Implementation.CURRENT -> ::ofCurrentSeven
            }

        when (scenario) {
            Scenario.ALL_VALID -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
            }

            Scenario.ONE_INVALID -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = invalid4
                r5 = valid
                r6 = valid
                r7 = valid
            }

            Scenario.FIRST_INVALID -> {
                r1 = invalid1
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
            }

            Scenario.LAST_INVALID -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = invalid7
            }

            Scenario.TWO_INVALID -> {
                r1 = valid
                r2 = invalid2
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = invalid6
                r7 = valid
            }

            Scenario.ALL_INVALID -> {
                r1 = invalid1
                r2 = invalid2
                r3 = invalid3
                r4 = invalid4
                r5 = invalid5
                r6 = invalid6
                r7 = invalid7
            }

            Scenario.ONE_INVALID_MULTIPLE_ERRORS -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = invalidMulti
                r5 = valid
                r6 = valid
                r7 = valid
            }

            Scenario.MIXED_ONE_AND_MULTIPLE_ERRORS -> {
                r1 = invalidMulti
                r2 = invalid1
                r3 = valid
                r4 = invalid2
                r5 = invalidMulti
                r6 = valid
                r7 = invalid3
            }
        }
    }

    @Benchmark
    fun benchmark(blackHole: Blackhole) {
        blackHole.consume(ofFunction(r1, r2, r3, r4, r5, r6, r7))
    }
}
