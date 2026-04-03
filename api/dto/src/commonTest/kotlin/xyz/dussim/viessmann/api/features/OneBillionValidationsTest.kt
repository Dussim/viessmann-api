package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import xyz.dussim.viessmann.api.features.generated.DeviceConfigurationFeature
import xyz.dussim.viessmann.api.features.generated.DeviceEtnFeature
import xyz.dussim.viessmann.api.features.generated.DeviceMessagesErrorsRawFeature
import xyz.dussim.viessmann.api.features.generated.DeviceSerialFeature
import xyz.dussim.viessmann.api.features.generated.DeviceTimeseriesMonitoringIonizationFeature
import xyz.dussim.viessmann.api.features.generated.DeviceZigbeeActiveFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerPumpsInternalTargetFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerSensorsTemperatureCommonSupplyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerSerialFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBoilerTemperatureFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBufferCylinderSensorsTemperatureMainFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBurnersFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBurnersNDemandTemperatureFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBurnersNFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBurnersNModulationFeature
import xyz.dussim.viessmann.api.features.generated.HeatingBurnersNStatisticsFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNCirculationPumpFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNFrostprotectionFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNHeatingCurveFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNHeatingScheduleFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingModesActiveFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingModesHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingModesStandbyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsActiveFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsComfortFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsForcedLastFromScheduleFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsNormalFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsReducedEnergySavingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsReducedFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsScreedDryingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNOperatingProgramsStandbyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNRemoteControllerFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNSensorsTemperatureSupplyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNTemperatureFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNTemperatureLevelsFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNZoneDemandFeature
import xyz.dussim.viessmann.api.features.generated.HeatingCircuitsNZoneModeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationBufferCylinderSizeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationCentralHeatingCylinderSizeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationDhwCylinderPumpFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationDhwCylinderSizeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationHouseHeatingLoadFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationHouseOrientationFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationMultiFamilyHouseFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationPressureTotalFeature
import xyz.dussim.viessmann.api.features.generated.HeatingConfigurationRegulationFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDeviceTimeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDeviceTimeSourceFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwHygieneFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwHygieneTriggerFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwOneTimeChargeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwOperatingModesActiveFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwOperatingModesBalancedFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwOperatingModesOffFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwPumpsCirculationFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwPumpsCirculationScheduleFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwScheduleFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwScheduleModeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwSensorsTemperatureDhwCylinderFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwSensorsTemperatureOutletFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwTemperatureHygieneFeature
import xyz.dussim.viessmann.api.features.generated.HeatingDhwTemperatureMainFeature
import xyz.dussim.viessmann.api.features.generated.HeatingFlueSensorsTemperatureMainFeature
import xyz.dussim.viessmann.api.features.generated.HeatingGasConsumptionDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingGasConsumptionHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingGasConsumptionSummaryDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingGasConsumptionSummaryHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingGasConsumptionTotalFeature
import xyz.dussim.viessmann.api.features.generated.HeatingHeatProductionDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingHeatProductionHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingHeatProductionSummaryDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingHeatProductionSummaryHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingHeatProductionTotalFeature
import xyz.dussim.viessmann.api.features.generated.HeatingOperatingProgramsHolidayAtHomeFeature
import xyz.dussim.viessmann.api.features.generated.HeatingOperatingProgramsHolidayFeature
import xyz.dussim.viessmann.api.features.generated.HeatingPowerConsumptionDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingPowerConsumptionHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingPowerConsumptionSummaryDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingPowerConsumptionSummaryHeatingFeature
import xyz.dussim.viessmann.api.features.generated.HeatingPowerConsumptionTotalFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSensorsPressureSupplyFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSensorsTemperatureOutsideFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarPowerProductionFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarPumpsCircuitFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarRechargeSuppressionFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarSensorsTemperatureCollectorFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarSensorsTemperatureDhwFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarStatisticsFeature
import xyz.dussim.viessmann.api.features.generated.HeatingSolarSummaryPowerProductionFeature
import xyz.dussim.viessmann.api.features.generated.HeatingValvesDiverterHeatDhwFeature
import xyz.dussim.viessmann.api.features.generated.descriptor
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.utils.json
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.FeatureResolver
import kotlin.time.Clock

