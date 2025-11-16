package xyz.dussim.feature.benchmark

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

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class TwoElementMapBenchmark {
    companion object {
        val key1Combined = combineToLong("key1".hashCode(), "key1".length)
        val key2Combined = combineToLong("key2".hashCode(), "key2".length)
        val nonexistentCombined = combineToLong("nonexistent".hashCode(), "nonexistent".length)
    }

    private lateinit var standardMap: Map<String, String>
    private lateinit var efficientMap: EfficientStringKeyMap<String>

    @Setup
    fun setup() {
        val data =
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
            )
        standardMap = data
        efficientMap = EfficientStringKeyMap.createFrom(data)
    }

    @Benchmark
    fun standardMapLookupHit(): String? = standardMap["key1"]

    @Benchmark
    fun efficientMapLookupHit(): String? = efficientMap["key1", key1Combined]

    @Benchmark
    fun standardMapLookupMiss(): String? = standardMap["nonexistent"]

    @Benchmark
    fun efficientMapLookupMiss(): String? = efficientMap["nonexistent", nonexistentCombined]
}
