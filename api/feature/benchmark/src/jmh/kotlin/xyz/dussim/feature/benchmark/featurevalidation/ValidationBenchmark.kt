package xyz.dussim.feature.benchmark.featurevalidation

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
import xyz.dussim.feature.benchmark.DeviceTimeseriesMonitoringIonizationFeature
import xyz.dussim.feature.benchmark.descriptor
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.invoke
import java.util.concurrent.TimeUnit

private const val CORRECT_FEATURE = """
{
  "feature": "device.timeseries.monitoringIonization",
  "gatewayId": "7724827000137183",
  "deviceId": "0",
  "timestamp": "2025-03-24T11:52:51.685Z",
  "isEnabled": true,
  "isReady": true,
  "apiVersion": 1,
  "uri": "https://api.viessmann.com/iot/v2/features/installations/2886465/gateways/7724827000137183/devices/0/features/device.timeseries.monitoringIonization",
  "properties": {
    "countOne": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampOne": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countTwo": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampTwo": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countThree": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampThree": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countFour": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampFour": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countFive": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampFive": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countSix": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampSix": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    },
    "countSeven": {
      "type": "number",
      "value": 0,
      "unit": ""
    },
    "timestampSeven": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z"
    }
  },
  "commands": {}
}
"""

private const val ALL_TYPES_INCORRECT_FEATURE = """
{
  "feature": "device.timeseries.monitoringIonization",
  "gatewayId": "7724827000137183",
  "deviceId": "0",
  "timestamp": "2025-03-24T11:52:51.685Z",
  "isEnabled": true,
  "isReady": true,
  "apiVersion": 1,
  "uri": "https://api.viessmann.com/iot/v2/features/installations/2886465/gateways/7724827000137183/devices/0/features/device.timeseries.monitoringIonization",
  "properties": {
    "countOne": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampOne": {
      "type": "number",
      "value": 0
    },
    "countTwo": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampTwo": {
      "type": "number",
      "value": 0
    },
    "countThree": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampThree": {
      "type": "number",
      "value": 0
    },
    "countFour": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampFour": {
      "type": "number",
      "value": 0
    },
    "countFive": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampFive": {
      "type": "number",
      "value": 0
    },
    "countSix": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampSix": {
      "type": "number",
      "value": 0
    },
    "countSeven": {
      "type": "string",
      "value": "1970-01-01T00:00:00.000Z",
      "unit": ""
    },
    "timestampSeven": {
      "type": "number",
      "value": 0
    }
  },
  "commands": {}
}  
"""

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class ValidationBenchmark {
    lateinit var factory: FeatureFactory<DeviceTimeseriesMonitoringIonizationFeature>
    lateinit var validator: ValidationRule<Feature, ValidationError>
    lateinit var validatorFailFast: ValidationRule<Feature, ValidationError>
    lateinit var feature: Feature
    lateinit var incorrectFeature: Feature

    @Setup
    fun setup() {
        val json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }

        factory = DeviceTimeseriesMonitoringIonizationFeature.descriptor
        validator = DeviceTimeseriesMonitoringIonizationFeature.descriptor.structureValidator
        validatorFailFast = DeviceTimeseriesMonitoringIonizationFeature.descriptor.failFastStructureValidator
        feature =
            json.decodeFromString(
                ViessmannFeature.serializer(),
                CORRECT_FEATURE,
            )
        incorrectFeature =
            json.decodeFromString(
                ViessmannFeature.serializer(),
                ALL_TYPES_INCORRECT_FEATURE,
            )
    }

    @Benchmark
    fun validateFeature(blackHole: Blackhole) {
        blackHole.consume(validator(feature))
    }

    @Benchmark
    fun validateFeatureFailFast(blackHole: Blackhole) {
        blackHole.consume(validatorFailFast(feature))
    }

    @Benchmark
    fun constructFeature(blackHole: Blackhole) {
        blackHole.consume(factory(feature))
    }

    @Benchmark
    fun validateIncorrectFeature(blackHole: Blackhole) {
        blackHole.consume(validator(incorrectFeature))
    }

    @Benchmark
    fun validateIncorrectFeatureFailFast(blackHole: Blackhole) {
        blackHole.consume(validatorFailFast(incorrectFeature))
    }
}
