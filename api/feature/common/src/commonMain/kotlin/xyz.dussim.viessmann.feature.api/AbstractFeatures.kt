package xyz.dussim.viessmann.feature.api

import kotlin.time.Instant

public abstract class AbstractDeviceFeature(
    override val feature: String,
    override val wildcardFeature: String,
    override val deviceId: String,
    override val gatewayId: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: EfficientStringKeyMap<Property>,
    override val commands: EfficientStringKeyMap<Command>,
    override val isActive: Boolean?,
    private val hashCode: Int,
) : Feature.Device {
    constructor(feature: Feature.Device) : this(
        feature = feature.feature,
        wildcardFeature = feature.wildcardFeature,
        deviceId = feature.deviceId,
        gatewayId = feature.gatewayId,
        isEnabled = feature.isEnabled,
        isReady = feature.isReady,
        apiVersion = feature.apiVersion,
        timestamp = feature.timestamp,
        uri = feature.uri,
        properties = feature.properties,
        commands = feature.commands,
        isActive = feature.isActive,
        hashCode = feature.hashCode(),
    )

    override fun equals(other: Any?): Boolean = equalsImpl(other)

    override fun hashCode(): Int = hashCode
}

public abstract class AbstractGatewayFeature(
    override val feature: String,
    override val wildcardFeature: String,
    override val gatewayId: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: EfficientStringKeyMap<Property>,
    override val commands: EfficientStringKeyMap<Command>,
    override val deviceId: String?,
    override val isActive: Boolean?,
    private val hashCode: Int,
) : Feature.Gateway {
    constructor(feature: Feature.Gateway) : this(
        feature = feature.feature,
        wildcardFeature = feature.wildcardFeature,
        gatewayId = feature.gatewayId,
        isEnabled = feature.isEnabled,
        isReady = feature.isReady,
        apiVersion = feature.apiVersion,
        timestamp = feature.timestamp,
        uri = feature.uri,
        properties = feature.properties,
        commands = feature.commands,
        deviceId = feature.deviceId,
        isActive = feature.isActive,
        hashCode = feature.hashCode(),
    )

    override fun equals(other: Any?): Boolean = equalsImpl(other)

    override fun hashCode(): Int = hashCode
}

public abstract class AbstractGeofencingFeature(
    override val feature: String,
    override val wildcardFeature: String,
    override val isActive: Boolean,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: EfficientStringKeyMap<Property>,
    override val commands: EfficientStringKeyMap<Command>,
    override val deviceId: String?,
    override val gatewayId: String?,
    private val hashCode: Int,
) : Feature.Geofencing {
    constructor(feature: Feature.Geofencing) : this(
        feature = feature.feature,
        wildcardFeature = feature.wildcardFeature,
        isActive = feature.isActive,
        isEnabled = feature.isEnabled,
        isReady = feature.isReady,
        apiVersion = feature.apiVersion,
        timestamp = feature.timestamp,
        uri = feature.uri,
        properties = feature.properties,
        commands = feature.commands,
        deviceId = feature.deviceId,
        gatewayId = feature.gatewayId,
        hashCode = feature.hashCode(),
    )

    override fun equals(other: Any?): Boolean = equalsImpl(other)

    override fun hashCode(): Int = hashCode
}
