package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.enums.AccessLevel
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.HeatingType
import xyz.dussim.viessmann.api.enums.InstallationType
import xyz.dussim.viessmann.api.enums.OwnershipType

@Serializable
data class Address(
    val street: String,
    val houseNumber: String,
    val zip: String,
    val city: String,
    val region: String,
    val country: String,
    val phoneNumber: String,
    val faxNumber: String,
    val geolocation: Geolocation,
)

@Serializable
data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
)

@Serializable
data class Installation(
    val id: Long,
    val description: String,
    val address: Address,
    val registeredAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val updatedAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val aggregatedStatus: AggregatedStatus,
    val servicedBy: String,
    val heatingType: HeatingType,
    val ownedByMaintainer: Boolean,
    val endUserWlanCommissioned: Boolean,
    val withoutViCareUser: Boolean,
    val installationType: InstallationType,
    val accessLevel: AccessLevel,
    val ownershipType: OwnershipType,
)

@Serializable
data class InstallationStrict(
    val id: Long,
    val description: String,
    val address: Address,
    val registeredAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val updatedAt: String, // TODO Consider using kotlinx.datetime.LocalDateTime for date-time fields
    val aggregatedStatus: AggregatedStatus.Strict,
    val servicedBy: String,
    val heatingType: HeatingType.Strict,
    val ownedByMaintainer: Boolean,
    val endUserWlanCommissioned: Boolean,
    val withoutViCareUser: Boolean,
    val installationType: InstallationType.Strict,
    val accessLevel: AccessLevel.Strict,
    val ownershipType: OwnershipType.Strict,
)
