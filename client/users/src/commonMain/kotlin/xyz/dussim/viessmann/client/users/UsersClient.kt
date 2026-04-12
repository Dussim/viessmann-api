package xyz.dussim.viessmann.client.users

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import xyz.dussim.viessmann.api.users.CheckPasswordRequest
import xyz.dussim.viessmann.api.users.CheckPasswordResponse
import xyz.dussim.viessmann.api.users.CreateConsumerRequest
import xyz.dussim.viessmann.api.users.CreateConsumerResponse
import xyz.dussim.viessmann.api.users.ValidateAddressRequest
import xyz.dussim.viessmann.api.users.ValidateAddressResponse
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.ViessmannService
import xyz.dussim.viessmann.client.core.viessmannRequest
import xyz.dussim.viessmann.client.core.viessmannRequestData

interface UsersClient {
    suspend fun checkUserPassword(request: CheckPasswordRequest): CheckPasswordResponse

    suspend fun createConsumer(
        request: CreateConsumerRequest,
        forwarded: String,
    ): CreateConsumerResponse

    suspend fun validateAddress(request: ValidateAddressRequest): ValidateAddressResponse
}

class DefaultUsersClient(
    private val httpClient: HttpClient,
    private val config: ViessmannClientConfig = ViessmannClientConfig(),
) : UsersClient {
    override suspend fun checkUserPassword(request: CheckPasswordRequest): CheckPasswordResponse =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/users/v1/users/check-user-password",
            authenticated = false,
        ) {
            setBody(request)
        }

    override suspend fun createConsumer(
        request: CreateConsumerRequest,
        forwarded: String,
    ): CreateConsumerResponse =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/users/v2/users/consumer",
            authenticated = false,
        ) {
            header(HttpHeaders.Forwarded, forwarded)
            setBody(request)
        }

    override suspend fun validateAddress(request: ValidateAddressRequest): ValidateAddressResponse =
        httpClient.viessmannRequest(
            config = config,
            service = ViessmannService.Api,
            method = HttpMethod.Post,
            path = "/users/v2/validate-address",
            authenticated = false,
        ) {
            setBody(request)
        }
}
