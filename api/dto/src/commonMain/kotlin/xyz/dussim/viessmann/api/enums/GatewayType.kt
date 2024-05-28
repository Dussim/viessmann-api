package xyz.dussim.viessmann.api.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import xyz.dussim.viessmann.api.utils.EntryHolder
import xyz.dussim.viessmann.api.utils.InstanceFactory
import xyz.dussim.viessmann.api.utils.factories.GatewayTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GatewayTypeInstanceFactory
import xyz.dussim.viessmann.api.utils.instanceFactorySerializer
import xyz.dussim.viessmann.api.utils.strictInstanceFactorySerializer

/**
 * Represents the type of `Gateway`.
 *
 * A gateway can have one of the following types:
 * * [Vitoconnect]
 * * * [Vitoconnect.OPTO2]
 * * * [Vitoconnect.OPTO3]
 * * * [Vitoconnect.Optolink]
 * * * [Vitoconnect.OpenTherm]
 * * [Tcu]
 * * * [Tcu.V101]
 * * * [Tcu.V102]
 * * * [Tcu.V201]
 * * * [Tcu.V301]
 * * [Sa]
 * * * [Sa.V171]
 * * * [Sa.V171s]
 * * * [Sa.V180]
 * * * [Sa.V180NBIoT]
 * * * [Sa.V1800019WiFi]
 * * * [Sa.V180Lan]
 * * [WiFi]
 * * * [WiFi.SA0019]
 * * * [WiFi.SA0041]
 * * [Thermostat]
 * * * [Thermostat.Nest]
 * * * [Thermostat.Smart]
 * * [VitocomLan4]
 * * [Lancard]
 * * [OneBaseEvolveBox]
 * * [Unknown]
 * */
@Serializable(with = GatewayType.Serializer::class)
sealed interface GatewayType : ViessmannEnum {
    /**
     * Represents the strictly defined gateway types.
     *
     * This sealed class encompasses all known and valid gateway types,
     * excluding [Unknown]. It serves as a type-safe way to handle and
     * serialize/deserialize known gateway types.
     *
     * Subtypes of this class are the only valid instances of [GatewayType],
     * apart from [Unknown].
     */
    @Serializable(with = StrictSerializer::class)
    sealed class Strict(
        override val name: String,
    ) : GatewayType

    sealed class Vitoconnect(
        name: String,
    ) : Strict(name) {
        data object OPTO2 : Vitoconnect("VitoconnectOPTO2")

        data object OPTO3 : Vitoconnect("VitoconnectOPTO3")

        data object Optolink : Vitoconnect("VitoconnectOptolink")

        data object OpenTherm : Vitoconnect("VitoconnectOpenTherm")
    }

    sealed class Tcu(
        name: String,
    ) : Strict(name) {
        data object V101 : Tcu("TCU101")

        data object V102 : Tcu("TCU102")

        data object V201 : Tcu("TCU201")

        data object V301 : Tcu("TCU301")
    }

    sealed class Sa(
        name: String,
    ) : Strict(name) {
        data object V171 : Sa("SA171")

        data object V171s : Sa("SA171s")

        data object V180 : Sa("SA180")

        data object V180NBIoT : Sa("SA180NBIoT")

        data object V1800019WiFi : Sa("SA1800019WiFi")

        data object V180Lan : Sa("SA180Lan")
    }

    sealed class WiFi(
        name: String,
    ) : Strict(name) {
        data object SA0019 : WiFi("WiFi_SA0019")

        data object SA0041 : WiFi("WiFi_SA0041")
    }

    sealed class Thermostat(
        name: String,
    ) : Strict(name) {
        data object Nest : Thermostat("NestThermostat")

        data object Smart : Thermostat("SmartThermostat")
    }

    data object VitocomLan4 : Strict("VitocomLan4")

    data object Lancard : Strict("Lancard")

    data object OneBaseEvolveBox : Strict("One_Base_Evolve_Box")

    data object VitocontrolAPro : Strict("Vitocontrol_A_PRO")

    /**
     * Represents an unknown [GatewayType].
     *
     * This is a special value used to represent a value that is not
     * known to the library. The [name] of this value is the value provided by
     * the API.
     *
     * @param name the value returned by the API.
     */
    data class Unknown(
        override val name: String,
    ) : GatewayType,
        UnknownEnumValue

    companion object :
        InstanceFactory<GatewayType> by GatewayTypeInstanceFactory,
        EntryHolder<Strict> by GatewayTypeEntryHolder

    object Serializer : KSerializer<GatewayType> by instanceFactorySerializer(GatewayType)

    object StrictSerializer : KSerializer<Strict> by strictInstanceFactorySerializer<GatewayType, _>()
}
