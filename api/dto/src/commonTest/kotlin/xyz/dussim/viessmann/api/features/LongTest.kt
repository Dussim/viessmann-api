package xyz.dussim.viessmann.api.features

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.ParseSpec
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.FeatureResolver

class LongTest :
    ParseSpec({ json ->
        val featureFactoriesWithExpectedCounts =
            listOf(
                DeviceEtnFeature.utils to 936,
                DeviceSerialFeature.utils to 936,
                DeviceTimeseriesMonitoringIonizationFeature.utils to 78,
                DeviceZigbeeActiveFeature.utils to 156,
                HeatingBoilerPumpsInternalFeature.utils to 1794,
                HeatingBoilerPumpsInternalTargetFeature.utils to 1248,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.utils to 1794,
                HeatingBoilerSerialFeature.utils to 936,
                HeatingBoilerTemperatureFeature.utils to 1248,
                HeatingBufferCylinderSensorsTemperatureMainFeature.utils to 1794,
                DeviceZigbeeCoordinatorFeature.utils to 0,
                RoomsFeature.utils to 0,
                RoomsOthersFeature.utils(0) to 0,
                DeviceTimezoneFeature.utils to 0,
                TcuModeFeature.utils to 78,
                HeatingCircuitsNHeatingScheduleFeature.utils(0) to 390,
                DeviceConfigurationFeature.utils to 78,
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

            featureFactoriesWithExpectedCounts.forEach { (utils, expected) ->
                val (factory, matchers, _) = utils
                val result = features.allOf(factory, matchers.byValidation)

                result shouldHaveSize expected
            }
        }

        context("Finds and decorates all DeviceFeature's via name").config(enabled = false) {
            val features = loadFeatures()

            repeat(5096) {
                listOf(
                    DeviceEtnFeature.utils to 78,
                    DeviceSerialFeature.utils to 78,
                    DeviceTimeseriesMonitoringIonizationFeature.utils to 78,
                    DeviceZigbeeActiveFeature.utils to 78,
                    HeatingBoilerPumpsInternalFeature.utils to 78,
                    HeatingBoilerPumpsInternalTargetFeature.utils to 78,
                    HeatingBoilerSensorsTemperatureCommonSupplyFeature.utils to 78,
                    HeatingBoilerSerialFeature.utils to 78,
                    HeatingBoilerTemperatureFeature.utils to 78,
                    HeatingBufferCylinderSensorsTemperatureMainFeature.utils to 78,
                    DeviceZigbeeCoordinatorFeature.utils to 0,
                    RoomsFeature.utils to 0,
                    RoomsOthersFeature.utils(0) to 0,
                    DeviceTimezoneFeature.utils to 0,
                    TcuModeFeature.utils to 0,
                    HeatingCircuitsNHeatingScheduleFeature.utils(0) to 78,
                    DeviceConfigurationFeature.utils to 78,
                ).forEach { (utils, expected) ->
                    val (factory, matchers, _) = utils
                    val result = features.allOf(factory, matchers.byName)

                    result shouldHaveSize expected
                }
            }
        }

        context("Finds and decorates all HeatingCircuitsNHeatingScheduleFeature's via validation").config(enabled = false) {
            val features = loadFeatures()

            repeat(10000) {
                val factory = HeatingCircuitsNHeatingScheduleFeature.factory
                val result = features.allOf(factory, HeatingCircuitsNHeatingScheduleFeature.matchers(0).byValidation)

                result shouldHaveSize 390
            }
        }

        context("Finds and decorates all DeviceFeature's via validation, long test") {
            val features = loadFeatures()

            repeat(5096) {
                featureFactoriesWithExpectedCounts.forEach { (utils, expected) ->
                    val (factory, matchers, _) = utils
                    val result = features.allOf(factory, matchers.byValidation)

                    try {
                        result shouldHaveSize expected
                    } catch (e: AssertionError) {
                        println("Failed for $factory")
                        throw e
                    }
                }
            }
        }
    })
