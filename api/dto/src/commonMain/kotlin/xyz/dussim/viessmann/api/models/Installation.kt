package xyz.dussim.viessmann.api.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.api.enums.AccessLevel
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.HeatingType
import xyz.dussim.viessmann.api.enums.InstallationType
import xyz.dussim.viessmann.api.enums.OwnershipType
import kotlin.time.Instant

@Serializable
data class Address(
    val street: String,
    val houseNumber: String,
    val zip: String,
    val city: String,
    val region: String?,
    val country: String,
    val phoneNumber: String?,
    val faxNumber: String?,
    val geolocation: Geolocation,
)

@Serializable
data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
)

@Serializable(with = InstallationSerializer::class)
interface Installation {
    val id: Long
    val description: String? // FIXME documentation says that this can NOT be null but it is for some devices :/
    val address: Address
    val registeredAt: Instant
    val updatedAt: Instant
    val aggregatedStatus: AggregatedStatus
    val servicedBy: String?
    val heatingType: HeatingType?
    val ownedByMaintainer: Boolean
    val endUserWlanCommissioned: Boolean
    val withoutViCareUser: Boolean
    val installationType: InstallationType
    val accessLevel: AccessLevel?
    val ownershipType: OwnershipType

    @Serializable
    data class Impl(
        override val id: Long,
        override val description: String?,
        override val address: Address,
        override val registeredAt: Instant,
        override val updatedAt: Instant,
        override val aggregatedStatus: AggregatedStatus,
        override val servicedBy: String?,
        override val heatingType: HeatingType?,
        override val ownedByMaintainer: Boolean,
        override val endUserWlanCommissioned: Boolean,
        override val withoutViCareUser: Boolean,
        override val installationType: InstallationType,
        override val accessLevel: AccessLevel?,
        override val ownershipType: OwnershipType,
    ) : Installation

    @Serializable
    data class Strict(
        override val id: Long,
        override val description: String?,
        override val address: Address,
        override val registeredAt: Instant,
        override val updatedAt: Instant,
        override val aggregatedStatus: AggregatedStatus.Strict,
        override val servicedBy: String?,
        override val heatingType: HeatingType.Strict?,
        override val ownedByMaintainer: Boolean,
        override val endUserWlanCommissioned: Boolean,
        override val withoutViCareUser: Boolean,
        override val installationType: InstallationType.Strict,
        override val accessLevel: AccessLevel.Strict?,
        override val ownershipType: OwnershipType.Strict,
    ) : Installation
}

object InstallationSerializer : KSerializer<Installation> {
    override val descriptor: SerialDescriptor = Installation.Impl.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Installation,
    ) {
        when (value) {
            is Installation.Impl -> Installation.Impl.serializer().serialize(encoder, value)
            is Installation.Strict -> Installation.Strict.serializer().serialize(encoder, value)
            else -> throw IllegalArgumentException("Unknown Installation type")
        }
    }

    override fun deserialize(decoder: Decoder): Installation = Installation.Impl.serializer().deserialize(decoder)
}
