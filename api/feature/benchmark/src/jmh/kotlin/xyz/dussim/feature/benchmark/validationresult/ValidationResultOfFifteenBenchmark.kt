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

private typealias OfFunctionFifteen<E> = (
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
    ValidationResult<E>,
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
open class ValidationResultOfFifteenBenchmark {
    @Param("CURRENT", "ALTERNATIVE")
    lateinit var implementation: Implementation

    @Param("ALL_VALID", "ONE_INVALID", "FIRST_INVALID", "LAST_INVALID", "TWO_INVALID", "ALL_INVALID", "ONE_INVALID_MULTIPLE_ERRORS", "MIXED_ONE_AND_MULTIPLE_ERRORS")
    lateinit var scenario: Scenario

    private val valid: ValidationResult<String> = ValidationResult.Valid
    private val invalid1: ValidationResult<String> = Invalid("error1")
    private val invalid2: ValidationResult<String> = Invalid("error2")
    private val invalid3: ValidationResult<String> = Invalid("error3")
    private val invalid4: ValidationResult<String> = Invalid("error4")
    private val invalid5: ValidationResult<String> = Invalid("error5")
    private val invalid6: ValidationResult<String> = Invalid("error6")
    private val invalid7: ValidationResult<String> = Invalid("error7")
    private val invalid8: ValidationResult<String> = Invalid("error8")
    private val invalid9: ValidationResult<String> = Invalid("error9")
    private val invalid10: ValidationResult<String> = Invalid("error10")
    private val invalid11: ValidationResult<String> = Invalid("error11")
    private val invalid12: ValidationResult<String> = Invalid("error12")
    private val invalid13: ValidationResult<String> = Invalid("error13")
    private val invalid14: ValidationResult<String> = Invalid("error14")
    private val invalid15: ValidationResult<String> = Invalid("error15")
    private val invalidMulti: ValidationResult<String> = Invalid(arrayOf("errorA", "errorB", "errorC"))

    private var ofFunction: OfFunctionFifteen<String> = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> ValidationResult.Valid }
    private var r1: ValidationResult<String> = valid
    private var r2: ValidationResult<String> = valid
    private var r3: ValidationResult<String> = valid
    private var r4: ValidationResult<String> = valid
    private var r5: ValidationResult<String> = valid
    private var r6: ValidationResult<String> = valid
    private var r7: ValidationResult<String> = valid
    private var r8: ValidationResult<String> = valid
    private var r9: ValidationResult<String> = valid
    private var r10: ValidationResult<String> = valid
    private var r11: ValidationResult<String> = valid
    private var r12: ValidationResult<String> = valid
    private var r13: ValidationResult<String> = valid
    private var r14: ValidationResult<String> = valid
    private var r15: ValidationResult<String> = valid

    @Setup
    fun setup() {
        ofFunction =
            when (implementation) {
                Implementation.CURRENT -> ::ofCurrentFifteen
                Implementation.ALTERNATIVE -> ::ofAlternativeFifteen
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
                r8 = valid
                r9 = valid
                r10 = valid
                r11 = valid
                r12 =
                    valid
                r13 = valid
                r14 = valid
                r15 = valid
            }

            Scenario.ONE_INVALID -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
                r8 = invalid8
                r9 = valid
                r10 = valid
                r11 = valid
                r12 =
                    valid
                r13 = valid
                r14 = valid
                r15 = valid
            }

            Scenario.FIRST_INVALID -> {
                r1 = invalid1
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
                r8 = valid
                r9 = valid
                r10 = valid
                r11 = valid
                r12 =
                    valid
                r13 = valid
                r14 = valid
                r15 = valid
            }

            Scenario.LAST_INVALID -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
                r8 = valid
                r9 = valid
                r10 = valid
                r11 = valid
                r12 =
                    valid
                r13 = valid
                r14 = valid
                r15 = invalid15
            }

            Scenario.TWO_INVALID -> {
                r1 = invalid1
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
                r8 = valid
                r9 = valid
                r10 = valid
                r11 = valid
                r12 =
                    valid
                r13 = valid
                r14 = valid
                r15 = invalid15
            }

            Scenario.ALL_INVALID -> {
                r1 = invalid1
                r2 = invalid2
                r3 = invalid3
                r4 = invalid4
                r5 = invalid5
                r6 = invalid6
                r7 = invalid7
                r8 = invalid8
                r9 = invalid9
                r10 =
                    invalid10
                r11 = invalid11
                r12 = invalid12
                r13 = invalid13
                r14 = invalid14
                r15 = invalid15
            }

            Scenario.ONE_INVALID_MULTIPLE_ERRORS -> {
                r1 = valid
                r2 = valid
                r3 = valid
                r4 = valid
                r5 = valid
                r6 = valid
                r7 = valid
                r8 = invalidMulti
                r9 = valid
                r10 = valid
                r11 =
                    valid
                r12 = valid
                r13 = valid
                r14 = valid
                r15 = valid
            }

            Scenario.MIXED_ONE_AND_MULTIPLE_ERRORS -> {
                r1 = invalidMulti
                r2 = invalid1
                r3 = valid
                r4 = invalidMulti
                r5 = invalid2
                r6 = valid
                r7 = invalidMulti
                r8 = invalid3
                r9 =
                    valid
                r10 = invalidMulti
                r11 = invalid4
                r12 = valid
                r13 = invalidMulti
                r14 = invalid5
                r15 = valid
            }
        }
    }

    @Benchmark
    fun benchmark(blackHole: Blackhole) {
        blackHole.consume(ofFunction(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15))
    }
}
