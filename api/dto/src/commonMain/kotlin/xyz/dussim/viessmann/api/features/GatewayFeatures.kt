package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation

@GenerateFeatureImplementation("gateway.firmware")
interface GatewayFirmwareViessmannFeature : ViessmannFeature.Gateway {
    companion object

    interface UpdateCommand : Command2<String, String> {
        val url: ViessmannFeatureCommandParamConstraints<String>
        val version: ViessmannFeatureCommandParamConstraints<String>
    }

    enum class UpdateStatus(
        override val propertyValue: String,
    ) : FeatureEnum {
        Idle("idle"),
        Downloading("downloading"),
        Updated("updated"),
        Rebooting("rebooting"),
        Error("error"),
    }

    val version: String
    val updateStatus: UpdateStatus
    val libraryVersion: String
    val libraryType: String

    val update: UpdateCommand
}

@GenerateFeatureImplementation("gateway.status")
interface GatewayStatusFeature : ViessmannFeature.Gateway {
    companion object

    val online: Boolean

    val reboot: Command0
}

@GenerateFeatureImplementation("gateway.remoteDiagnostics")
interface GatewayRemoteDiagnosticsFeature : ViessmannFeature.Gateway {
    companion object

    val status: String

    val open: Command0
    val close: Command0
}

@GenerateFeatureImplementation("gateway.wifi")
interface GatewayWifiFeature : ViessmannFeature.Gateway {
    companion object

    val strength: Int
}

@GenerateFeatureImplementation("gateway.mode")
interface GatewayModeFeature : ViessmannFeature.Gateway {
    companion object

    val mode: String
}

@GenerateFeatureImplementation("gateway.devices")
interface GatewayDevicesFeature : ViessmannFeature.Gateway {
    companion object

    val devices: List<DeviceInfo>
}

@GenerateFeatureImplementation("gateway.bmuconnection")
interface GatewayBmuConnectionFeature : ViessmannFeature.Gateway {
    companion object

    val status: String
}

@GenerateFeatureImplementation("battery.overview")
interface BatteryOverviewFeature : ViessmannFeature.Gateway {
    companion object
}

@GenerateFeatureImplementation("photovoltaic.overview")
interface PhotovoltaicOverviewFeature : ViessmannFeature.Gateway {
    companion object
}

@GenerateFeatureImplementation("power.limitation.overview")
interface PowerLimitationOverviewFeature : ViessmannFeature.Gateway {
    companion object
}

@GenerateFeatureImplementation("thermal.cockpit")
interface ThermalCockpitFeature : ViessmannFeature.Gateway {
    companion object
}
