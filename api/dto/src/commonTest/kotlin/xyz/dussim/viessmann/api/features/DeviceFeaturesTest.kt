package xyz.dussim.viessmann.api.features

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.api.testing.ParseSpec
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.FeatureResolver
import xyz.dussim.viessmann.feature.api.StringValue

class DeviceFeaturesTest :
    ParseSpec({ json ->
        val features =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/features_0.json"),
                ).data
                .let(::FeatureResolver)

        val featuresRoomControl =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/features_34772_7637415001735184_RoomControl-1.json"),
                ).data
                .let(::FeatureResolver)

        val features7637415001735184Gateway =
            json
                .decodeFromString<ResponseData<DeviceFeature>>(
                    readFileFromResources("features/device/features_34772_7637415001735184_gateway.json"),
                ).data
                .let(::FeatureResolver)

        context("Decorates with DeviceEtnFeature") {
            val feature = features[DeviceEtnFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue ""
            }
        }

        context("Decorates with DeviceSerialFeature") {
            val feature = features[DeviceSerialFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue "7956226000137183"
            }
        }

        context("Decorates with DeviceTimeseriesMonitoringIonizationFeature") {
            val feature = features[DeviceTimeseriesMonitoringIonizationFeature.descriptor]
            assertSoftly {
                feature.countOne shouldBeValue 0.0
                feature.timestampOne shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countTwo shouldBeValue 0.0
                feature.timestampTwo shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countThree shouldBeValue 0.0
                feature.timestampThree shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countFour shouldBeValue 0.0
                feature.timestampFour shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countFive shouldBeValue 0.0
                feature.timestampFive shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countSix shouldBeValue 0.0
                feature.timestampSix shouldBeValue "1970-01-01T00:00:00.000Z"
                feature.countSeven shouldBeValue 0.0
                feature.timestampSeven shouldBeValue "1970-01-01T00:00:00.000Z"
            }
        }

        context("Decorates with DeviceZigbeeActiveFeature") {
            assertSoftly(features[DeviceZigbeeActiveFeature.descriptor]) { feature ->
                feature.active shouldBeValue true
                // Verify the commands exist (we can't test execution)
                feature.activate.shouldNotBeNull()
                feature.deactivate.shouldNotBeNull()
                feature.setActive.shouldNotBeNull()
            }
        }

        context("Decorates with HeatingBoilerPumpsInternalFeature") {
            val feature = features[HeatingBoilerPumpsInternalFeature.descriptor]
            assertSoftly {
                feature.status shouldBe HeatingBoilerPumpsInternalFeature.Status(StringValue("off"))
            }
        }

        context("Decorates with HeatingBoilerPumpsInternalTargetFeature") {
            val feature = features[HeatingBoilerPumpsInternalTargetFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue 0.0
            }
        }

        context("Decorates with HeatingBoilerSensorsTemperatureCommonSupplyFeature") {
            val feature = features[HeatingBoilerSensorsTemperatureCommonSupplyFeature.descriptor]
            assertSoftly {
                feature.status shouldBeValue "error"
            }
        }

        context("Decorates with HeatingBoilerSerialFeature") {
            val feature = features[HeatingBoilerSerialFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue "7956226000137183"
            }
        }

        context("Decorates with HeatingBoilerTemperatureFeature") {
            val feature = features[HeatingBoilerTemperatureFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue 15.0
            }
        }

        context("Decorates with HeatingBufferCylinderSensorsTemperatureMainFeature") {
            val feature = features[HeatingBufferCylinderSensorsTemperatureMainFeature.descriptor]
            assertSoftly {
                feature.status shouldBeValue "notConnected"
            }
        }

        context("Decorates with DeviceConfigurationFeature") {
            val feature = featuresRoomControl[DeviceConfigurationFeature.descriptor]
            assertSoftly {
                feature.dhwActive shouldBeValue false
                feature.dhwEnabled shouldBeValue false
                feature.solarActive shouldBeValue false
                feature.solarEnabled shouldBeValue false
                feature.circuitsActive shouldBeValue emptyList()
                feature.circuitsEnabled shouldBeValue emptyList()
                feature.heatingConfigurationRegulation shouldBeValue ""
                feature.roomsActive.element.size shouldBe 17
                feature.roomsOthersActive.element.size shouldBe 2
                feature.roomsEnabled.element.size shouldBe 17
                feature.roomsOthersEnabled.element.size shouldBe 2
            }
        }

        context("Decorates with DeviceZigbeeCoordinatorFeature") {
            val feature = featuresRoomControl[DeviceZigbeeCoordinatorFeature.descriptor]
            assertSoftly {
                feature.timeout shouldBeValue 120.0
                feature.status.element.size shouldBe 1
                feature.status.element[0].device shouldBe "zigbee-0000000000000000"
                feature.status.element[0].value shouldBe "OK"
                feature.addDevice.shouldNotBeNull()
                feature.removeDevice.shouldNotBeNull()
            }
        }

        context("Decorates with RoomsFeature") {
            val feature = featuresRoomControl[RoomsFeature.descriptor]
            assertSoftly {
                feature.enabled.element.size shouldBe 17
                feature.add.shouldNotBeNull()
            }
        }

        context("Decorates with RoomsOthersFeature") {
            val feature = featuresRoomControl[RoomsOthersNFeature.descriptor, 0]
            assertSoftly {
                feature.active shouldBeValue true
                feature.name shouldBeValue "OR 2"
                feature.heatingCircuit shouldBeValue 1.0
//                feature.actors shouldBe emptyList()
                feature.activate.shouldNotBeNull()
                feature.deactivate.shouldNotBeNull()
                feature.setActive.shouldNotBeNull()
                feature.setName.shouldNotBeNull()
            }
        }

        context("Decorates with DeviceTimezoneFeature") {
            val feature = features7637415001735184Gateway[DeviceTimezoneFeature.descriptor]
            assertSoftly {
                feature.value shouldBeValue "Europe/Warsaw"
                feature.setTimezone.shouldNotBeNull()
            }
        }

        context("Decorates with TcuModeFeature") {
            val feature = features7637415001735184Gateway[TcuModeFeature.descriptor]
            assertSoftly {
                feature.setMode.shouldNotBeNull()
            }
        }

        context("Decorates with HeatingCircuitsNHeatingScheduleFeature") {
            val feature = features[HeatingCircuitsNHeatingScheduleFeature.descriptor, 0]
            assertSoftly {
                feature.entries.element.size shouldBe 7
                feature.setSchedule.shouldNotBeNull()
                feature.resetSchedule.shouldNotBeNull()
            }
        }
    })
