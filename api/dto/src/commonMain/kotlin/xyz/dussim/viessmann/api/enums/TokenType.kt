package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.TokenTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.TokenTypeInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the token type.
 *
 * A token type can have one of the following values:
 * * [Invited]
 * * [Requested]
 * * [Unknown]
 */
@Serializable(with = TokenType.Serializer::class)
sealed interface TokenType : ViessmannEnum {
    /**
     * Represents the strictly defined token types.
     *
     * This sealed class encompasses all known and valid token types,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known token types.
     *
     * Subtypes of this class are the only valid instances of [TokenType],
     * apart from [Unknown].
     *
     * @property name The string representation of the token type.
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : TokenType

    data object Invited : Strict("Invited")

    data object Requested : Strict("Requested")

    /**
     * Represents an unknown [TokenType].
     *
     * This is a special value used to represent a token type that is not
     * known to the library. The [name] of this value is the token type provided by
     * the API.
     *
     * @param name the token type returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : TokenType,
        UnknownEnumValue

    companion object :
        InstanceFactory<TokenType> by TokenTypeInstanceFactory,
        EntryHolder<Strict> by TokenTypeEntryHolder

    object Serializer : KSerializer<TokenType> by instanceFactorySerializer(TokenType)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<TokenType, _>()
}
