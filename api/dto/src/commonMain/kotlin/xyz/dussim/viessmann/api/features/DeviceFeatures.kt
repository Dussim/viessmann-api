package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command1
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.Command3
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.ListDeviceErrorValue
import xyz.dussim.viessmann.feature.api.ListRoomActorValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.ListZigbeeDeviceStatusValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.Schedule
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue
import kotlin.jvm.JvmRecord

@GenerateFeatureImplementation("device.etn")
interface DeviceEtnFeature : Feature.Device {
    companion object

    val value: StringValue
}

@GenerateFeatureImplementation("device.messages.errors.raw")
interface DeviceMessagesErrorsRawFeature : Feature.Device {
    companion object

    val entries: ListDeviceErrorValue
}

@GenerateFeatureImplementation("device.serial")
interface DeviceSerialFeature : Feature.Device {
    companion object

    val value: StringValue
}

@GenerateFeatureImplementation("device.timeseries.monitoringIonization")
interface DeviceTimeseriesMonitoringIonizationFeature : Feature.Device {
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

@GenerateFeatureImplementation("device.zigbee.active")
interface DeviceZigbeeActiveFeature : Feature.Device {
    companion object

    interface SetActive : Command1<Boolean> {
        companion object

        val active: BooleanConstraints
    }

    interface Activate : Command0 {
        companion object
    }

    interface Deactivate : Command0 {
        companion object
    }

    val active: BooleanValue

    val activate: Activate
    val deactivate: Deactivate
    val setActive: SetActive
}

@GenerateFeatureImplementation("heating.boiler.pumps.internal")
interface HeatingBoilerPumpsInternalFeature : Feature.Device {
    companion object;

    @FeatureEnum
    sealed interface Status {
        data object On : Status

        data object Off : Status

        @JvmRecord
        data class Unknown(
            val value: String,
        ) : Status

        @FeatureEnum.Factory
        companion object : FeatureEnumFactory<StringValue, Status> {
            override fun invoke(propertyValue: StringValue): Status =
                when (propertyValue.element) {
                    "on" -> On
                    "off" -> Off
                    else -> Unknown(propertyValue.element)
                }
        }
    }

    val status: Status
}

@GenerateFeatureImplementation("heating.boiler.pumps.internal.target")
interface HeatingBoilerPumpsInternalTargetFeature : Feature.Device {
    companion object

    val value: DoubleValue
}

@GenerateFeatureImplementation("heating.boiler.sensors.temperature.commonSupply")
interface HeatingBoilerSensorsTemperatureCommonSupplyFeature : Feature.Device {
    companion object

    val status: StringValue
}

@GenerateFeatureImplementation("heating.boiler.serial")
interface HeatingBoilerSerialFeature : Feature.Device {
    companion object

    val value: StringValue
}

@GenerateFeatureImplementation("heating.boiler.temperature")
interface HeatingBoilerTemperatureFeature : Feature.Device {
    companion object

    val value: DoubleValue
}

@GenerateFeatureImplementation("heating.bufferCylinder.sensors.temperature.main")
interface HeatingBufferCylinderSensorsTemperatureMainFeature : Feature.Device {
    companion object

    val status: StringValue
}

@GenerateFeatureImplementation("device.configuration")
interface DeviceConfigurationFeature : Feature.Device {
    companion object

    val ttCircuitsActive: ListStringValue
    val ttCircuitsEnabled: ListStringValue
    val dhwActive: BooleanValue
    val dhwEnabled: BooleanValue
    val solarActive: BooleanValue
    val solarEnabled: BooleanValue
    val circuitsActive: ListStringValue
    val circuitsEnabled: ListStringValue
    val heatingConfigurationRegulation: StringValue
    val roomsActive: ListStringValue
    val roomsOthersActive: ListStringValue
    val roomsEnabled: ListStringValue
    val roomsOthersEnabled: ListStringValue
}

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

@GenerateFeatureImplementation("rooms")
interface RoomsFeature : Feature.Device {
    companion object

    interface Add : Command2<String, String> {
        companion object

        val name: StringConstraints
        val type: StringConstraints
    }

    val enabled: ListStringValue

    val add: Add
}

@GenerateFeatureImplementation("rooms.{}")
interface RoomsNFeature : Feature.Device {
    companion object

    interface AddActor : Command2<String, Double> {
        companion object

        val actorDeviceId: StringConstraints
        val heatingCircuit: NumberConstraints
    }

    interface MoveActor : Command3<String, Double, Double> {
        companion object

        val actorDeviceId: StringConstraints
        val heatingCircuit: NumberConstraints
        val newRoomId: NumberConstraints
    }

    interface SetName : Command1<String> {
        companion object

        val name: StringConstraints
    }

    interface SetType : Command1<String> {
        companion object

        val type: StringConstraints
    }

    interface RemoveActor : Command1<String> {
        companion object

        val actorDeviceId: StringConstraints
    }

    interface Remove : Command0 {
        companion object
    }

    val name: StringValue
    val type: StringValue
    val actors: ListRoomActorValue

    val addActor: AddActor
    val moveActor: MoveActor
    val setName: SetName
    val setType: SetType
    val removeActor: RemoveActor
    val remove: Remove
}

@GenerateFeatureImplementation("rooms.others.{}")
interface RoomsOthersNFeature : Feature.Device {
    companion object

    interface SetActive : Command1<Boolean> {
        companion object

        val active: BooleanConstraints
    }

    interface SetName : Command1<String> {
        companion object

        val name: StringConstraints
    }

    interface Activate : Command0 {
        companion object
    }

    interface Deactivate : Command0 {
        companion object
    }

    val active: BooleanValue
    val name: StringValue
    val heatingCircuit: DoubleValue
    val actors: ListRoomActorValue

    val activate: Activate
    val deactivate: Deactivate
    val setActive: SetActive
    val setName: SetName
}

@GenerateFeatureImplementation("device.timezone")
interface DeviceTimezoneFeature : Feature.Device {
    companion object

    interface SetTimezone : Command1<String> {
        companion object

        val value: StringConstraints
    }

    val value: StringValue

    val setTimezone: SetTimezone
}

@GenerateFeatureImplementation("tcu.mode")
interface TcuModeFeature : Feature.Device {
    companion object

    interface SetMode : Command1<String> {
        companion object

        val mode: StringConstraints
    }

    val mode: StringValue
    val setMode: SetMode
}

@GenerateFeatureImplementation("heating.circuits.{}.heating.schedule")
interface HeatingCircuitsNHeatingScheduleFeature : Feature.Device {
    companion object

    interface SetSchedule : Command1<Map<String, List<Schedule>>> {
        companion object

        val newSchedule: ScheduleConstraints
    }

    interface ResetSchedule : Command0 {
        companion object
    }

    val entries: ScheduleValue
    val active: BooleanValue

    val setSchedule: SetSchedule
    val resetSchedule: ResetSchedule
}
