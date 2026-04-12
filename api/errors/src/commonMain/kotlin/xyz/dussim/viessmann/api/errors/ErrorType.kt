package xyz.dussim.viessmann.api.errors

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ErrorTypeSerializer::class)
sealed interface ErrorType {
    val name: String

    data object BadRequest : ErrorType {
        override val name: String = "BAD_REQUEST"
    }

    data object Unauthorized : ErrorType {
        override val name: String = "UNAUTHORIZED"
    }

    data object Forbidden : ErrorType {
        override val name: String = "FORBIDDEN"
    }

    data object NotAllowed : ErrorType {
        override val name: String = "NOT_ALLOWED"
    }

    data object ResourceNotFound : ErrorType {
        override val name: String = "RESOURCE_NOT_FOUND"
    }

    data object Conflict : ErrorType {
        override val name: String = "CONFLICT"
    }

    data object ValidationError : ErrorType {
        override val name: String = "VALIDATION_ERROR"
    }

    data object InternalServerError : ErrorType {
        override val name: String = "INTERNAL_SERVER_ERROR"
    }

    data object GatewayError : ErrorType {
        override val name: String = "GATEWAY_ERROR"
    }

    data class Unknown(
        override val name: String,
    ) : ErrorType

    companion object {
        fun valueOf(value: String): ErrorType =
            when (value) {
                BadRequest.name -> BadRequest
                Unauthorized.name -> Unauthorized
                Forbidden.name -> Forbidden
                NotAllowed.name -> NotAllowed
                ResourceNotFound.name -> ResourceNotFound
                Conflict.name -> Conflict
                ValidationError.name -> ValidationError
                InternalServerError.name -> InternalServerError
                GatewayError.name -> GatewayError
                else -> Unknown(value)
            }
    }
}

object ErrorTypeSerializer : KSerializer<ErrorType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ErrorType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ErrorType = ErrorType.valueOf(decoder.decodeString())

    override fun serialize(
        encoder: Encoder,
        value: ErrorType,
    ) {
        encoder.encodeString(value.name)
    }
}
