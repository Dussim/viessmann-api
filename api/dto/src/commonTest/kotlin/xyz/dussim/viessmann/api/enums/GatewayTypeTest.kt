package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class GatewayTypeTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                GatewayType.Vitoconnect.OPTO2,
                GatewayType.Vitoconnect.OPTO3,
                GatewayType.Vitoconnect.Optolink,
                GatewayType.Vitoconnect.OpenTherm,
                GatewayType.Tcu.V101,
                GatewayType.Tcu.V102,
                GatewayType.Tcu.V201,
                GatewayType.Tcu.V301,
                GatewayType.Sa.V171,
                GatewayType.Sa.V171s,
                GatewayType.Sa.V180,
                GatewayType.Sa.V180NBIoT,
                GatewayType.Sa.V1800019WiFi,
                GatewayType.Sa.V180Lan,
                GatewayType.WiFi.SA0019,
                GatewayType.WiFi.SA0041,
                GatewayType.Thermostat.Nest,
                GatewayType.Thermostat.Smart,
                GatewayType.VitocomLan4,
                GatewayType.Lancard,
                GatewayType.OneBaseEvolveBox,
                GatewayType.VitocontrolAPro,
            ) {
                GatewayType.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "VitoconnectOPTO2" to GatewayType.Vitoconnect.OPTO2,
                "VitoconnectOPTO3" to GatewayType.Vitoconnect.OPTO3,
                "VitoconnectOptolink" to GatewayType.Vitoconnect.Optolink,
                "VitoconnectOpenTherm" to GatewayType.Vitoconnect.OpenTherm,
                "TCU101" to GatewayType.Tcu.V101,
                "TCU102" to GatewayType.Tcu.V102,
                "TCU201" to GatewayType.Tcu.V201,
                "TCU301" to GatewayType.Tcu.V301,
                "SA171" to GatewayType.Sa.V171,
                "SA171s" to GatewayType.Sa.V171s,
                "SA180" to GatewayType.Sa.V180,
                "SA180NBIoT" to GatewayType.Sa.V180NBIoT,
                "SA1800019WiFi" to GatewayType.Sa.V1800019WiFi,
                "SA180Lan" to GatewayType.Sa.V180Lan,
                "WiFi_SA0019" to GatewayType.WiFi.SA0019,
                "WiFi_SA0041" to GatewayType.WiFi.SA0041,
                "NestThermostat" to GatewayType.Thermostat.Nest,
                "SmartThermostat" to GatewayType.Thermostat.Smart,
                "VitocomLan4" to GatewayType.VitocomLan4,
                "Lancard" to GatewayType.Lancard,
                "One_Base_Evolve_Box" to GatewayType.OneBaseEvolveBox,
                "Vitocontrol_A_PRO" to GatewayType.VitocontrolAPro,
            ) { (name, expected) ->
                GatewayType.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                GatewayType.entries,
            ) { entry ->
                GatewayType.valueOf(entry.name) shouldBe entry
            }
        }
    })
