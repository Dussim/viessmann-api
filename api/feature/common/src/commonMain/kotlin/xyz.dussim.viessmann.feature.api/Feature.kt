package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmRecord
import kotlin.time.Instant

interface Feature {
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
}

@Serializable
@JvmRecord
data class ViessmannFeature(
    override val feature: String,
    override val wildcardFeature: String = feature.toWildcardFeature(),
    override val deviceId: String? = null,
    override val gatewayId: String? = null,
    override val isActive: Boolean? = null,
    override val isEnabled: Boolean,
    override val isReady: Boolean,
    override val apiVersion: Int,
    override val timestamp: Instant,
    override val uri: String,
    @Serializable(with = PropertiesSerializer::class)
    override val properties: EfficientStringKeyMap<Property>,
    @Serializable(with = CommandsSerializer::class)
    override val commands: EfficientStringKeyMap<Command>,
) : Feature

@Deprecated("Use ViessmannFeature", ReplaceWith("ViessmannFeature"))
typealias DeviceFeature = ViessmannFeature

@Deprecated("Use ViessmannFeature", ReplaceWith("ViessmannFeature"))
typealias GatewayFeature = ViessmannFeature

@Deprecated("Use ViessmannFeature", ReplaceWith("ViessmannFeature"))
typealias GeofencingFeature = ViessmannFeature

fun Feature.equalsImpl(other: Any?): Boolean {
    if (other === this) return true
    if (other !is Feature) return false
    return feature == other.feature &&
        wildcardFeature == other.wildcardFeature &&
        isEnabled == other.isEnabled &&
        isReady == other.isReady &&
        apiVersion == other.apiVersion &&
        timestamp == other.timestamp &&
        uri == other.uri &&
        properties == other.properties &&
        commands == other.commands &&
        deviceId == other.deviceId &&
        gatewayId == other.gatewayId &&
        isActive == other.isActive
}

private fun String.toWildcardFeature(): String =
    split(".").joinToString(".") { segment ->
        if (segment.isNotEmpty() && segment.all { it.isDigit() }) "{}" else segment
    }
