package xyz.dussim.viessmann.api.features.internal

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.features.ViessmannFeature
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommand
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty

@Serializable
internal data class ViessmannFeatureUnspecified(
    override val feature: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: Map<String, ViessmannFeatureProperty<*>>,
    override val commands: Map<String, ViessmannFeatureCommand>,
    override val deviceId: String? = null,
    override val gatewayId: String? = null,
    override val isActive: Boolean? = null,
) : ViessmannFeature

@Serializable
internal data class ViessmannDeviceFeatureImpl(
    override val feature: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: Map<String, ViessmannFeatureProperty<*>>,
    override val commands: Map<String, ViessmannFeatureCommand>,
    override val deviceId: String,
    override val gatewayId: String,
    override val isActive: Boolean? = null,
) : ViessmannFeature.Device {
    companion object {
        operator fun invoke(viessmannFeature: ViessmannFeature) =
            with(viessmannFeature) {
                ViessmannDeviceFeatureImpl(
                    feature = feature,
                    isEnabled = isEnabled,
                    isReady = isReady,
                    apiVersion = apiVersion,
                    timestamp = timestamp,
                    uri = uri,
                    properties = properties,
                    commands = commands,
                    deviceId = requireNotNull(deviceId) { "'deviceId' is required for device features" },
                    gatewayId = requireNotNull(gatewayId) { "'gatewayId' is required for device features" },
                    isActive = isActive,
                )
            }
    }
}

@Serializable
internal data class ViessmannGatewayFeatureImpl(
    override val feature: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: Map<String, ViessmannFeatureProperty<*>>,
    override val commands: Map<String, ViessmannFeatureCommand>,
    override val deviceId: String? = null,
    override val gatewayId: String,
    override val isActive: Boolean? = null,
) : ViessmannFeature.Gateway {
    companion object {
        operator fun invoke(viessmannFeature: ViessmannFeature) =
            with(viessmannFeature) {
                ViessmannGatewayFeatureImpl(
                    feature = feature,
                    isEnabled = isEnabled,
                    isReady = isReady,
                    apiVersion = apiVersion,
                    timestamp = timestamp,
                    uri = uri,
                    properties = properties,
                    commands = commands,
                    deviceId = deviceId,
                    gatewayId = requireNotNull(gatewayId) { "'gatewayId' is required for gateway features" },
                    isActive = isActive,
                )
            }
    }
}

@Serializable
internal data class ViessmannGeofencingFeatureImpl(
    override val feature: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: Map<String, ViessmannFeatureProperty<*>>,
    override val commands: Map<String, ViessmannFeatureCommand>,
    override val deviceId: String? = null,
    override val gatewayId: String? = null,
    override val isActive: Boolean,
) : ViessmannFeature.Geofencing {
    companion object {
        operator fun invoke(viessmannFeature: ViessmannFeature) =
            with(viessmannFeature) {
                ViessmannGeofencingFeatureImpl(
                    feature = feature,
                    isEnabled = isEnabled,
                    isReady = isReady,
                    apiVersion = apiVersion,
                    timestamp = timestamp,
                    uri = uri,
                    properties = properties,
                    commands = commands,
                    deviceId = deviceId,
                    gatewayId = gatewayId,
                    isActive = requireNotNull(isActive) { "'isActive' is required for geofencing features" },
                )
            }
    }
}
