package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmRecord
import kotlin.time.Instant

sealed interface Feature {
    val feature: String

    val isEnabled: Boolean
    val isReady: Boolean

    val apiVersion: Int
    val timestamp: Instant
    val uri: String // TODO multiplatform uri?

    val properties: EfficientStringKeyMap<Property>
    val commands: EfficientStringKeyMap<Command>

    val deviceId: String?
    val gatewayId: String?
    val isActive: Boolean?

    val wildcardFeature: String get() = feature.toWildcardFeature()

    interface Device : Feature {
        override val deviceId: String
        override val gatewayId: String
    }

    interface Gateway : Feature {
        override val gatewayId: String
    }

    interface Geofencing : Feature {
        override val isActive: Boolean
    }
}

@Serializable
@JvmRecord
data class DeviceFeature(
    override val feature: String,
    override val wildcardFeature: String = feature.toWildcardFeature(),
    override val deviceId: String,
    override val gatewayId: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    @Serializable(with = PropertiesSerializer::class)
    override val properties: EfficientStringKeyMap<Property>,
    @Serializable(with = CommandsSerializer::class)
    override val commands: EfficientStringKeyMap<Command>,
    override val isActive: Boolean?,
) : Feature.Device

@Serializable
@JvmRecord
data class GatewayFeature(
    override val feature: String,
    override val wildcardFeature: String = feature.toWildcardFeature(),
    override val gatewayId: String,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    @Serializable(with = PropertiesSerializer::class)
    override val properties: EfficientStringKeyMap<Property>,
    @Serializable(with = CommandsSerializer::class)
    override val commands: EfficientStringKeyMap<Command>,
    override val deviceId: String?,
    override val isActive: Boolean?,
) : Feature.Gateway

@Serializable
@JvmRecord
data class GeofencingFeature(
    override val feature: String,
    override val wildcardFeature: String = feature.toWildcardFeature(),
    override val isActive: Boolean,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    @Serializable(with = PropertiesSerializer::class)
    override val properties: EfficientStringKeyMap<Property>,
    @Serializable(with = CommandsSerializer::class)
    override val commands: EfficientStringKeyMap<Command>,
    override val deviceId: String?,
    override val gatewayId: String?,
) : Feature.Geofencing

private fun String.toWildcardFeature(): String =
    split(".").joinToString(".") { segment ->
        if (segment.isNotEmpty() && segment.all { it.isDigit() }) "{}" else segment
    }
