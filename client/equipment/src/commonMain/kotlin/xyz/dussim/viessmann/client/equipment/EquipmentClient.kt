package xyz.dussim.viessmann.client.equipment

import io.ktor.client.HttpClient
import xyz.dussim.viessmann.api.equipment.GatewayStatus
import xyz.dussim.viessmann.api.equipment.InstallationStatus
import xyz.dussim.viessmann.api.models.Device
import xyz.dussim.viessmann.api.models.Gateway
import xyz.dussim.viessmann.api.models.GatewaysSummary
import xyz.dussim.viessmann.api.models.Installation
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.appendIfNotNull
import xyz.dussim.viessmann.client.core.viessmannApiGet
import xyz.dussim.viessmann.client.core.viessmannApiGetData

data class InstallationsQuery(
    val accessLevel: String? = null,
    val cursor: String? = null,
    val deviceBmuSerial: String? = null,
    val deviceBoilerSerial: String? = null,
    val gatewaySerial: String? = null,
    val includeAccessList: Boolean? = null,
    val includeGateways: Boolean? = null,
    val limit: Int? = null,
    val onlyWithLockedDevices: Boolean? = null,
    val onlyWithRemoteDiagnosticsSupport: Boolean? = null,
    val ownedByMaintainer: Boolean? = null,
    val serial: String? = null,
    val withoutViCareUser: Boolean? = null,
)

data class GatewaysQuery(
    val bmuSerial: String? = null,
    val boilerSerial: String? = null,
    val cursor: String? = null,
    val deviceRoles: String? = null,
    val gatewayType: String? = null,
    val includeBorders: Boolean? = null,
    val includeDevices: Boolean? = null,
    val includeNbiotGateways: Boolean? = null,
    val limit: Int? = null,
    val maxVersion: String? = null,
    val minVersion: String? = null,
    val onlyRegistered: Boolean? = null,
    val ownedByMaintainer: Boolean? = null,
    val serial: String? = null,
)

interface EquipmentClient {
    suspend fun getInstallations(query: InstallationsQuery = InstallationsQuery()): ResponseData<Installation>

    suspend fun getInstallation(
        installationId: String,
        includeAccessList: Boolean? = null,
        includeGateways: Boolean? = null,
    ): Installation

    suspend fun getInstallationsSummary(): GatewaysSummary

    suspend fun getInstallationStatus(installationId: String): InstallationStatus

    suspend fun getGateways(query: GatewaysQuery = GatewaysQuery()): ResponseData<Gateway>

    suspend fun getGateway(
        gatewaySerial: String,
        includeDevices: Boolean? = null,
    ): Gateway

    suspend fun getGatewaysSummary(): GatewaysSummary

    suspend fun getGatewayStatus(gatewaySerial: String): GatewayStatus

    suspend fun getInstallationGateways(
        installationId: String,
        cursor: String? = null,
        includeDevices: Boolean? = null,
        limit: Int? = null,
    ): ResponseData<Gateway>

    suspend fun getInstallationGateway(
        installationId: String,
        gatewaySerial: String,
    ): Gateway

    suspend fun getInstallationGatewayStatus(
        installationId: String,
        gatewaySerial: String,
    ): GatewayStatus

    suspend fun getInstallationGatewayDevices(
        installationId: String,
        gatewaySerial: String,
    ): ResponseData<Device>
}

class DefaultEquipmentClient(
    private val httpClient: HttpClient,
    private val config: ViessmannClientConfig = ViessmannClientConfig(),
) : EquipmentClient {
    override suspend fun getInstallations(query: InstallationsQuery): ResponseData<Installation> =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/installations",
        ) {
            url.parameters.apply {
                appendIfNotNull(
                    "accessLevel" to query.accessLevel,
                    "cursor" to query.cursor,
                    "deviceBmuSerial" to query.deviceBmuSerial,
                    "deviceBoilerSerial" to query.deviceBoilerSerial,
                    "gatewaySerial" to query.gatewaySerial,
                    "includeAccessList" to query.includeAccessList,
                    "includeGateways" to query.includeGateways,
                    "limit" to query.limit,
                    "onlyWithLockedDevices" to query.onlyWithLockedDevices,
                    "onlyWithRemoteDiagnosticsSupport" to query.onlyWithRemoteDiagnosticsSupport,
                    "ownedByMaintainer" to query.ownedByMaintainer,
                    "serial" to query.serial,
                    "withoutViCareUser" to query.withoutViCareUser,
                )
            }
        }

    override suspend fun getInstallation(
        installationId: String,
        includeAccessList: Boolean?,
        includeGateways: Boolean?,
    ): Installation =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId",
        ) {
            url.parameters.apply {
                appendIfNotNull(
                    "includeAccessList" to includeAccessList,
                    "includeGateways" to includeGateways,
                )
            }
        }

    override suspend fun getInstallationsSummary(): GatewaysSummary =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/installations/summary",
        )

    override suspend fun getInstallationStatus(installationId: String): InstallationStatus =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId/status",
        )

    override suspend fun getGateways(query: GatewaysQuery): ResponseData<Gateway> =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/gateways",
        ) {
            url.parameters.apply {
                appendIfNotNull(
                    "bmuSerial" to query.bmuSerial,
                    "boilerSerial" to query.boilerSerial,
                    "cursor" to query.cursor,
                    "deviceRoles" to query.deviceRoles,
                    "gatewayType" to query.gatewayType,
                    "includeBorders" to query.includeBorders,
                    "includeDevices" to query.includeDevices,
                    "includeNbiotGateways" to query.includeNbiotGateways,
                    "limit" to query.limit,
                    "maxVersion" to query.maxVersion,
                    "minVersion" to query.minVersion,
                    "onlyRegistered" to query.onlyRegistered,
                    "ownedByMaintainer" to query.ownedByMaintainer,
                    "serial" to query.serial,
                )
            }
        }

    override suspend fun getGateway(
        gatewaySerial: String,
        includeDevices: Boolean?,
    ): Gateway =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/gateways/$gatewaySerial",
        ) {
            url.parameters.apply {
                appendIfNotNull("includeDevices", includeDevices)
            }
        }

    override suspend fun getGatewaysSummary(): GatewaysSummary =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/gateways/summary",
        )

    override suspend fun getGatewayStatus(gatewaySerial: String): GatewayStatus =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/gateways/$gatewaySerial/status",
        )

    override suspend fun getInstallationGateways(
        installationId: String,
        cursor: String?,
        includeDevices: Boolean?,
        limit: Int?,
    ): ResponseData<Gateway> =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId/gateways",
        ) {
            url.parameters.apply {
                appendIfNotNull(
                    "cursor" to cursor,
                    "includeDevices" to includeDevices,
                    "limit" to limit,
                )
            }
        }

    override suspend fun getInstallationGateway(
        installationId: String,
        gatewaySerial: String,
    ): Gateway =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial",
        )

    override suspend fun getInstallationGatewayStatus(
        installationId: String,
        gatewaySerial: String,
    ): GatewayStatus =
        httpClient.viessmannApiGetData(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial/status",
        )

    override suspend fun getInstallationGatewayDevices(
        installationId: String,
        gatewaySerial: String,
    ): ResponseData<Device> =
        httpClient.viessmannApiGet(
            config = config,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial/devices",
        )
}
