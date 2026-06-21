package xyz.dussim.viessmann.client.users

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import xyz.dussim.viessmann.api.users.CheckPasswordRequest
import xyz.dussim.viessmann.api.users.CheckPasswordResponse
import xyz.dussim.viessmann.api.users.CreateConsumerRequest
import xyz.dussim.viessmann.api.users.CreateConsumerResponse
import xyz.dussim.viessmann.api.users.ValidateAddressRequest
import xyz.dussim.viessmann.api.users.ValidateAddressResponse
import xyz.dussim.viessmann.client.core.ViessmannClientConfig
import xyz.dussim.viessmann.client.core.viessmannApiPost

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
        httpClient.viessmannApiPost(
            config = config,
            path = "/users/v1/users/check-user-password",
            authenticated = false,
        ) {
            setBody(request)
        }

    override suspend fun createConsumer(
        request: CreateConsumerRequest,
        forwarded: String,
    ): CreateConsumerResponse =
        httpClient.viessmannApiPost(
            config = config,
            path = "/users/v2/users/consumer",
            authenticated = false,
        ) {
            header(HttpHeaders.Forwarded, forwarded)
            setBody(request)
        }

    override suspend fun validateAddress(request: ValidateAddressRequest): ValidateAddressResponse =
        httpClient.viessmannApiPost(
            config = config,
            path = "/users/v2/validate-address",
            authenticated = false,
        ) {
            setBody(request)
        }
}
