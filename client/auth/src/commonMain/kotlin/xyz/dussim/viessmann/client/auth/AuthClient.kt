package xyz.dussim.viessmann.client.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.auth.CsrfJwksResponse
import xyz.dussim.viessmann.api.auth.CsrfTokenRequest
import xyz.dussim.viessmann.api.auth.CsrfTokenResponse
import xyz.dussim.viessmann.api.auth.ValidateResponse
import xyz.dussim.viessmann.client.core.RawViessmannResponse
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.appendIfNotNull
import xyz.dussim.viessmann.client.core.viessmannApiGet
import xyz.dussim.viessmann.client.core.viessmannApiPost
import xyz.dussim.viessmann.client.core.viessmannApiRawGet
import xyz.dussim.viessmann.client.core.viessmannApiRawPost

@Serializable
data class RoutePolicyValidationRequest(
    val forwardedHost: String,
    val forwardedMethod: String,
    val forwardedProto: String,
    val forwardedUri: String,
)

@Serializable
data class SamlSsoRequest(
    val originalHost: String,
    val referer: String? = null,
    val appId: String? = null,
    val registrationLink: String? = null,
    val forgotUsername: String? = null,
    val redirectUrl: String? = null,
    val iamBackgroundImage: String? = null,
)

@Serializable
data class SamlValidationRequest(
    val originalHost: String,
)

@Serializable
data class LogoutRequest(
    val originalHost: String,
    val referer: String? = null,
    val redirectUrl: String? = null,
)

interface AuthClient {
    suspend fun validate(): ValidateResponse

    suspend fun validateSession(): ValidateResponse

    suspend fun validateSessionForDotnet(): ValidateResponse

    suspend fun validateWithRouteBasedPolicies(request: RoutePolicyValidationRequest): ValidateResponse

    suspend fun getCsrfJwks(): CsrfJwksResponse

    suspend fun createCsrfToken(
        referer: String,
        request: CsrfTokenRequest,
    ): CsrfTokenResponse

    suspend fun getSamlSsoRequest(request: SamlSsoRequest): RawViessmannResponse

    suspend fun validateSamlResponseGet(request: SamlValidationRequest): RawViessmannResponse

    suspend fun validateSamlResponsePost(
        request: SamlValidationRequest,
        body: String,
        contentType: ContentType = ContentType.Application.FormUrlEncoded,
    ): RawViessmannResponse

    suspend fun logout(request: LogoutRequest): RawViessmannResponse
}

class DefaultAuthClient(
    private val httpClient: HttpClient,
    private val config: ViessmannClientConfig = ViessmannClientConfig(),
) : AuthClient {
    override suspend fun validate(): ValidateResponse =
        httpClient.viessmannApiGet(
            config = config,
            path = "/auth/v1/validate",
        )

    override suspend fun validateSession(): ValidateResponse =
        httpClient.viessmannApiGet(
            config = config,
            path = "/auth/v1/validate-session",
        )

    override suspend fun validateSessionForDotnet(): ValidateResponse =
        httpClient.viessmannApiGet(
            config = config,
            path = "/auth/v1/validate-session-for-dotnet",
        )

    override suspend fun validateWithRouteBasedPolicies(request: RoutePolicyValidationRequest): ValidateResponse =
        httpClient.viessmannApiGet(
            config = config,
            path = "/auth/v1/validate-with-route-based-policies",
            authenticated = false,
        ) {
            header("x-forwarded-host", request.forwardedHost)
            header("x-forwarded-method", request.forwardedMethod)
            header("x-forwarded-proto", request.forwardedProto)
            header("x-forwarded-uri", request.forwardedUri)
        }

    override suspend fun getCsrfJwks(): CsrfJwksResponse =
        httpClient.viessmannApiGet(
            config = config,
            path = "/auth/v1/saml/csrf/jwks.json",
            authenticated = false,
        )

    override suspend fun createCsrfToken(
        referer: String,
        request: CsrfTokenRequest,
    ): CsrfTokenResponse =
        httpClient.viessmannApiPost(
            config = config,
            path = "/auth/v1/saml/csrf",
            authenticated = false,
        ) {
            header(HttpHeaders.Referrer, referer)
            setBody(request)
        }

    override suspend fun getSamlSsoRequest(request: SamlSsoRequest): RawViessmannResponse =
        httpClient.viessmannApiRawGet(
            config = config,
            path = "/auth/v1/saml/sso/request",
            authenticated = false,
        ) {
            header("x-original-host", request.originalHost)
            header(HttpHeaders.Referrer, request.referer)
            url.parameters.apply {
                appendIfNotNull("appId", request.appId)
                appendIfNotNull("RegistrationLink", request.registrationLink)
                appendIfNotNull("ForgotUsername", request.forgotUsername)
                appendIfNotNull("redirectUrl", request.redirectUrl)
                appendIfNotNull("iambgimg", request.iamBackgroundImage)
            }
        }

    override suspend fun validateSamlResponseGet(request: SamlValidationRequest): RawViessmannResponse =
        httpClient.viessmannApiRawGet(
            config = config,
            path = "/auth/v1/saml/sso/validate",
            authenticated = false,
        ) {
            header("x-original-host", request.originalHost)
        }

    override suspend fun validateSamlResponsePost(
        request: SamlValidationRequest,
        body: String,
        contentType: ContentType,
    ): RawViessmannResponse =
        httpClient.viessmannApiRawPost(
            config = config,
            path = "/auth/v1/saml/sso/validate",
            authenticated = false,
        ) {
            header("x-original-host", request.originalHost)
            header(HttpHeaders.ContentType, contentType.toString())
            setBody(body)
        }

    override suspend fun logout(request: LogoutRequest): RawViessmannResponse =
        httpClient.viessmannApiRawPost(
            config = config,
            path = "/auth/v1/saml/logout",
            authenticated = false,
        ) {
            header("x-original-host", request.originalHost)
            header(HttpHeaders.Referrer, request.referer)
            url.parameters.apply {
                appendIfNotNull("redirectUrl", request.redirectUrl)
            }
        }
}
