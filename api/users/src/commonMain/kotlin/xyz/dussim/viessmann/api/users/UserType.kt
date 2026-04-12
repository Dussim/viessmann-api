package xyz.dussim.viessmann.api.users

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UserTypeSerializer::class)
sealed interface UserType {
    val name: String

    data object Customer : UserType {
        override val name: String = "customer"
    }

    data object Consumer : UserType {
        override val name: String = "consumer"
    }

    data class Unknown(
        override val name: String,
    ) : UserType

    companion object {
        fun valueOf(value: String): UserType =
            when (value) {
                Customer.name -> Customer
                Consumer.name -> Consumer
                else -> Unknown(value)
            }
    }
}

object UserTypeSerializer : KSerializer<UserType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UserType = UserType.valueOf(decoder.decodeString())

    override fun serialize(
        encoder: Encoder,
        value: UserType,
    ) {
        encoder.encodeString(value.name)
    }
}
