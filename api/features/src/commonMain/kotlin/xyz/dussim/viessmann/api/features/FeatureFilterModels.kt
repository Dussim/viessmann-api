package xyz.dussim.viessmann.api.features

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFilterRequest(
    val filter: List<String>? = null,
    val regex: String? = null,
    val skipDisabled: Boolean? = null,
    val skipNotReady: Boolean? = null,
)

@Serializable
data class GatewayFeatureFilterRequest(
    val filter: List<String>? = null,
    val regex: String? = null,
    val skipDisabled: Boolean? = null,
    val skipNotReady: Boolean? = null,
    val includeDevicesFeatures: Boolean? = null,
)

@Serializable
data class CommandExecutionResponse(
    val data: CommandResult,
)

@Serializable
data class CommandResult(
    val success: String,
    val message: String? = null,
    val reason: String? = null,
)
