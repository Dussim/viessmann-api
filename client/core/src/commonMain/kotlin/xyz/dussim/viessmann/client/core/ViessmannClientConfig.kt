package xyz.dussim.viessmann.client.core

import kotlinx.serialization.json.Json

val defaultViessmannJson =
    Json {
        explicitNulls = false
        encodeDefaults = false
        ignoreUnknownKeys = true
        prettyPrint = true
    }

data class ViessmannClientConfig(
    val apiBaseUrl: String = "https://api.viessmann-climatesolutions.com",
    val iamBaseUrl: String = "https://iam.viessmann-climatesolutions.com",
    val defaultHeaders: Map<String, String> = emptyMap(),
    val accessTokenProvider: suspend () -> String? = { null },
    val json: Json = defaultViessmannJson,
)

enum class ViessmannService(
    internal val baseUrl: ViessmannClientConfig.() -> String,
) {
    Api({ apiBaseUrl }),
    Iam({ iamBaseUrl }),
}

internal fun ViessmannClientConfig.resolveUrl(
    service: ViessmannService,
    path: String,
): String = service.baseUrl(this).trimEnd('/') + "/" + path.trimStart('/')
