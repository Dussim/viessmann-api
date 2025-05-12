package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable

@Serializable
data class GatewaysSummary(
    val data: Data,
) {
    @Serializable
    data class Data(
        val aggregatedStatus: AggregatedStatus,
        val total: Int,
    )

    @Serializable
    data class AggregatedStatus(
        val error: Int,
        val offline: Int,
        val maintenance: Int,
        val worksProperly: Int,
        val remoteDiagnosticSession: Int,
        val nbIotConnected: Int,
    )
}
