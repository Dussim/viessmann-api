package xyz.dussim.viessmann.client.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

fun createViessmannHttpClient(
    config: ViessmannClientConfig = ViessmannClientConfig(),
    engineFactory: HttpClientEngineFactory<*>? = null,
): HttpClient =
    if (engineFactory == null) {
        HttpClient {
            installViessmannClient(config)
        }
    } else {
        HttpClient(engineFactory) {
            installViessmannClient(config)
        }
    }

fun io.ktor.client.HttpClientConfig<*>.installViessmannClient(config: ViessmannClientConfig) {
    expectSuccess = false
    followRedirects = false

    install(ContentNegotiation) {
        json(config.json)
    }

    install(DefaultRequest) {
        config.defaultHeaders.forEach { (name, value) -> header(name, value) }
        header(HttpHeaders.Accept, ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }
}
