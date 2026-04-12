package xyz.dussim.viessmann.api.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateResponse(
    @SerialName("x-uuid")
    val xUuid: String,
    val roles: String,
)

@Serializable
data class AuthErrorResponse(
    val error: String,
)

@Serializable
data class CsrfJwk(
    val e: String,
    val kty: String,
    val alg: String,
    val n: String,
    val use: String,
    val kid: String,
)

@Serializable
data class CsrfJwksResponse(
    val keys: List<CsrfJwk>,
)

@Serializable
data class CsrfTokenRequest(
    val appId: String,
)

@Serializable
data class CsrfTokenResponse(
    val token: String,
)

@Serializable
data class SamlValidationError(
    val statusCode: Int,
    val errorType: String,
    val message: String,
    val validationErrors: List<SamlValidationErrorDetail>,
)

@Serializable
data class SamlValidationErrorDetail(
    val path: String,
    val type: String,
    val message: String,
)

@Serializable
data class ForbiddenError(
    val statusCode: Int,
    val errorType: String,
    val message: String,
)
