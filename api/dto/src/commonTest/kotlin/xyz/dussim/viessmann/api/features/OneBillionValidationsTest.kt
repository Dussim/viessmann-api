package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
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
                HeatingBoilerPumpsInternalFeature.descriptor to 1794,
                HeatingBoilerPumpsInternalTargetFeature.descriptor to 1326,
                HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor to 234,
                HeatingBoilerSerialFeature.descriptor to 936,
                HeatingBoilerTemperatureFeature.descriptor to 1326,
                HeatingBufferSensorsTemperatureMainFeature.descriptor to 1794,
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
                HeatingCircuitsNOperatingProgramsNoDemandFeature.descriptor to 4056,
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
                HeatingConfigurationHouseLocationFeature.descriptor to 78,
                HeatingConfigurationHouseOrientationFeature.descriptor to 78,
                HeatingConfigurationMultiFamilyHouseFeature.descriptor to 4056,
                HeatingConfigurationPressureTotalFeature.descriptor to 78,
                HeatingDeviceTimeFeature.descriptor to 78,
                HeatingDeviceTimeOffsetFeature.descriptor to 1326,
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
                HeatingDhwSensorsTemperatureDhwCylinderFeature.descriptor to 234,
                HeatingDhwSensorsTemperatureHotWaterStorageFeature.descriptor to 234,
                HeatingDhwSensorsTemperatureOutletFeature.descriptor to 234,
                HeatingDhwTemperatureHygieneFeature.descriptor to 78,
                HeatingDhwTemperatureMainFeature.descriptor to 78,
                HeatingFlueSensorsTemperatureMainFeature.descriptor to 1794,
                HeatingGasConsumptionDhwFeature.descriptor to 858,
                HeatingGasConsumptionHeatingFeature.descriptor to 858,
                HeatingGasConsumptionSummaryDhwFeature.descriptor to 546,
                HeatingGasConsumptionSummaryHeatingFeature.descriptor to 546,
                HeatingGasConsumptionTotalFeature.descriptor to 858,
                HeatingHeatProductionFeature.descriptor to 858,
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
                HeatingSensorsTemperatureOutsideFeature.descriptor to 234,
                HeatingSolarFeature.descriptor to 4056,
                HeatingSolarPowerProductionFeature.descriptor to 858,
                HeatingSolarPumpsCircuitFeature.descriptor to 1794,
                HeatingSolarRechargeSuppressionFeature.descriptor to 1794,
                HeatingSolarSensorsTemperatureCollectorFeature.descriptor to 1794,
                HeatingSolarSensorsTemperatureDhwFeature.descriptor to 234,
                HeatingSolarStatisticsFeature.descriptor to 156,
                HeatingSolarSummaryPowerProductionFeature.descriptor to 546,
                HeatingValvesDiverterHeatDhwFeature.descriptor to 78,
                HeatingConfigurationRegulationFeature.descriptor to 78,
                DeviceConfigurationFeature.descriptor to 78,
                HeatingCircuitsNOperatingProgramsActiveFeature.descriptor to 936,
                HeatingCircuitsNNameFeature.descriptor to 468,
            )
        val features = loadFeatures()

        context("!One billion validations test") {
            val size = 850
            val step = size / 100
            val startTime = Clock.System.now()
            repeat(size) { repeatIndex ->
                descriptors.forEachIndexed { _, (descriptor, expected) ->
                    val result = features.allOf(descriptor, descriptor.byStructure)
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
