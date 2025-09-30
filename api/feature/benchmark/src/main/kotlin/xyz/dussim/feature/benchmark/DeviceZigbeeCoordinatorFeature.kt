package xyz.dussim.feature.benchmark

import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.StringConstraints

@GenerateFeatureImplementation("device.zigbee.coordinator")
interface DeviceZigbeeCoordinatorFeature : Feature.Device {
    companion object

    interface AddDevice : Command3<String, String, String> {
        companion object

        val id: StringConstraints
        val ic: StringConstraints
        val type: StringConstraints
    }

    interface RemoveDevice : Command1<String> {
        companion object

        val id: StringConstraints
    }

    val status: ListZigbeeDeviceStatusValue
    val timeout: DoubleValue

    val addDevice: AddDevice
    val removeDevice: RemoveDevice
}
