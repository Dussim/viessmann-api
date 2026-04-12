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
                GatewayType.Tcu10xVitodensSeS1,
                GatewayType.Tcu10xViAirVitoconnectV,
                GatewayType.Tcu10xVitopureEu,
                GatewayType.Tcu10xVitocal262060,
                GatewayType.Tcu10xVitoset,
                GatewayType.Scu1000,
                GatewayType.Tcu301Vitocal,
                GatewayType.Tcu301Vitocharge,
                GatewayType.Tcu301Vitodens,
                GatewayType.Tcu301Vitocrossal,
                GatewayType.Tcu10xDevBoards,
                GatewayType.Tcu301DevBoards,
                GatewayType.Infinity,
                GatewayType.Tcu10xVitodensSeNa,
                GatewayType.Tcu10xRac,
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
                "TCU10x_Vitodens_SE_S1" to GatewayType.Tcu10xVitodensSeS1,
                "TCU10x_ViAir_VitoconnectV" to GatewayType.Tcu10xViAirVitoconnectV,
                "TCU10x_Vitopure_EU" to GatewayType.Tcu10xVitopureEu,
                "TCU10x_Vitocal_262_060" to GatewayType.Tcu10xVitocal262060,
                "TCU10x_Vitoset" to GatewayType.Tcu10xVitoset,
                "SCU_1000" to GatewayType.Scu1000,
                "TCU301_Vitocal" to GatewayType.Tcu301Vitocal,
                "TCU301_Vitocharge" to GatewayType.Tcu301Vitocharge,
                "TCU301_Vitodens" to GatewayType.Tcu301Vitodens,
                "TCU301_Vitocrossal" to GatewayType.Tcu301Vitocrossal,
                "TCU10x_DevBoards" to GatewayType.Tcu10xDevBoards,
                "TCU301_DevBoards" to GatewayType.Tcu301DevBoards,
                "Infinity" to GatewayType.Infinity,
                "TCU10x_Vitodens_SE_NA" to GatewayType.Tcu10xVitodensSeNa,
                "TCU10x_RAC" to GatewayType.Tcu10xRac,
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
