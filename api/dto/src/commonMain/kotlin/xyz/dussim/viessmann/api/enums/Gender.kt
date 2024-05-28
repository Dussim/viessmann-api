package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.GenderEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GenderInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the gender of a user.
 *
 * A user can have one of the following genders:
 * * [Male]
 * * [Female]
 * * [Other]
 * * [Unknown]
 * */
@Serializable(with = Gender.Serializer::class)
sealed interface Gender : ViessmannEnum {
    /**
     * Represents the strictly defined genders.
     *
     * This sealed class encompasses all known and valid genders,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known genders.
     *
     * Subtypes of this class are the only valid instances of [Gender],
     * apart from [Unknown].
     *
     * @property name The string representation of the gender.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : Gender

    data object Male : Strict("MALE")

    data object Female : Strict("FEMALE")

    data object Other : Strict("OTHER")

    /**
     * Represents an unknown [TargetRealm].
     *
     * This is a special value used to represent a target realm that is not
     * known to the library. The [name] of this value is the target realm provided by
     * the API.
     *
     * @param name the target realm returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : Gender,
        UnknownEnumValue

    companion object :
        InstanceFactory<Gender> by GenderInstanceFactory,
        EntryHolder<Strict> by GenderEntryHolder

    object Serializer : KSerializer<Gender> by instanceFactorySerializer(Gender)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<Gender, _>()
}
