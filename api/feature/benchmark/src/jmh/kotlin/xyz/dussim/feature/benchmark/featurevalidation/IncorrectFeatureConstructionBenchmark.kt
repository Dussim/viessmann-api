package xyz.dussim.feature.benchmark.featurevalidation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
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
import xyz.dussim.viessmann.api.features.generated.DeviceZigbeeCoordinatorFeature
import xyz.dussim.viessmann.api.features.generated.descriptor
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureFactory
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import xyz.dussim.viessmann.feature.api.ViessmannFeature
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

private fun JsonObject.updateObject(
    key: String,
    transform: (JsonObject) -> JsonObject,
): JsonObject {
    val nested = this[key] as? JsonObject ?: error("Missing object key '$key'")
    return JsonObject(this + (key to transform(nested)))
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@Suppress("unused")
open class IncorrectFeatureConstructionBenchmark {
    lateinit var factory: FeatureFactory<DeviceZigbeeCoordinatorFeature>
    lateinit var feature: Feature
    lateinit var wrongPropertyTypeFeature: Feature
    lateinit var missingPropertyFeature: Feature
    lateinit var missingCommandFeature: Feature
    lateinit var wrongConstraintFeature: Feature

    @Setup
    fun setup() {
        val json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }

        factory = DeviceZigbeeCoordinatorFeature.descriptor
        val baseFeatureJson = json.decodeFromString(JsonObject.serializer(), CORRECT_FEATURE)

        val wrongPropertyTypeJson =
            baseFeatureJson.updateObject("properties") { properties ->
                JsonObject(
                    properties +
                        (
                            "timeout" to
                                JsonObject(
                                    mapOf(
                                        "type" to JsonPrimitive("string"),
                                        "value" to JsonPrimitive("120"),
                                    ),
                                )
                        ),
                )
            }

        val missingPropertyJson =
            baseFeatureJson.updateObject("properties") { properties ->
                JsonObject(properties - "timeout")
            }

        val missingCommandJson =
            baseFeatureJson.updateObject("commands") { commands ->
                JsonObject(commands - "removeDevice")
            }

        val wrongConstraintJson =
            baseFeatureJson.updateObject("commands") { commands ->
                commands.updateObject("addDevice") { addDevice ->
                    addDevice.updateObject("params") { params ->
                        params.updateObject("id") { id ->
                            JsonObject(id + ("constraints" to JsonPrimitive("invalid-constraint")))
                        }
                    }
                }
            }

        feature = json.decodeFromJsonElement(ViessmannFeature.serializer(), baseFeatureJson)
        wrongPropertyTypeFeature = json.decodeFromJsonElement(ViessmannFeature.serializer(), wrongPropertyTypeJson)
        missingPropertyFeature = json.decodeFromJsonElement(ViessmannFeature.serializer(), missingPropertyJson)
        missingCommandFeature = json.decodeFromJsonElement(ViessmannFeature.serializer(), missingCommandJson)
        wrongConstraintFeature = json.decodeFromJsonElement(ViessmannFeature.serializer(), wrongConstraintJson)

        ensureInvalid(wrongPropertyTypeFeature)
        ensureInvalid(missingPropertyFeature)
        ensureInvalid(missingCommandFeature)
        ensureInvalid(wrongConstraintFeature)
    }

    @Benchmark
    fun constructFeature(blackHole: Blackhole) {
        blackHole.consume(factory(feature))
    }

    @Benchmark
    fun constructFeatureOrNull(blackHole: Blackhole) {
        blackHole.consume(factory.getOrNull(feature))
    }

    @Benchmark
    fun constructFeatureWrongPropertyType(blackHole: Blackhole) {
        blackHole.consume(constructFeatureCatching(wrongPropertyTypeFeature))
    }

    @Benchmark
    fun constructFeatureWrongPropertyTypeOrNull(blackHole: Blackhole) {
        blackHole.consume(factory.getOrNull(wrongPropertyTypeFeature))
    }

    @Benchmark
    fun constructFeatureMissingProperty(blackHole: Blackhole) {
        blackHole.consume(constructFeatureCatching(missingPropertyFeature))
    }

    @Benchmark
    fun constructFeatureMissingPropertyOrNull(blackHole: Blackhole) {
        blackHole.consume(factory.getOrNull(missingPropertyFeature))
    }

    @Benchmark
    fun constructFeatureMissingCommand(blackHole: Blackhole) {
        blackHole.consume(constructFeatureCatching(missingCommandFeature))
    }

    @Benchmark
    fun constructFeatureMissingCommandOrNull(blackHole: Blackhole) {
        blackHole.consume(factory.getOrNull(missingCommandFeature))
    }

    @Benchmark
    fun constructFeatureMissingCommandNoMessage(blackHole: Blackhole) {
        val exception = constructFeatureCatching(missingCommandFeature)
        blackHole.consume(exception)
    }

    @Benchmark
    fun constructFeatureMissingCommandMessage(blackHole: Blackhole) {
        val exception = constructFeatureCatching(missingCommandFeature)
        blackHole.consume(exception?.message)
    }

    @Benchmark
    fun constructFeatureWrongConstraint(blackHole: Blackhole) {
        blackHole.consume(constructFeatureCatching(wrongConstraintFeature))
    }

    @Benchmark
    fun constructFeatureWrongConstraintOrNull(blackHole: Blackhole) {
        blackHole.consume(factory.getOrNull(wrongConstraintFeature))
    }

    private fun ensureInvalid(feature: Feature) {
        check(constructFeatureCatching(feature) is FeatureValidationException)
        check(factory.getOrNull(feature) == null)
    }

    private fun constructFeatureCatching(feature: Feature): Throwable? =
        try {
            factory(feature)
            null
        } catch (exception: FeatureValidationException) {
            exception
        }
}
