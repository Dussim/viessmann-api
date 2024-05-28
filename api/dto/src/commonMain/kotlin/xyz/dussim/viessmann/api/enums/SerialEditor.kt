package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.SerialEditorEntryHolder
import xyz.dussim.viessmann.api.utils.factories.SerialEditorInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the serial editor.
 *
 * The serial editor is a special permission that needs to be assigned to a user to
 * edit the serial number of a gateway. There are three types of editors:
 * * [User]
 * * [DeviceCommunication]
 * * [Supporter]
 * * [Unknown]
 */
@Serializable(with = SerialEditor.Serializer::class)
sealed interface SerialEditor : ViessmannEnum {
    /**
     * Represents the strictly defined serial editors.
     *
     * This sealed class encompasses all known and valid serial editors,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known serial editors.
     *
     * Subtypes of this class are the only valid instances of [SerialEditor],
     * apart from [Unknown].
     *
     * @property name The string representation of the serial editor.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : SerialEditor

    data object User : Strict("User")

    data object DeviceCommunication : Strict("DeviceCommunication")

    data object Supporter : Strict("Supporter")

    /**
     * Represents an unknown [SerialEditor].
     *
     * This is a special value used to represent a serial editor that is not
     * known to the library. The [name] of this value is the editor provided by
     * the API.
     *
     * @property name serial editor as returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : SerialEditor,
        UnknownEnumValue

    companion object :
        InstanceFactory<SerialEditor> by SerialEditorInstanceFactory,
        EntryHolder<Strict> by SerialEditorEntryHolder

    object Serializer : KSerializer<SerialEditor> by instanceFactorySerializer(SerialEditor)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<SerialEditor, _>()
}
