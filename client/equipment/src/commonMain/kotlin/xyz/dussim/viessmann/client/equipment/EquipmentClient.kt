package xyz.dussim.viessmann.client.equipment

import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import xyz.dussim.viessmann.api.equipment.GatewayStatus
import xyz.dussim.viessmann.api.equipment.InstallationStatus
import xyz.dussim.viessmann.api.models.Device
import xyz.dussim.viessmann.api.models.Gateway
import xyz.dussim.viessmann.api.models.GatewaysSummary
import xyz.dussim.viessmann.api.models.Installation
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.ViessmannService
import xyz.dussim.viessmann.client.core.appendIfNotNull
import xyz.dussim.viessmann.client.core.viessmannRequest
import xyz.dussim.viessmann.client.core.viessmannRequestData

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
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations",
        ) {
            url.parameters.apply {
                appendIfNotNull("accessLevel", query.accessLevel)
                appendIfNotNull("cursor", query.cursor)
                appendIfNotNull("deviceBmuSerial", query.deviceBmuSerial)
                appendIfNotNull("deviceBoilerSerial", query.deviceBoilerSerial)
                appendIfNotNull("gatewaySerial", query.gatewaySerial)
                appendIfNotNull("includeAccessList", query.includeAccessList)
                appendIfNotNull("includeGateways", query.includeGateways)
                appendIfNotNull("limit", query.limit)
                appendIfNotNull("onlyWithLockedDevices", query.onlyWithLockedDevices)
                appendIfNotNull("onlyWithRemoteDiagnosticsSupport", query.onlyWithRemoteDiagnosticsSupport)
                appendIfNotNull("ownedByMaintainer", query.ownedByMaintainer)
                appendIfNotNull("serial", query.serial)
                appendIfNotNull("withoutViCareUser", query.withoutViCareUser)
            }
        }

    override suspend fun getInstallation(
        installationId: String,
        includeAccessList: Boolean?,
        includeGateways: Boolean?,
    ): Installation =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId",
        ) {
            url.parameters.apply {
                appendIfNotNull("includeAccessList", includeAccessList)
                appendIfNotNull("includeGateways", includeGateways)
            }
        }

    override suspend fun getInstallationsSummary(): GatewaysSummary =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/summary",
        )

    override suspend fun getInstallationStatus(installationId: String): InstallationStatus =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId/status",
        )

    override suspend fun getGateways(query: GatewaysQuery): ResponseData<Gateway> =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/gateways",
        ) {
            url.parameters.apply {
                appendIfNotNull("bmuSerial", query.bmuSerial)
                appendIfNotNull("boilerSerial", query.boilerSerial)
                appendIfNotNull("cursor", query.cursor)
                appendIfNotNull("deviceRoles", query.deviceRoles)
                appendIfNotNull("gatewayType", query.gatewayType)
                appendIfNotNull("includeBorders", query.includeBorders)
                appendIfNotNull("includeDevices", query.includeDevices)
                appendIfNotNull("includeNbiotGateways", query.includeNbiotGateways)
                appendIfNotNull("limit", query.limit)
                appendIfNotNull("maxVersion", query.maxVersion)
                appendIfNotNull("minVersion", query.minVersion)
                appendIfNotNull("onlyRegistered", query.onlyRegistered)
                appendIfNotNull("ownedByMaintainer", query.ownedByMaintainer)
                appendIfNotNull("serial", query.serial)
            }
        }

    override suspend fun getGateway(
        gatewaySerial: String,
        includeDevices: Boolean?,
    ): Gateway =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/gateways/$gatewaySerial",
        ) {
            url.parameters.apply {
                appendIfNotNull("includeDevices", includeDevices)
            }
        }

    override suspend fun getGatewaysSummary(): GatewaysSummary =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/gateways/summary",
        )

    override suspend fun getGatewayStatus(gatewaySerial: String): GatewayStatus =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/gateways/$gatewaySerial/status",
        )

    override suspend fun getInstallationGateways(
        installationId: String,
        cursor: String?,
        includeDevices: Boolean?,
        limit: Int?,
    ): ResponseData<Gateway> =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId/gateways",
        ) {
            url.parameters.apply {
                appendIfNotNull("cursor", cursor)
                appendIfNotNull("includeDevices", includeDevices)
                appendIfNotNull("limit", limit)
            }
        }

    override suspend fun getInstallationGateway(
        installationId: String,
        gatewaySerial: String,
    ): Gateway =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial",
        )

    override suspend fun getInstallationGatewayStatus(
        installationId: String,
        gatewaySerial: String,
    ): GatewayStatus =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial/status",
        )

    override suspend fun getInstallationGatewayDevices(
        installationId: String,
        gatewaySerial: String,
    ): ResponseData<Device> =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/equipment/installations/$installationId/gateways/$gatewaySerial/devices",
        )
}
