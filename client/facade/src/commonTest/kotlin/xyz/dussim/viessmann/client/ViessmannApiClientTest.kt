package xyz.dussim.viessmann.client

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.respond
import xyz.dussim.viessmann.client.testing.jsonHeaders
import xyz.dussim.viessmann.client.testing.testViessmannClientConfig
import xyz.dussim.viessmann.client.testing.testViessmannHttpClient

val ViessmannApiClientTest by testSuite {
    test("creates facade from existing http client") {
        val config = testViessmannClientConfig(accessToken = null)
        val httpClient = testViessmannHttpClient(config) { respond("{}", headers = jsonHeaders()) }

        val facade = ViessmannApiClient.fromHttpClient(httpClient, config)

        facade.httpClient shouldBe httpClient
        facade.auth.shouldNotBeNull()
        facade.equipment.shouldNotBeNull()
        facade.features.shouldNotBeNull()
        facade.users.shouldNotBeNull()
    }
}
