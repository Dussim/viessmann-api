package xyz.dussim.viessmann.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import xyz.dussim.viessmann.client.auth.AuthClient
import xyz.dussim.viessmann.client.auth.DefaultAuthClient
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.createViessmannHttpClient
import xyz.dussim.viessmann.client.equipment.DefaultEquipmentClient
import xyz.dussim.viessmann.client.equipment.EquipmentClient
import xyz.dussim.viessmann.client.features.DefaultFeaturesClient
import xyz.dussim.viessmann.client.features.FeaturesClient
import xyz.dussim.viessmann.client.users.DefaultUsersClient
import xyz.dussim.viessmann.client.users.UsersClient

class ViessmannApiClient private constructor(
    val config: ViessmannClientConfig,
    val httpClient: HttpClient,
    val auth: AuthClient,
    val equipment: EquipmentClient,
    val features: FeaturesClient,
    val users: UsersClient,
) {
    fun close() {
        httpClient.close()
    }

    companion object {
        fun create(
            config: ViessmannClientConfig = ViessmannClientConfig(),
            engineFactory: HttpClientEngineFactory<*>? = null,
        ): ViessmannApiClient {
            val httpClient = createViessmannHttpClient(config, engineFactory)

            return ViessmannApiClient(
                config = config,
                httpClient = httpClient,
                auth = DefaultAuthClient(httpClient, config),
                equipment = DefaultEquipmentClient(httpClient, config),
                features = DefaultFeaturesClient(httpClient, config),
                users = DefaultUsersClient(httpClient, config),
            )
        }

        fun fromHttpClient(
            httpClient: HttpClient,
            config: ViessmannClientConfig = ViessmannClientConfig(),
        ): ViessmannApiClient =
            ViessmannApiClient(
                config = config,
                httpClient = httpClient,
                auth = DefaultAuthClient(httpClient, config),
                equipment = DefaultEquipmentClient(httpClient, config),
                features = DefaultFeaturesClient(httpClient, config),
                users = DefaultUsersClient(httpClient, config),
            )
    }
}
