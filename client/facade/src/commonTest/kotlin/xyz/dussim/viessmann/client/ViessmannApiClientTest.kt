package xyz.dussim.viessmann.client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.installViessmannClient

class ViessmannApiClientTest :
    FunSpec({
        test("creates facade from existing http client") {
            val config = ViessmannClientConfig(apiBaseUrl = "https://example.com")
            val httpClient =
                HttpClient(MockEngine { respond("{}", headers = jsonHeaders()) }) {
                    installViessmannClient(config)
                }

            val facade = ViessmannApiClient.fromHttpClient(httpClient, config)

            facade.httpClient shouldBe httpClient
            facade.auth.shouldNotBeNull()
            facade.equipment.shouldNotBeNull()
            facade.features.shouldNotBeNull()
            facade.users.shouldNotBeNull()
        }
    })

private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
