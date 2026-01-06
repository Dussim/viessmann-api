package xyz.dussim.viessmann.api.features

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.ParseSpec
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.FeatureResolver
import kotlin.time.Clock

class LongTest :
    ParseSpec({ json ->
        val featureFactoriesWithExpectedCounts =
            listOf(
                DeviceEtnFeature.descriptor to 936,
                DeviceSerialFeature.descriptor to 936,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 156,
                HeatingBoilerPumpsInternalFeature.descriptor to 1794,
                HeatingBoilerPumpsInternalTargetFeature.descriptor to 1248,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 156,
                HeatingBoilerSerialFeature.descriptor to 936,
                HeatingBoilerTemperatureFeature.descriptor to 1248,
                HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor to 1794,
                DeviceZigbeeCoordinatorFeature.descriptor to 0,
                RoomsFeature.descriptor to 0,
                RoomsOthersNFeature.descriptor to 0,
                DeviceTimezoneFeature.descriptor to 0,
                TcuModeFeature.descriptor to 78,
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 390,
                DeviceConfigurationFeature.descriptor to 78,
            )

        fun loadFeatures() =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/very_long.json"),
                ).data
                .let(::FeatureResolver)

        context("Deserializes 10000+ features").config(enabled = false) {
            val features = loadFeatures()

            features.size shouldBe 11544
        }

        context("Finds and decorates all DeviceFeature's via validation").config(enabled = false) {
            val features = loadFeatures()

            featureFactoriesWithExpectedCounts.forEach { (descriptor, expected) ->
                val result = features.allOf(descriptor, descriptor.byStructure)

                result shouldHaveSize expected
            }
        }

        context("Finds and decorates all DeviceFeature's via name").config(enabled = false) {
            val features = loadFeatures()

            repeat(5096) {
                listOf(
                    DeviceEtnFeature.descriptor to 78,
                    DeviceSerialFeature.descriptor to 78,
                    DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                    DeviceZigbeeActiveFeature.descriptor to 78,
                    HeatingBoilerPumpsInternalFeature.descriptor to 78,
                    HeatingBoilerPumpsInternalTargetFeature.descriptor to 78,
                    HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 78,
                    HeatingBoilerSerialFeature.descriptor to 78,
                    HeatingBoilerTemperatureFeature.descriptor to 78,
                    HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor to 78,
                    DeviceZigbeeCoordinatorFeature.descriptor to 0,
                    RoomsFeature.descriptor to 0,
                    RoomsOthersNFeature.descriptor to 0,
                    DeviceTimezoneFeature.descriptor to 0,
                    TcuModeFeature.descriptor to 0,
                    HeatingCircuitsNHeatingScheduleFeature.descriptor to 78,
                    DeviceConfigurationFeature.descriptor to 78,
                ).forEach { (descriptor, expected) ->
                    val result = features.allOf(descriptor, descriptor.byWildcardName)

                    result shouldHaveSize expected
                }
            }
        }

        context("Finds and decorates all DeviceFeature's via validation, long test").config(enabled = true) {
            val features = loadFeatures()
            val startTime = Clock.System.now()
            repeat(5096) { repeatIndex ->
                featureFactoriesWithExpectedCounts.forEach { (descriptor, expected) ->
                    val result = features.allOf(descriptor, descriptor.byStructure)
                    try {
                        result shouldHaveSize expected
                    } catch (e: AssertionError) {
                        throw AssertionError("Failed for $descriptor", e)
                    }
                }
                if (repeatIndex % 100 == 0) {
                    println("Finished $repeatIndex/5096")
                }
            }
            println("Finished in ${Clock.System.now() - startTime}")
        }
    })
