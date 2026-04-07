package xyz.dussim.viessmann.feature.api

import kotlin.time.Instant

public abstract class BaseFeature(
    override val feature: String,
    override val wildcardFeature: String,
    override val deviceId: String?,
    override val gatewayId: String?,
    override val isActive: Boolean?,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    override val properties: EfficientStringKeyMap<Property>,
    override val commands: EfficientStringKeyMap<Command>,
    private val hashCode: Int,
) : Feature {
    constructor(feature: Feature) : this(
        feature = feature.feature,
        wildcardFeature = feature.wildcardFeature,
        deviceId = feature.deviceId,
        gatewayId = feature.gatewayId,
        isActive = feature.isActive,
        isEnabled = feature.isEnabled,
        isReady = feature.isReady,
        apiVersion = feature.apiVersion,
        timestamp = feature.timestamp,
        uri = feature.uri,
        properties = feature.properties,
        commands = feature.commands,
        hashCode = feature.hashCode(),
    )

    override fun equals(other: Any?): Boolean = equalsImpl(other)

    override fun hashCode(): Int = hashCode
}
