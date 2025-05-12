package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent.ZigbeeDeviceStatus

@GenerateFeatureImplementation("device.etn")
interface DeviceEtnFeature : ViessmannFeature.Device {
    companion object

    val value: String
}

@GenerateFeatureImplementation("device.messages.errors.raw")
interface DeviceMessagesErrorsRawFeature : ViessmannFeature.Device {
    companion object

    val entries: List<DeviceError>
}

@GenerateFeatureImplementation("device.serial")
interface DeviceSerialFeature : ViessmannFeature.Device {
    companion object

    val value: String
}

@GenerateFeatureImplementation("device.timeseries.monitoringIonization")
interface DeviceTimeseriesMonitoringIonizationFeature : ViessmannFeature.Device {
    companion object

    val countOne: Int
    val timestampOne: String
    val countTwo: Int
    val timestampTwo: String
    val countThree: Int
    val timestampThree: String
    val countFour: Int
    val timestampFour: String
    val countFive: Int
    val timestampFive: String
    val countSix: Int
    val timestampSix: String
    val countSeven: Int
    val timestampSeven: String
}

@GenerateFeatureImplementation("device.zigbee.active")
interface DeviceZigbeeActiveFeature : ViessmannFeature.Device {
    companion object

    interface SetActive : Command1<Boolean> {
        val active: ViessmannFeatureCommandParamConstraints<Boolean>
    }

    val active: Boolean

    val activate: Command0
    val deactivate: Command0
    val setActive: SetActive
}

@GenerateFeatureImplementation("heating.boiler.pumps.internal")
interface HeatingBoilerPumpsInternalFeature : ViessmannFeature.Device {
    companion object

    val status: String
}

@GenerateFeatureImplementation("heating.boiler.pumps.internal.target")
interface HeatingBoilerPumpsInternalTargetFeature : ViessmannFeature.Device {
    companion object

    val value: Double
}

@GenerateFeatureImplementation("heating.boiler.sensors.temperature.commonSupply")
interface HeatingBoilerSensorsTemperatureCommonSupplyFeature : ViessmannFeature.Device {
    companion object

    val status: String
}

@GenerateFeatureImplementation("heating.boiler.serial")
interface HeatingBoilerSerialFeature : ViessmannFeature.Device {
    companion object

    val value: String
}

@GenerateFeatureImplementation("heating.boiler.temperature")
interface HeatingBoilerTemperatureFeature : ViessmannFeature.Device {
    companion object

    val value: Double
}

@GenerateFeatureImplementation("heating.bufferCylinder.sensors.temperature.main")
interface HeatingBufferCylinderSensorsTemperatureMainFeature : ViessmannFeature.Device {
    companion object

    val status: String
}

@GenerateFeatureImplementation("device.configuration")
interface DeviceConfigurationFeature : ViessmannFeature.Device {
    companion object

    val ttCircuitsActive: List<String>
    val ttCircuitsEnabled: List<String>
    val dhwActive: Boolean
    val dhwEnabled: Boolean
    val solarActive: Boolean
    val solarEnabled: Boolean
    val circuitsActive: List<String>
    val circuitsEnabled: List<String>
    val heatingConfigurationRegulation: String
    val roomsActive: List<String>
    val roomsOthersActive: List<String>
    val roomsEnabled: List<String>
    val roomsOthersEnabled: List<String>
}

@GenerateFeatureImplementation("device.zigbee.coordinator")
interface DeviceZigbeeCoordinatorFeature : ViessmannFeature.Device {
    companion object

    interface AddDevice : Command3<String, String, String> {
        val id: ViessmannFeatureCommandParamConstraints<String>
        val ic: ViessmannFeatureCommandParamConstraints<String>
        val type: ViessmannFeatureCommandParamConstraints<String>
    }

    interface RemoveDevice : Command1<String> {
        val id: ViessmannFeatureCommandParamConstraints<String>
    }

    val status: List<ZigbeeDeviceStatus>
    val timeout: Int

    val addDevice: AddDevice
    val removeDevice: RemoveDevice
}

@GenerateFeatureImplementation("rooms")
interface RoomsFeature : ViessmannFeature.Device {
    companion object

    interface Add : Command2<String, String> {
        val name: ViessmannFeatureCommandParamConstraints<String>
        val type: ViessmannFeatureCommandParamConstraints<String>
    }

    val enabled: List<String>

    val add: Add
}

@GenerateFeatureImplementation("rooms.{}")
interface RoomsNFeature : ViessmannFeature.Device {
    companion object

    interface AddActor : Command2<String, Double> {
        val actorDeviceId: ViessmannFeatureCommandParamConstraints<String>
        val heatingCircuit: ViessmannFeatureCommandParamConstraints<Double>
    }

    interface MoveActor : Command3<String, Double, Double> {
        val actorDeviceId: ViessmannFeatureCommandParamConstraints<String>
        val heatingCircuit: ViessmannFeatureCommandParamConstraints<Double>
        val newRoomId: ViessmannFeatureCommandParamConstraints<Double>
    }

    interface SetName : Command1<String> {
        val name: ViessmannFeatureCommandParamConstraints<String>
    }

    interface SetType : Command1<String> {
        val type: ViessmannFeatureCommandParamConstraints<String>
    }

    interface RemoveActor : Command1<String> {
        val actorDeviceId: ViessmannFeatureCommandParamConstraints<String>
    }

    val name: String
    val type: String
    val actors: Nothing // Fixme I have no idea what it is

    val addActor: AddActor
    val moveActor: MoveActor
    val setName: SetName
    val setType: SetType
    val removeActor: RemoveActor
    val remove: Command0
}

@GenerateFeatureImplementation("rooms.others.{}")
interface RoomsOthersFeature : ViessmannFeature.Device {
    companion object

    interface SetActive : Command1<Boolean> {
        val active: ViessmannFeatureCommandParamConstraints<Boolean>
    }

    interface SetName : Command1<String> {
        val name: ViessmannFeatureCommandParamConstraints<String>
    }

    val active: Boolean
    val name: String
    val heatingCircuit: Int
    val actors: Nothing

    val activate: Command0
    val deactivate: Command0
    val setActive: SetActive
    val setName: SetName
}

@GenerateFeatureImplementation("device.timezone")
interface DeviceTimezoneFeature : ViessmannFeature.Device {
    companion object

    interface SetTimezone : Command1<String> {
        val value: ViessmannFeatureCommandParamConstraints<String>
    }

    val value: String

    val setTimezone: SetTimezone
}

@GenerateFeatureImplementation("tcu.mode")
interface TcuModeFeature : ViessmannFeature.Device {
    companion object

    interface SetMode : Command1<String> {
        val mode: ViessmannFeatureCommandParamConstraints<String>
    }

    val mode: String
    val setMode: SetMode
}

@GenerateFeatureImplementation("heating.circuits.{}.heating.schedule")
interface HeatingCircuitsNHeatingScheduleFeature : ViessmannFeature.Device {
    companion object

    interface SetSchedule : Command1<ScheduleMap> {
        val newSchedule: ViessmannFeatureCommandParamConstraints<ScheduleMap>
    }

    val entries: ScheduleMap
    val active: Boolean

    val setSchedule: SetSchedule
    val resetSchedule: Command0
}