class OneBillionValidationsTest :
    FunSpec({
        fun loadFeatures() =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/very_long.json"),
                ).data
                .let(::FeatureResolver)

        val descriptors =
            listOf(
                DeviceEtnFeature.descriptor to 936,
                DeviceMessagesErrorsRawFeature.descriptor to 78,
                DeviceSerialFeature.descriptor to 936,
                DeviceTimeseriesMonitoringIonizationFeature.descriptor to 78,
                DeviceZigbeeActiveFeature.descriptor to 156,
                HeatingBoilerPumpsInternalTargetFeature.descriptor to 1326,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 1794,
                HeatingBoilerSerialFeature.descriptor to 936,
                HeatingBoilerTemperatureFeature.descriptor to 1326,
                HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor to 1794,
                HeatingBurnersFeature.descriptor to 156,
                HeatingBurnersNDemandTemperatureFeature.descriptor to 1326,
                HeatingBurnersNModulationFeature.descriptor to 1326,
                HeatingBurnersNStatisticsFeature.descriptor to 78,
                HeatingBurnersNFeature.descriptor to 4056,
                HeatingCircuitsFeature.descriptor to 156,
                HeatingCircuitsNCirculationPumpFeature.descriptor to 1794,
                HeatingCircuitsNFrostprotectionFeature.descriptor to 1794,
                HeatingCircuitsNHeatingCurveFeature.descriptor to 234,
                HeatingCircuitsNHeatingScheduleFeature.descriptor to 390,
                HeatingCircuitsNOperatingModesActiveFeature.descriptor to 390,
                HeatingCircuitsNOperatingModesHeatingFeature.descriptor to 4056,
                HeatingCircuitsNOperatingModesStandbyFeature.descriptor to 4056,
                HeatingCircuitsNOperatingProgramsComfortFeature.descriptor to 234,
                HeatingCircuitsNOperatingProgramsForcedLastFromScheduleFeature.descriptor to 390,
                HeatingCircuitsNOperatingProgramsNormalFeature.descriptor to 702,
                HeatingCircuitsNOperatingProgramsReducedFeature.descriptor to 702,
                HeatingCircuitsNOperatingProgramsReducedEnergySavingFeature.descriptor to 234,
                HeatingCircuitsNOperatingProgramsScreedDryingFeature.descriptor to 234,
                HeatingCircuitsNOperatingProgramsStandbyFeature.descriptor to 4056,
                HeatingCircuitsNRemoteControllerFeature.descriptor to 234,
                HeatingCircuitsNSensorsTemperatureSupplyFeature.descriptor to 1794,
                HeatingCircuitsNTemperatureFeature.descriptor to 1326,
                HeatingCircuitsNTemperatureLevelsFeature.descriptor to 234,
                HeatingCircuitsNZoneDemandFeature.descriptor to 234,
                HeatingCircuitsNZoneModeFeature.descriptor to 4056,
                HeatingCircuitsNFeature.descriptor to 234,
                HeatingConfigurationBufferCylinderSizeFeature.descriptor to 312,
                HeatingConfigurationCentralHeatingCylinderSizeFeature.descriptor to 312,
                HeatingConfigurationDhwCylinderPumpFeature.descriptor to 78,
                HeatingConfigurationDhwCylinderSizeFeature.descriptor to 312,
                HeatingConfigurationHouseHeatingLoadFeature.descriptor to 312,
                HeatingConfigurationHouseOrientationFeature.descriptor to 78,
                HeatingConfigurationMultiFamilyHouseFeature.descriptor to 4056,
                HeatingConfigurationPressureTotalFeature.descriptor to 78,
                HeatingDeviceTimeFeature.descriptor to 78,
                HeatingDeviceTimeSourceFeature.descriptor to 78,
                HeatingDhwFeature.descriptor to 78,
                HeatingDhwHygieneFeature.descriptor to 78,
                HeatingDhwHygieneTriggerFeature.descriptor to 78,
                HeatingDhwOneTimeChargeFeature.descriptor to 156,
                HeatingDhwOperatingModesActiveFeature.descriptor to 390,
                HeatingDhwOperatingModesBalancedFeature.descriptor to 4056,
                HeatingDhwOperatingModesOffFeature.descriptor to 4056,
                HeatingDhwPumpsCirculationFeature.descriptor to 1794,
                HeatingDhwPumpsCirculationScheduleFeature.descriptor to 390,
                HeatingDhwScheduleFeature.descriptor to 390,
                HeatingDhwScheduleModeFeature.descriptor to 390,
                HeatingDhwSensorsTemperatureDhwCylinderFeature.descriptor to 1794,
                HeatingDhwSensorsTemperatureOutletFeature.descriptor to 1794,
                HeatingDhwTemperatureHygieneFeature.descriptor to 78,
                HeatingDhwTemperatureMainFeature.descriptor to 78,
                HeatingFlueSensorsTemperatureMainFeature.descriptor to 1794,
                HeatingGasConsumptionDhwFeature.descriptor to 858,
                HeatingGasConsumptionHeatingFeature.descriptor to 858,
                HeatingGasConsumptionSummaryDhwFeature.descriptor to 546,
                HeatingGasConsumptionSummaryHeatingFeature.descriptor to 546,
                HeatingGasConsumptionTotalFeature.descriptor to 858,
                HeatingHeatProductionDhwFeature.descriptor to 858,
                HeatingHeatProductionHeatingFeature.descriptor to 858,
                HeatingHeatProductionSummaryDhwFeature.descriptor to 546,
                HeatingHeatProductionSummaryHeatingFeature.descriptor to 546,
                HeatingHeatProductionTotalFeature.descriptor to 858,
                HeatingOperatingProgramsHolidayFeature.descriptor to 156,
                HeatingOperatingProgramsHolidayAtHomeFeature.descriptor to 156,
                HeatingPowerConsumptionDhwFeature.descriptor to 858,
                HeatingPowerConsumptionHeatingFeature.descriptor to 858,
                HeatingPowerConsumptionSummaryDhwFeature.descriptor to 546,
                HeatingPowerConsumptionSummaryHeatingFeature.descriptor to 546,
                HeatingPowerConsumptionTotalFeature.descriptor to 858,
                HeatingSensorsPressureSupplyFeature.descriptor to 1794,
                HeatingSensorsTemperatureOutsideFeature.descriptor to 1794,
                HeatingSolarFeature.descriptor to 4056,
                HeatingSolarPowerProductionFeature.descriptor to 858,
                HeatingSolarPumpsCircuitFeature.descriptor to 1794,
                HeatingSolarRechargeSuppressionFeature.descriptor to 1794,
                HeatingSolarSensorsTemperatureCollectorFeature.descriptor to 1794,
                HeatingSolarSensorsTemperatureDhwFeature.descriptor to 1794,
                HeatingSolarStatisticsFeature.descriptor to 156,
                HeatingSolarSummaryPowerProductionFeature.descriptor to 546,
                HeatingValvesDiverterHeatDhwFeature.descriptor to 78,
                HeatingConfigurationRegulationFeature.descriptor to 78,
                DeviceConfigurationFeature.descriptor to 78,
                HeatingCircuitsNOperatingProgramsActiveFeature.descriptor to 936,
            )
        val features = loadFeatures()

        context("!One billion validations test") {
            val size = 930
            val step = size / 100
            val startTime = Clock.System.now()
            repeat(size) { repeatIndex ->
                descriptors.forEachIndexed { _, (descriptor, expected) ->
                    val result = features.allOf(descriptor, descriptor.byStructure)
                    try {
                        result shouldHaveSize expected
                    } catch (e: AssertionError) {
                        try {
                            features.allOf(descriptor, descriptor.byWildcardName)
                            throw AssertionError("Failed for $descriptor", e)
                        } catch (e: Exception) {
                            throw e
                        }
                    }
                }
                if (repeatIndex % step == 0) {
                    println("Finished $repeatIndex/$size")
                }
            }
            println("Finished in ${Clock.System.now() - startTime}")
        }

        context("!One billion validations fail fast test") {
            val size = 930
            val step = size / 100
            val startTime = Clock.System.now()
            repeat(size) { repeatIndex ->
                descriptors.forEachIndexed { _, (descriptor, expected) ->
                    val result = features.allOf(descriptor, descriptor.byFailFastStructure)
                    try {
                        result shouldHaveSize expected
                    } catch (e: AssertionError) {
                        throw AssertionError("Failed for $descriptor", e)
                    }
                }
                if (repeatIndex % step == 0) {
                    println("Finished $repeatIndex/$size")
                }
            }
            println("Finished in ${Clock.System.now() - startTime}")
        }
    })
