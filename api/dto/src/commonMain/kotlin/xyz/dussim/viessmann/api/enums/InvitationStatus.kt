package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.InvitationStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.InvitationStatusInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the invitation status.
 *
 * An invitation can have one of the following statuses:
 * * [Pending]
 * * [Accepted]
 * * [Rejected]
 * * [Canceled]
 * * [Expired]
 * * [Terminated]
 * * [Unknown]
 */
@Serializable(with = InvitationStatus.Serializer::class)
sealed interface InvitationStatus : ViessmannEnum {
    /**
     * Represents the strictly defined invitation statuses.
     *
     * This sealed class encompasses all known and valid invitation statuses,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known invitation statuses.
     *
     * Subtypes of this class are the only valid instances of [InvitationStatus],
     * apart from [Unknown].
     *
     * @property name The string representation of the invitation status.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : InvitationStatus

    data object Pending : Strict("Pending")

    data object Accepted : Strict("Accepted")

    data object Rejected : Strict("Rejected")

    data object Canceled : Strict("Canceled")

    data object Expired : Strict("Expired")

    data object Terminated : Strict("Terminated")

    /**
     * Represents an unknown [InvitationStatus].
     *
     * This is a special value used to represent an invitation status that is not
     * known to the library. The [name] of this value is the status provided by
     * the API.
     *
     * @property name invitation status as returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : InvitationStatus,
        UnknownEnumValue

    companion object :
        InstanceFactory<InvitationStatus> by InvitationStatusInstanceFactory,
        EntryHolder<Strict> by InvitationStatusEntryHolder

    object Serializer : KSerializer<InvitationStatus> by instanceFactorySerializer(InvitationStatus)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<InvitationStatus, _>()
}
