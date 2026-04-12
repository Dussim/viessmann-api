package xyz.dussim.viessmann.client.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import xyz.dussim.viessmann.api.errors.ViessmannApiError

@Serializable
data class NullableResponseEnvelope<T>(
    val data: T? = null,
)

suspend inline fun <reified T> HttpClient.viessmannRequest(
    config: ViessmannClientConfig,
    service: ViessmannService,
    method: HttpMethod,
    path: String,
    authenticated: Boolean = true,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): T {
    val response =
        executeViessmannRequest(
            config = config,
            service = service,
            method = method,
            path = path,
            authenticated = authenticated,
            block = block,
        )
    return decodeResponse(response, config)
}

suspend inline fun <reified T> HttpClient.viessmannRequestData(
    config: ViessmannClientConfig,
    service: ViessmannService,
    method: HttpMethod,
    path: String,
    authenticated: Boolean = true,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): T {
    val response =
        executeViessmannRequest(
            config = config,
            service = service,
            method = method,
            path = path,
            authenticated = authenticated,
            block = block,
        )
    return decodeEnvelope(response, config)
}

suspend fun HttpClient.viessmannRawRequest(
    config: ViessmannClientConfig,
    service: ViessmannService,
    method: HttpMethod,
    path: String,
    authenticated: Boolean = true,
    block: HttpRequestBuilder.() -> Unit = {},
): RawViessmannResponse {
    val response =
        executeViessmannRequest(
            config = config,
            service = service,
            method = method,
            path = path,
            authenticated = authenticated,
            validateStatus = false,
            block = block,
        )
    val body = response.bodyAsText()
    return RawViessmannResponse(response.status.value, response.headers.entries().associate { it.key to it.value }, body)
}

data class RawViessmannResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String,
)

suspend fun HttpClient.executeViessmannRequest(
    config: ViessmannClientConfig,
    service: ViessmannService,
    method: HttpMethod,
    path: String,
    authenticated: Boolean,
    validateStatus: Boolean = true,
    block: HttpRequestBuilder.() -> Unit,
): HttpResponse {
    val response =
        request {
            this.method = method
            url.takeFrom(config.resolveUrl(service, path))
            if (authenticated) {
                val token = config.accessTokenProvider() ?: throw MissingAccessTokenException()
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            block()
        }

    if (validateStatus && !response.status.isSuccess()) {
        throw response.toApiException(config)
    }

    return response
}

suspend inline fun <reified T> decodeResponse(
    response: HttpResponse,
    config: ViessmannClientConfig,
): T =
    try {
        response.body()
    } catch (cause: Throwable) {
        throw ResponseDecodingException(
            "Failed to decode response body as ${T::class.simpleName ?: "response"}",
            cause,
        )
    }

suspend inline fun <reified T> decodeEnvelope(
    response: HttpResponse,
    config: ViessmannClientConfig,
): T {
    val envelope =
        try {
            response.body<NullableResponseEnvelope<T>>()
        } catch (cause: Throwable) {
            throw ResponseDecodingException(
                "Failed to decode response envelope as ${T::class.simpleName ?: "response"}",
                cause,
            )
        }

    return envelope.data
        ?: throw ResponseDecodingException("Response envelope did not contain a data payload")
}

suspend fun HttpResponse.toApiException(config: ViessmannClientConfig): ViessmannApiException {
    val body = bodyAsText()
    val error =
        try {
            config.json.decodeFromString(ViessmannApiError.serializer(), body)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    return ViessmannApiException(status, error, body)
}
