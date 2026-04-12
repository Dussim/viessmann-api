package xyz.dussim.viessmann.client.core

import io.ktor.http.HttpStatusCode
import xyz.dussim.viessmann.api.errors.ViessmannApiError

open class ViessmannApiException(
    val statusCode: HttpStatusCode,
    val error: ViessmannApiError? = null,
    val responseBody: String? = null,
) : RuntimeException(buildMessage(statusCode, error, responseBody)) {
    companion object {
        private fun buildMessage(
            statusCode: HttpStatusCode,
            error: ViessmannApiError?,
            responseBody: String?,
        ): String {
            val message = error?.message ?: responseBody?.takeIf { it.isNotBlank() } ?: "HTTP ${statusCode.value}"
            return "Viessmann API request failed with ${statusCode.value} ${statusCode.description}: $message"
        }
    }
}

class MissingAccessTokenException : RuntimeException("No Viessmann access token is available for an authenticated request")

class ResponseDecodingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
