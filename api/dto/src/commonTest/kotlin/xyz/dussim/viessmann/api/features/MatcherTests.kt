package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.features.generated.DeviceConfigurationFeature
import xyz.dussim.viessmann.api.features.generated.DeviceEtnFeature
import xyz.dussim.viessmann.api.features.generated.DeviceSerialFeature
import xyz.dussim.viessmann.api.features.generated.DeviceTimeseriesMonitoringIonizationFeature
import xyz.dussim.viessmann.api.features.generated.DeviceTimezoneFeature
import xyz.dussim.viessmann.api.features.generated.DeviceZigbeeActiveFeature
import xyz.dussim.viessmann.api.features.generated.DeviceZigbeeCoordinatorFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerPumpsInternalTargetFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerSensorsTemperatureCommonSupplyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerSerialFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerTemperatureFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBufferCylinderSensorsTemperatureMainFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNHeatingScheduleFeature
import xyz.dussim.viessmann.api.features.generated.RoomsFeature
import xyz.dussim.viessmann.api.features.generated.RoomsOthersNFeature
import xyz.dussim.viessmann.api.features.generated.TcuModeFeature
import xyz.dussim.viessmann.api.features.generated.descriptor
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.utils.json
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.FeatureResolver

class MatcherTests :
    FunSpec({
        val features =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/very_long.json"),
                ).data
                .let(::FeatureResolver)

        context("Deserializes 10000+ features") {
            features.size shouldBe 11544
        }

        context("Finds and decorates all DeviceFeature's via validation") {
            listOf(
                DeviceEtnFeature.descriptor to 936,
                DeviceSerialFeature.descriptor to 936,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 156,
                HeatingBoilerPumpsInternalTargetFeature.descriptor to 1326,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 1794,
                HeatingBoilerSerialFeature.descriptor to 936,
                HeatingBoilerTemperatureFeature.descriptor to 1326,
                HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor to 1794,
                DeviceZigbeeCoordinatorFeature.descriptor to 0,
                RoomsFeature.descriptor to 0,
                RoomsOthersNFeature.descriptor to 0,
                DeviceTimezoneFeature.descriptor to 0,
                TcuModeFeature.descriptor to 78,
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 390,
                DeviceConfigurationFeature.descriptor to 78,
            ).forEach { (descriptor, expected) ->
                val result = features.allOf(descriptor, descriptor.byStructure)

                result shouldHaveSize expected
            }
        }

        context("Finds and decorates all DeviceFeature's via name") {
            listOf(
                DeviceEtnFeature.descriptor to 78,
                DeviceSerialFeature.descriptor to 78,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 78,
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
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 234,
                DeviceConfigurationFeature.descriptor to 78,
            ).forEach { (descriptor, expected) ->
                val result = features.allOf(descriptor, descriptor.byWildcardName)

                result shouldHaveSize expected
            }
        }

        context("Finds and decorates all DeviceFeature's via fail fast validation") {
            listOf(
                DeviceEtnFeature.descriptor to 936,
                DeviceSerialFeature.descriptor to 936,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 156,
                HeatingBoilerPumpsInternalTargetFeature.descriptor to 1326,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 1794,
                HeatingBoilerSerialFeature.descriptor to 936,
                HeatingBoilerTemperatureFeature.descriptor to 1326,
                HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor to 1794,
                DeviceZigbeeCoordinatorFeature.descriptor to 0,
                RoomsFeature.descriptor to 0,
                RoomsOthersNFeature.descriptor to 0,
                DeviceTimezoneFeature.descriptor to 0,
                TcuModeFeature.descriptor to 78,
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 390,
                DeviceConfigurationFeature.descriptor to 78,
            ).forEach { (descriptor, expected) ->
                val result = features.allOf(descriptor, descriptor.byFailFastStructure)

                result shouldHaveSize expected
            }
        }

        context("Finds and decorates all DeviceFeature's via name then fail fast validation") {
            listOf(
                DeviceEtnFeature.descriptor to 78,
                DeviceSerialFeature.descriptor to 78,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 78,
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
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 234,
                DeviceConfigurationFeature.descriptor to 78,
            ).forEach { (descriptor, expected) ->
                val result = features.allOf(descriptor, descriptor.byWildcardNameThenFailFastStructure)

                result shouldHaveSize expected
            }
        }
    })
