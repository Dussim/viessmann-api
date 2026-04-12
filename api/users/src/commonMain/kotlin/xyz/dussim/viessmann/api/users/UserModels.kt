package xyz.dussim.viessmann.api.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import xyz.dussim.viessmann.api.enums.Gender

@Serializable
data class CheckPasswordRequest(
    val password: String,
    val type: UserType,
)

@Serializable
data class CheckPasswordResponse(
    val valid: Boolean,
    val errors: List<Map<String, String>>? = null,
)

@Serializable
data class CreateConsumerRequest(
    val loginId: String,
    val documents: List<DocumentAcceptance>? = null,
    val optIn: Boolean,
    val appId: String? = null,
    val languageCode: String,
    val gender: Gender? = null,
    val birthDate: String? = null,
    val name: ConsumerName? = null,
    val address: ConsumerAddress,
    val contacts: ConsumerContacts? = null,
    val password: String? = null,
    val invitationId: String? = null,
    val redirectLink: String? = null,
    val token: String? = null,
)

@Serializable
data class DocumentAcceptance(
    val id: String,
    val accepted: Boolean,
)

@Serializable
data class ConsumerName(
    val title: String? = null,
    val firstName: String,
    val familyName: String,
)

@Serializable
data class ConsumerAddress(
    val countryCode: String,
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val locality: String? = null,
    val addressline1: String? = null,
    val addressline2: String? = null,
    val dwellingNumber: String? = null,
    val postOfficeBoxText: String? = null,
    val postOfficeBoxNumber: Int? = null,
    val region: String? = null,
)

@Serializable
data class ConsumerContacts(
    val telephone: String? = null,
    val telefax: String? = null,
    val mobile: String? = null,
)

@Serializable
data class CreateConsumerResponse(
    val data: CreatedUser,
)

@Serializable
data class CreatedUser(
    val id: String,
    val type: String,
    val attributes: UserAttributes,
)

@Serializable
data class UserAttributes(
    val loginId: String? = null,
    val address: JsonObject? = null,
    val clientExtId: String? = null,
    val languageCode: String? = null,
    val userState: String? = null,
    val name: UserName? = null,
    val validity: Validity? = null,
    val contacts: UserContacts? = null,
    val properties: List<UserProperty>? = null,
    @SerialName("_id")
    val internalId: String? = null,
    val roles: List<String>? = null,
)

@Serializable
data class UserName(
    val title: String? = null,
    val firstName: String? = null,
    val familyName: String? = null,
)

@Serializable
data class Validity(
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class UserContacts(
    val telephone: String? = null,
    val telefax: String? = null,
    val mobile: String? = null,
)

@Serializable
data class UserProperty(
    val name: String? = null,
    val value: String? = null,
)

@Serializable
data class ValidateAddressRequest(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String,
)

@Serializable
data class ValidateAddressResponse(
    val isValid: Boolean,
    val alternatives: List<AddressAlternative>? = null,
)

@Serializable
data class AddressAlternative(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String,
)
