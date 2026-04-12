package xyz.dussim.viessmann.api.errors

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ViessmannApiError(
    val viErrorId: String? = null,
    val statusCode: Int,
    val errorType: ErrorType,
    val message: String,
    val extendedPayload: JsonObject? = null,
    val validationErrors: List<ValidationError>? = null,
)
