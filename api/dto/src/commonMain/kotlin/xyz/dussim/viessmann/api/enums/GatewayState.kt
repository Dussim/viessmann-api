package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.GatewayStateEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GatewayStateInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the state of a `Gateway`.
 *
 * A gateway can have one of the following states:
 * * [Produced]
 * * [Registered]
 * * [Unknown]
 * */
@Serializable(with = GatewayState.Serializer::class)
sealed interface GatewayState : ViessmannEnum {
    /**
     * Represents the strictly defined gateway states.
     *
     * This sealed class encompasses all known and valid gateway states,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known gateway states.
     *
     * Subtypes of this class are the only valid instances of [GatewayState],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : GatewayState

    data object Produced : Strict("Produced")

    data object Registered : Strict("Registered")

    /**
     * Represents an unknown [GatewayState].
     *
     * This is a special value used to represent a state that is not
     * known to the library. The [name] of this value is the state provided by
     * the API.
     *
     * @param name the state returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : GatewayState,
        UnknownEnumValue

    companion object :
        InstanceFactory<GatewayState> by GatewayStateInstanceFactory,
        EntryHolder<Strict> by GatewayStateEntryHolder

    object Serializer : KSerializer<GatewayState> by instanceFactorySerializer(GatewayState)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<GatewayState, _>()
}
