package xyz.dussim.viessmann.api.features

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.FeatureRegistry
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import kotlin.time.Clock

class DescriptorExpectation<F : Feature>(
    val descriptor: FeatureDescriptor<F>,
    val expected: Int,
) {
    fun validateStructure(features: FeatureRegistry) {
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

    fun validateFailFast(features: FeatureRegistry) {
        val result = features.allOf(descriptor, descriptor.byFailFastStructure)
        try {
            result shouldHaveSize expected
        } catch (e: AssertionError) {
            throw AssertionError("Failed for $descriptor", e)
        }
    }
}

private infix fun <F : Feature> FeatureDescriptor<F>.expecting(expected: Int) = DescriptorExpectation(this, expected)

val validationDescriptors =
    listOf(
        DeviceEtnFeature.descriptor expecting 936,
        DeviceMessagesErrorsRawFeature.descriptor expecting 78,
        DeviceSerialFeature.descriptor expecting 936,
        DeviceTimeseriesMonitoringIonizationFeature.descriptor expecting 78,
        DeviceZigbeeActiveFeature.descriptor expecting 156,
        HeatingBoilerPumpsInternalTargetFeature.descriptor expecting 1326,
        HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor expecting 1794,
        HeatingBoilerSerialFeature.descriptor expecting 936,
        HeatingBoilerTemperatureFeature.descriptor expecting 1326,
        HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor expecting 1794,
        HeatingBurnersFeature.descriptor expecting 156,
        HeatingBurnersNDemandTemperatureFeature.descriptor expecting 1326,
        HeatingBurnersNModulationFeature.descriptor expecting 1326,
        HeatingBurnersNStatisticsFeature.descriptor expecting 78,
        HeatingBurnersNFeature.descriptor expecting 4056,
        HeatingCircuitsFeature.descriptor expecting 156,
        HeatingCircuitsNCirculationPumpFeature.descriptor expecting 1794,
        HeatingCircuitsNFrostprotectionFeature.descriptor expecting 1794,
        HeatingCircuitsNHeatingCurveFeature.descriptor expecting 234,
        HeatingCircuitsNHeatingScheduleFeature.descriptor expecting 390,
        HeatingCircuitsNOperatingModesActiveFeature.descriptor expecting 390,
        HeatingCircuitsNOperatingModesHeatingFeature.descriptor expecting 4056,
        HeatingCircuitsNOperatingModesStandbyFeature.descriptor expecting 4056,
        HeatingCircuitsNOperatingProgramsComfortFeature.descriptor expecting 234,
        HeatingCircuitsNOperatingProgramsForcedLastFromScheduleFeature.descriptor expecting 390,
        HeatingCircuitsNOperatingProgramsNormalFeature.descriptor expecting 702,
        HeatingCircuitsNOperatingProgramsReducedFeature.descriptor expecting 702,
        HeatingCircuitsNOperatingProgramsReducedEnergySavingFeature.descriptor expecting 234,
        HeatingCircuitsNOperatingProgramsScreedDryingFeature.descriptor expecting 234,
        HeatingCircuitsNOperatingProgramsStandbyFeature.descriptor expecting 4056,
        HeatingCircuitsNRemoteControllerFeature.descriptor expecting 234,
        HeatingCircuitsNSensorsTemperatureSupplyFeature.descriptor expecting 1794,
        HeatingCircuitsNTemperatureFeature.descriptor expecting 1326,
        HeatingCircuitsNTemperatureLevelsFeature.descriptor expecting 234,
        HeatingCircuitsNZoneDemandFeature.descriptor expecting 234,
        HeatingCircuitsNZoneModeFeature.descriptor expecting 4056,
        HeatingCircuitsNFeature.descriptor expecting 234,
        HeatingConfigurationBufferCylinderSizeFeature.descriptor expecting 312,
        HeatingConfigurationCentralHeatingCylinderSizeFeature.descriptor expecting 312,
        HeatingConfigurationDhwCylinderPumpFeature.descriptor expecting 78,
        HeatingConfigurationDhwCylinderSizeFeature.descriptor expecting 312,
        HeatingConfigurationHouseHeatingLoadFeature.descriptor expecting 312,
        HeatingConfigurationHouseOrientationFeature.descriptor expecting 78,
        HeatingConfigurationMultiFamilyHouseFeature.descriptor expecting 4056,
        HeatingConfigurationPressureTotalFeature.descriptor expecting 78,
        HeatingDeviceTimeFeature.descriptor expecting 78,
        HeatingDeviceTimeSourceFeature.descriptor expecting 78,
        HeatingDhwFeature.descriptor expecting 78,
        HeatingDhwHygieneFeature.descriptor expecting 78,
        HeatingDhwHygieneTriggerFeature.descriptor expecting 78,
        HeatingDhwOneTimeChargeFeature.descriptor expecting 156,
        HeatingDhwOperatingModesActiveFeature.descriptor expecting 390,
        HeatingDhwOperatingModesBalancedFeature.descriptor expecting 4056,
        HeatingDhwOperatingModesOffFeature.descriptor expecting 4056,
        HeatingDhwPumpsCirculationFeature.descriptor expecting 1794,
        HeatingDhwPumpsCirculationScheduleFeature.descriptor expecting 390,
        HeatingDhwScheduleFeature.descriptor expecting 390,
        HeatingDhwScheduleModeFeature.descriptor expecting 390,
        HeatingDhwSensorsTemperatureDhwCylinderFeature.descriptor expecting 1794,
        HeatingDhwSensorsTemperatureOutletFeature.descriptor expecting 1794,
        HeatingDhwTemperatureHygieneFeature.descriptor expecting 78,
        HeatingDhwTemperatureMainFeature.descriptor expecting 78,
        HeatingFlueSensorsTemperatureMainFeature.descriptor expecting 1794,
        HeatingGasConsumptionDhwFeature.descriptor expecting 858,
        HeatingGasConsumptionHeatingFeature.descriptor expecting 858,
        HeatingGasConsumptionSummaryDhwFeature.descriptor expecting 546,
        HeatingGasConsumptionSummaryHeatingFeature.descriptor expecting 546,
        HeatingGasConsumptionTotalFeature.descriptor expecting 858,
        HeatingHeatProductionDhwFeature.descriptor expecting 858,
        HeatingHeatProductionHeatingFeature.descriptor expecting 858,
        HeatingHeatProductionSummaryDhwFeature.descriptor expecting 546,
        HeatingHeatProductionSummaryHeatingFeature.descriptor expecting 546,
        HeatingHeatProductionTotalFeature.descriptor expecting 858,
        HeatingOperatingProgramsHolidayFeature.descriptor expecting 156,
        HeatingOperatingProgramsHolidayAtHomeFeature.descriptor expecting 156,
        HeatingPowerConsumptionDhwFeature.descriptor expecting 858,
        HeatingPowerConsumptionHeatingFeature.descriptor expecting 858,
        HeatingPowerConsumptionSummaryDhwFeature.descriptor expecting 546,
        HeatingPowerConsumptionSummaryHeatingFeature.descriptor expecting 546,
        HeatingPowerConsumptionTotalFeature.descriptor expecting 858,
        HeatingSensorsPressureSupplyFeature.descriptor expecting 1794,
        HeatingSensorsTemperatureOutsideFeature.descriptor expecting 1794,
        HeatingSolarFeature.descriptor expecting 4056,
        HeatingSolarPowerProductionFeature.descriptor expecting 858,
        HeatingSolarPumpsCircuitFeature.descriptor expecting 1794,
        HeatingSolarRechargeSuppressionFeature.descriptor expecting 1794,
        HeatingSolarSensorsTemperatureCollectorFeature.descriptor expecting 1794,
        HeatingSolarSensorsTemperatureDhwFeature.descriptor expecting 1794,
        HeatingSolarStatisticsFeature.descriptor expecting 156,
        HeatingSolarSummaryPowerProductionFeature.descriptor expecting 546,
        HeatingValvesDiverterHeatDhwFeature.descriptor expecting 78,
        HeatingConfigurationRegulationFeature.descriptor expecting 78,
        DeviceConfigurationFeature.descriptor expecting 78,
        HeatingCircuitsNOperatingProgramsActiveFeature.descriptor expecting 936,
    )

fun loadFeatures(registryFactory: (List<ViessmannFeature>) -> FeatureRegistry): FeatureRegistry =
    json
        .decodeFromString<ResponseData<ViessmannFeature>>(
            readFileFromResources("features/device/very_long.json"),
        ).data
        .let(registryFactory)

fun FunSpec.singleThreadedValidationTest(
    testName: String,
    features: FeatureRegistry,
    size: Int = 930,
) {
    context(testName) {
        val step = size / 10
        val startTime = Clock.System.now()
        repeat(size) { repeatIndex ->
            validationDescriptors.forEach { it.validateStructure(features) }
            if (repeatIndex % step == 0) {
                println("[$testName] Finished $repeatIndex/$size")
            }
        }
        println("[$testName] Finished in ${Clock.System.now() - startTime}")
    }
}

fun FunSpec.parallelValidationTest(
    testName: String,
    features: FeatureRegistry,
    size: Int = 930,
) {
    context(testName) {
        val step = size / 10
        val startTime = Clock.System.now()
        val dispatcher = Dispatchers.Default
        repeat(size) { repeatIndex ->
            validationDescriptors
                .map { expectation ->
                    async(dispatcher) { expectation.validateStructure(features) }
                }.awaitAll()
            if (repeatIndex % step == 0) {
                println("[$testName] Finished $repeatIndex/$size")
            }
        }
        println("[$testName] Finished in ${Clock.System.now() - startTime}")
    }
}

fun FunSpec.singleThreadedFailFastValidationTest(
    testName: String,
    features: FeatureRegistry,
    size: Int = 930,
) {
    context(testName) {
        val step = size / 10
        val startTime = Clock.System.now()
        repeat(size) { repeatIndex ->
            validationDescriptors.forEach { it.validateFailFast(features) }
            if (repeatIndex % step == 0) {
                println("[$testName] Finished $repeatIndex/$size")
            }
        }
        println("[$testName] Finished in ${Clock.System.now() - startTime}")
    }
}

fun FunSpec.parallelFailFastValidationTest(
    testName: String,
    features: FeatureRegistry,
    size: Int = 930,
) {
    context(testName) {
        val step = size / 10
        val startTime = Clock.System.now()
        val dispatcher = Dispatchers.Default
        repeat(size) { repeatIndex ->
            validationDescriptors
                .map { expectation ->
                    async(dispatcher) { expectation.validateFailFast(features) }
                }.awaitAll()
            if (repeatIndex % step == 0) {
                println("[$testName] Finished $repeatIndex/$size")
            }
        }
        println("[$testName] Finished in ${Clock.System.now() - startTime}")
    }
}
