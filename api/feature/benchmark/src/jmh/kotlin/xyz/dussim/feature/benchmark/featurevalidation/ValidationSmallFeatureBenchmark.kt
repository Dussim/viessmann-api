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
import xyz.dussim.feature.benchmark.DeviceZigbeeCoordinatorFeature
import xyz.dussim.feature.benchmark.descriptor
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import xyz.dussim.viessmann.feature.api.validation.invoke
import java.util.concurrent.TimeUnit

private const val CORRECT_FEATURE = """
    {
      "feature": "device.zigbee.coordinator",
      "gatewayId": "7637415001735184",
      "deviceId": "RoomControl-1",
      "timestamp": "2025-03-28T18:10:42.176Z",
      "isEnabled": true,
      "isReady": true,
      "apiVersion": 1,
      "uri": "https://api-integration.viessmann.com/iot/v2/features/installations/34772/gateways/7637415001735184/devices/RoomControl-1/features/device.zigbee.coordinator",
      "properties": {
        "status": {
          "type": "array",
          "value": [
            {
              "device": "zigbee-90fd9ffffe4a5d41",
              "value": "OK"
            }
          ]
        },
        "timeout": {
          "type": "number",
          "value": 120,
          "unit": ""
        }
      },
      "commands": {
        "addDevice": {
          "uri": "https://api-integration.viessmann.com/iot/v2/features/installations/34772/gateways/7637415001735184/devices/RoomControl-1/features/device.zigbee.coordinator/commands/addDevice",
          "name": "addDevice",
          "isExecutable": true,
          "params": {
            "id": {
              "type": "string",
              "required": true,
              "constraints": {
                "minLength": 1,
                "maxLength": 16
              }
            },
            "ic": {
              "type": "string",
              "required": true,
              "constraints": {
                "minLength": 1,
                "maxLength": 36
              }
            },
            "type": {
              "type": "string",
              "required": true,
              "constraints": {
                "enum": [
                  "cs",
                  "fht",
                  "repeater",
                  "trv",
                  "airQualitySensor"
                ]
              }
            }
          }
        },
        "removeDevice": {
          "uri": "https://api-integration.viessmann.com/iot/v2/features/installations/34772/gateways/7637415001735184/devices/RoomControl-1/features/device.zigbee.coordinator/commands/removeDevice",
          "name": "removeDevice",
          "isExecutable": true,
          "params": {
            "id": {
              "type": "string",
              "required": true,
              "constraints": {
                "minLength": 1,
                "maxLength": 16
              }
            }
          }
        }
      }
    }
"""

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class ValidationSmallFeatureBenchmark {
    lateinit var factory: FeatureFactory<DeviceZigbeeCoordinatorFeature>
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
        factory = DeviceZigbeeCoordinatorFeature.descriptor
        validator = DeviceZigbeeCoordinatorFeature.descriptor.structureValidator
        validatorFailFast = DeviceZigbeeCoordinatorFeature.descriptor.failFastStructureValidator
        feature =
            json.decodeFromString(
                DeviceFeature.serializer(),
                CORRECT_FEATURE,
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
    fun constructFeature() = factory(feature)
}
