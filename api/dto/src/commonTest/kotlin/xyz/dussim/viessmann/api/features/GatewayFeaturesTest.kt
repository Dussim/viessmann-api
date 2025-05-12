package xyz.dussim.viessmann.api.features

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.ParseSpec

class GatewayFeaturesTest :
    ParseSpec({ json ->
        val features =
            json
                .decodeFromString<ResponseData<ViessmannFeature.Gateway>>(
                    readFileFromResources("features/gateway/features_gateway.json"),
                ).data
                .let(::ViessmannFeatureResolver)

        val features7637415001735184 =
            json
                .decodeFromString<ResponseData<ViessmannFeature.Gateway>>(
                    readFileFromResources("features/gateway/features_7637415001735184.json"),
                ).data
                .let(::ViessmannFeatureResolver)

        context("Decorates with GatewayFirmwareViessmannFeature") {
            val feature = features[GatewayFirmwareViessmannFeature.factory]
            assertSoftly {
                feature.version shouldBe "0506.2307.0007"
                feature.updateStatus shouldBe GatewayFirmwareViessmannFeature.UpdateStatus.Idle
                feature.libraryVersion shouldBe "1.0.3"
                feature.libraryType shouldBe "uDCE"

                feature.update.url.regEx shouldBe "((ftp|https?):\\/)?\\/?([^:\\/\\s]+)(:[0-9]+)?((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?"
                feature.update.version.regEx shouldBe "^([0-9]+)\\.([0-9]+)\\.([0-9]+)((\\.[0-9]+)?)(?:(-[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-\\-.]+)?$"
            }
        }

        context("Decorates with GatewayStatusFeature") {
            val feature = features[GatewayStatusFeature.factory]
            assertSoftly {
                feature.online shouldBe true
                // Verify the reboot command exists (we can't test execution)
                feature.reboot.shouldNotBeNull()
            }
        }

        context("Decorates with GatewayDevicesFeature") {
            val feature = features[GatewayDevicesFeature.factory]
            assertSoftly {
                feature.devices shouldHaveSize 3

                val device0 = feature.devices[0]
                device0.id shouldBe "0"
                device0.fingerprint shouldBe "0019_wifi;7956226000137183;0002.0525.2314.0643;0000.0000.0000.0000"
                device0.modelId shouldBe "E3_Vitodens_200_0821"
                device0.name shouldBe "E3_Vitodens_200_0821"
                device0.type shouldBe "heating"
                device0.roles shouldHaveSize 16
                device0.status shouldBe "online"

                val deviceTCU = feature.devices[1]
                deviceTCU.id shouldBe "TCU"
                deviceTCU.type shouldBe "tcu"

                val deviceRoomControl = feature.devices[2]
                deviceRoomControl.id shouldBe "RoomControl-1"
                deviceRoomControl.type shouldBe "roomControl"
            }
        }

        context("Decorates with GatewayWifiFeature") {
            val feature = features[GatewayWifiFeature.factory]
            assertSoftly {
                feature.strength shouldBe -52
            }
        }

        context("Decorates with GatewayModeFeature") {
            val feature = features[GatewayModeFeature.factory]
            assertSoftly {
                feature.mode shouldBe "wifi"
            }
        }

        context("Decorates with GatewayRemoteDiagnosticsFeature") {
            val feature = features[GatewayRemoteDiagnosticsFeature.factory]
            assertSoftly {
                feature.status shouldBe "inactive"
                // Verify the commands exist (we can't test execution)
                feature.open.shouldNotBeNull()
                feature.close.shouldNotBeNull()
            }
        }

        context("Decorates with GatewayBmuConnectionFeature") {
            val feature = features7637415001735184[GatewayBmuConnectionFeature.factory]
            assertSoftly {
                feature.status shouldBe "OK"
            }
        }

        // The following features have no properties or commands to test
        // but we verify they can be accessed
        context("Decorates with BatteryOverviewFeature") {
            val feature = features[BatteryOverviewFeature.factory]
            // No properties to test
        }

        context("Decorates with PhotovoltaicOverviewFeature") {
            val feature = features[PhotovoltaicOverviewFeature.factory]
            // No properties to test
        }

        context("Decorates with PowerLimitationOverviewFeature") {
            val feature = features7637415001735184[PowerLimitationOverviewFeature.factory]
            // No properties to test
        }

        context("Decorates with ThermalCockpitFeature") {
            val feature = features[ThermalCockpitFeature.factory]
            // No properties to test
        }
    })
