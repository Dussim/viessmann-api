package xyz.dussim.viessmann.client.testing

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.installViessmannClient

fun testViessmannClientConfig(
    apiBaseUrl: String = "https://example.com",
    accessToken: String? = "token-123",
): ViessmannClientConfig =
    ViessmannClientConfig(
        apiBaseUrl = apiBaseUrl,
        accessTokenProvider = { accessToken },
    )

fun testViessmannHttpClient(
    config: ViessmannClientConfig,
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient =
    HttpClient(MockEngine(handler)) {
        installViessmannClient(config)
    }

fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
