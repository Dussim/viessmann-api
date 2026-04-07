package xyz.dussim.feature.benchmark

import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.StringValue

@GenerateFeatureImplementation("device.timeseries.monitoringIonization")
interface DeviceTimeseriesMonitoringIonizationFeature : Feature {
    companion object

    val countOne: DoubleValue
    val timestampOne: StringValue
    val countTwo: DoubleValue
    val timestampTwo: StringValue
    val countThree: DoubleValue
    val timestampThree: StringValue
    val countFour: DoubleValue
    val timestampFour: StringValue
    val countFive: DoubleValue
    val timestampFive: StringValue
    val countSix: DoubleValue
    val timestampSix: StringValue
    val countSeven: DoubleValue
    val timestampSeven: StringValue
}
