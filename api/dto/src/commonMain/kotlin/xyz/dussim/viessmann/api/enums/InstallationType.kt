package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.InstallationTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.InstallationTypeInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the installation type.
 *
 * An installation type can have one of the following values:
 * * [Residential]
 * * [Commercial]
 * * [Unknown]
 */
@Serializable(with = InstallationType.Serializer::class)
sealed interface InstallationType : ViessmannEnum {
    /**
     * Represents the strictly defined installation types.
     *
     * This sealed class encompasses all known and valid installation types,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known installation types.
     *
     * Subtypes of this class are the only valid instances of [InstallationType],
     * apart from [Unknown].
     *
     * @property name The string representation of the installation type.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : InstallationType

    data object Residential : Strict("Residential")

    data object Commercial : Strict("Commercial")

    /**
     * Represents an unknown [InstallationType].
     *
     * This is a special value used to represent an installation type that is not
     * known to the library. The [name] of this value is the installation type provided by
     * the API.
     *
     * @param name the installation type returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : InstallationType,
        UnknownEnumValue

    companion object :
        InstanceFactory<InstallationType> by InstallationTypeInstanceFactory,
        EntryHolder<Strict> by InstallationTypeEntryHolder

    object Serializer : KSerializer<InstallationType> by instanceFactorySerializer(InstallationType)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<InstallationType, _>()
}
