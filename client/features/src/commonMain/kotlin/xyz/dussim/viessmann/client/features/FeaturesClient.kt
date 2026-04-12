package xyz.dussim.viessmann.client.features

import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.json.JsonObject
import xyz.dussim.viessmann.api.features.CommandExecutionResponse
import xyz.dussim.viessmann.api.features.FeatureFilterRequest
import xyz.dussim.viessmann.api.features.GatewayFeatureFilterRequest
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.ViessmannService
import xyz.dussim.viessmann.client.core.appendAllIfNotEmpty
import xyz.dussim.viessmann.client.core.appendIfNotNull
import xyz.dussim.viessmann.client.core.viessmannRequest
import xyz.dussim.viessmann.client.core.viessmannRequestData
import xyz.dussim.viessmann.feature.api.ViessmannFeature

data class FeatureQuery(
    val filter: List<String>? = null,
    val regex: String? = null,
    val skipDisabled: Boolean? = null,
    val skipNotReady: Boolean? = null,
)

data class GatewayFeatureQuery(
    val filter: List<String>? = null,
    val regex: String? = null,
    val skipDisabled: Boolean? = null,
    val skipNotReady: Boolean? = null,
    val includeDevicesFeatures: Boolean? = null,
)

interface FeaturesClient {
    suspend fun getInstallationFeatures(
        installationId: String,
        query: FeatureQuery = FeatureQuery(),
    ): List<ViessmannFeature>

    suspend fun getInstallationFeature(
        installationId: String,
        featureName: String,
    ): ViessmannFeature

    suspend fun filterInstallationFeatures(
        installationId: String,
        request: FeatureFilterRequest,
    ): List<ViessmannFeature>

    suspend fun getGatewayFeatures(
        installationId: String,
        gatewaySerial: String,
        query: GatewayFeatureQuery = GatewayFeatureQuery(),
    ): List<ViessmannFeature>

    suspend fun getGatewayFeature(
        installationId: String,
        gatewaySerial: String,
        featureName: String,
    ): ViessmannFeature

    suspend fun filterGatewayFeatures(
        installationId: String,
        gatewaySerial: String,
        request: GatewayFeatureFilterRequest,
    ): List<ViessmannFeature>

    suspend fun executeGatewayCommand(
        installationId: String,
        gatewaySerial: String,
        featureName: String,
        commandName: String,
        body: JsonObject,
    ): CommandExecutionResponse

    suspend fun getDeviceFeatures(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        query: FeatureQuery = FeatureQuery(),
    ): List<ViessmannFeature>

    suspend fun getDeviceFeature(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        featureName: String,
    ): ViessmannFeature

    suspend fun filterDeviceFeatures(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        request: FeatureFilterRequest,
    ): List<ViessmannFeature>

    suspend fun executeDeviceCommand(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        featureName: String,
        commandName: String,
        body: JsonObject,
    ): CommandExecutionResponse
}

class DefaultFeaturesClient(
    private val httpClient: HttpClient,
    private val config: ViessmannClientConfig = ViessmannClientConfig(),
) : FeaturesClient {
    override suspend fun getInstallationFeatures(
        installationId: String,
        query: FeatureQuery,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/features",
        ) {
            url.parameters.applyFeatureQuery(query)
        }

    override suspend fun getInstallationFeature(
        installationId: String,
        featureName: String,
    ): ViessmannFeature =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/features/$featureName",
        )

    override suspend fun filterInstallationFeatures(
        installationId: String,
        request: FeatureFilterRequest,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/iot/v2/features/installations/$installationId/features/filter",
        ) {
            setBody(request)
        }

    override suspend fun getGatewayFeatures(
        installationId: String,
        gatewaySerial: String,
        query: GatewayFeatureQuery,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/features",
        ) {
            url.parameters.applyGatewayFeatureQuery(query)
        }

    override suspend fun getGatewayFeature(
        installationId: String,
        gatewaySerial: String,
        featureName: String,
    ): ViessmannFeature =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/features/$featureName",
        )

    override suspend fun filterGatewayFeatures(
        installationId: String,
        gatewaySerial: String,
        request: GatewayFeatureFilterRequest,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/features/filter",
        ) {
            setBody(request)
        }

    override suspend fun executeGatewayCommand(
        installationId: String,
        gatewaySerial: String,
        featureName: String,
        commandName: String,
        body: JsonObject,
    ): CommandExecutionResponse =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/features/$featureName/commands/$commandName",
        ) {
            setBody(body)
        }

    override suspend fun getDeviceFeatures(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        query: FeatureQuery,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/devices/$deviceId/features",
        ) {
            url.parameters.applyFeatureQuery(query)
        }

    override suspend fun getDeviceFeature(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        featureName: String,
    ): ViessmannFeature =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Get,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/devices/$deviceId/features/$featureName",
        )

    override suspend fun filterDeviceFeatures(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        request: FeatureFilterRequest,
    ): List<ViessmannFeature> =
        httpClient.viessmannRequestData(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/devices/$deviceId/features/filter",
        ) {
            setBody(request)
        }

    override suspend fun executeDeviceCommand(
        installationId: String,
        gatewaySerial: String,
        deviceId: String,
        featureName: String,
        commandName: String,
        body: JsonObject,
    ): CommandExecutionResponse =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path =
                "/iot/v2/features/installations/$installationId/gateways/$gatewaySerial/devices/$deviceId/" +
                    "features/$featureName/commands/$commandName",
        ) {
            setBody(body)
        }
}

private fun ParametersBuilder.applyFeatureQuery(query: FeatureQuery) {
    appendAllIfNotEmpty("filter", query.filter)
    appendIfNotNull("regex", query.regex)
    appendIfNotNull("skipDisabled", query.skipDisabled)
    appendIfNotNull("skipNotReady", query.skipNotReady)
}

private fun ParametersBuilder.applyGatewayFeatureQuery(query: GatewayFeatureQuery) {
    appendAllIfNotEmpty("filter", query.filter)
    appendIfNotNull("regex", query.regex)
    appendIfNotNull("skipDisabled", query.skipDisabled)
    appendIfNotNull("skipNotReady", query.skipNotReady)
    appendIfNotNull("includeDevicesFeatures", query.includeDevicesFeatures)
}
