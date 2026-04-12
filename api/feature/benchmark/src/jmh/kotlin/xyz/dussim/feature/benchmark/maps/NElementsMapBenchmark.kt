package xyz.dussim.feature.benchmark.maps

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
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import java.util.concurrent.TimeUnit

/**
 * Benchmarks for NElements implementation (6 entries, keys < 64 chars).
 * Keys "bb" and "cc" share the same length (2), exercising the do-while loop.
 *
 * Miss variants:
 * - [efficientMapLookupMiss]: "xx" has length 2 (IN bitset) — tests full loop scan
 * - [efficientMapLookupMissEarlyExit]: "xxxxxxx" has length 7 (NOT in map) — tests bitset short-circuit
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class NElementsMapBenchmark {
    companion object {
        // First entry in length-2 bucket
        val bbCombined = combineToLong("bb".hashCode(), "bb".length)

        // Second entry in length-2 bucket (worst-case hit: must scan past "bb")
        val ccCombined = combineToLong("cc".hashCode(), "cc".length)

        // Length 2, NOT in map — bitset passes, full loop scan
        val xxCombined = combineToLong("xx".hashCode(), "xx".length)

        // Length 7, NOT in map — bitset short-circuits immediately
        val xxxxxxxCombined = combineToLong("xxxxxxx".hashCode(), "xxxxxxx".length)
    }

    private lateinit var standardMap: Map<String, String>
    private lateinit var efficientMap: EfficientStringKeyMap<String>

    @Setup
    fun setup() {
        val data =
            mapOf(
                "a" to "v1", // length 1
                "bb" to "v2", // length 2, first in bucket
                "cc" to "v3", // length 2, second in bucket
                "ddd" to "v4", // length 3
                "eeee" to "v5", // length 4
                "fffff" to "v6", // length 5
            )
        standardMap = data
        efficientMap = EfficientStringKeyMap(data)
    }

    @Benchmark
    fun standardMapLookupHit(): String? = standardMap["bb"]

    @Benchmark
    fun efficientMapLookupHit(): String? = efficientMap["bb", bbCombined]

    @Benchmark
    fun efficientMapLookupHitLast(): String? = efficientMap["cc", ccCombined]

    @Benchmark
    fun standardMapLookupMiss(): String? = standardMap["xx"]

    @Benchmark
    fun efficientMapLookupMiss(): String? = efficientMap["xx", xxCombined]

    @Benchmark
    fun efficientMapLookupMissEarlyExit(): String? = efficientMap["xxxxxxx", xxxxxxxCombined]
}
