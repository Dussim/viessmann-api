package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import kotlin.time.Instant

val PropertySerializerTest by testSuite {
    val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    // region Primitive arrays

    test("string array dispatches without key inspection") {
        val input = """{"type": "array", "value": ["a", "b", "c"]}"""

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListStringValue>()
        property.value.element shouldBe listOf("a", "b", "c")
    }

    test("double array dispatches without key inspection") {
        val input = """{"type": "array", "value": [1.0, 2.0]}"""

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListDoubleValue>()
    }

    test("empty array deserializes to ListEmptyValue") {
        val input = """{"type": "array", "value": []}"""

        val property = json.decodeFromString(Property.serializer(), input)
        property.value shouldBe ListEmptyValue
    }

    // endregion

    // region Nullable primitives

    test("nullable primitive values deserialize from JsonNull") {
        val doubleProperty = json.decodeFromString(Property.serializer(), """{"type": "number", "value": null}""")
        doubleProperty.value.shouldBeInstanceOf<NullableDoubleValue>().element shouldBe null

        val stringProperty = json.decodeFromString(Property.serializer(), """{"type": "string", "value": null}""")
        stringProperty.value.shouldBeInstanceOf<NullableStringValue>().element shouldBe null

        val booleanProperty = json.decodeFromString(Property.serializer(), """{"type": "boolean", "value": null}""")
        booleanProperty.value.shouldBeInstanceOf<NullableBooleanValue>().element shouldBe null
    }

    // endregion

    // region Unique-key fast-path dispatch

    test("DeviceError via unique key 'audiences'") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "errorCode": "A1",
                  "timestamp": "2025-01-01T00:00:00.000Z",
                  "accessLevel": "admin",
                  "priority": "high",
                  "audiences": ["installer"]
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListDeviceErrorValue>()
        property.value.element.size shouldBe 1
    }

    test("WifiNetwork via unique key 'ssid'") {
        val input =
            """
            {
              "type": "array",
              "value": [{"ssid": "MyWifi", "signalStrength": -50.0}]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListWifiNetworkValue>()
    }

    test("SolarlogDevice via unique key 'serialNumber'") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"index": "0", "type": "inverter", "manufacturer": "SMA", "model": "STP 10.0", "serialNumber": "ABC123"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListSolarlogDeviceValue>()
    }

    test("VentilationMessage accepts legacy status/count variant") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"timestamp": "2025-01-01T00:00:00.000Z", "errorCode": "V1", "status": "active", "count": 3.0, "priority": "low"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListVentilationMessageValue>()
    }

    test("VentilationMessage accepts E3 bus message variant") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "timestamp": "2020-07-26T13:16:05.000Z",
                  "errorCode": "A.73",
                  "priority": "warning",
                  "busType": "Zigbee",
                  "busAddress": 25
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListVentilationMessageValue>()
        value.element.single().status shouldBe null
        value.element.single().count shouldBe null
        value.element.single().busType shouldBe "Zigbee"
        value.element.single().busAddress shouldBe 25
    }

    test("SystemMessageEntry via unique key 'code'") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "code": "S.4",
                  "firstAppearanceTime": "1970-01-01T00:00:00.000Z",
                  "firstGoneTime": "1970-01-01T00:00:00.000Z",
                  "lastAppearanceTime": "1970-01-01T00:00:00.000Z",
                  "lastGoneTime": "1970-01-01T00:00:00.000Z",
                  "counter": 1,
                  "busAddress": "0",
                  "busType": "unknown",
                  "controller": "unknown",
                  "active": true,
                  "dataTracing": "unavailable",
                  "audiences": []
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListSystemMessageEntryValue>()
        value.element.single().code shouldBe "S.4"
    }

    test("OperatingDataCellsDetail via unique key 'voltageValue'") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "voltageValue": {"type": "number", "value": 3.2},
                  "cellBalance": {"type": "number", "value": 0.01},
                  "functionStatus": {"type": "string", "value": "ok"},
                  "safetyStatus": {"type": "string", "value": "ok"}
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListOperatingDataCellsDetailValue>()
    }

    test("OnboardUpdaterLastErrorCode via unique key 'subCode'") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"deviceFamily": 24, "error": "genericError", "subCode": "113901648"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListOnboardUpdaterLastErrorCodeValue>()
        property.value.element shouldBe
            listOf(
                OnboardUpdaterLastErrorCode(
                    deviceFamily = 24,
                    error = "genericError",
                    subCode = "113901648",
                ),
            )
    }

    // endregion

    // region Excluded-key dispatch

    test("FuelCellError — has priority but no audiences") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"timestamp": "2025-01-01T00:00:00.000Z", "errorCode": "F1", "accessLevel": "admin", "priority": "high"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListFuelCellErrorValue>()
    }

    test("FuelCellError accepts DateNoTimeZoneSchema timestamp as UTC instant") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"timestamp": "2017-12-22T12:19:05.613", "errorCode": "a4", "accessLevel": "customer", "priority": "criticalError"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListFuelCellErrorValue>()
        value.element.single().timestamp shouldBe Instant.parse("2017-12-22T12:19:05.613Z")
    }

    test("EebusServicePartner — has ski but no brand/model") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"type": "partner", "id": "P1", "ski": "abc123"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListEebusServicePartnerValue>()
    }

    test("EebusDevicesPaired — has type and busAddress") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"type": "heatpump", "busAddress": "unknown"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListEebusDevicesPairedValue>()
        property.value.element shouldBe listOf(EebusDevicesPaired(type = "heatpump", busAddress = "unknown"))
    }

    test("SolarlogDevicesPaired — has type and index") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {"type": "photovoltaicInverter", "index": "123"}
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListSolarlogDevicesPairedValue>()
        property.value.element shouldBe listOf(SolarlogDevicesPaired(type = "photovoltaicInverter", index = "123"))
    }

    test("BusType preserves bus topology fields when device information fields are present") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "busAddress": 1,
                  "busType": "CanInternal",
                  "deviceObjectProperty": "HBMU",
                  "deviceFunction": "VitodensOneHundred",
                  "softwareVersion": "0037.0500.2111.0079",
                  "hardwareVersion": "0037.0602.0602.0001",
                  "etn": "0000000000000000"
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListBusTypeValue>()
        value.element shouldBe
            listOf(
                BusType(
                    busAddress = 1,
                    busType = "CanInternal",
                    deviceObjectProperty = "HBMU",
                    deviceFunction = "VitodensOneHundred",
                    softwareVersion = "0037.0500.2111.0079",
                    hardwareVersion = "0037.0602.0602.0001",
                    etn = "0000000000000000",
                ),
            )
    }

    test("DeviceInformation still handles device information entries without bus routing fields") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "deviceObjectProperty": "HBMU",
                  "deviceFunction": "VitodensOneHundred",
                  "softwareVersion": "0037.0500.2111.0079",
                  "hardwareVersion": "0037.0602.0602.0001",
                  "etn": "0000000000000000"
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListDeviceInformationValue>()
        value.element shouldBe
            listOf(
                DeviceInformation(
                    deviceObjectProperty = "HBMU",
                    deviceFunction = "VitodensOneHundred",
                    softwareVersion = "0037.0500.2111.0079",
                    hardwareVersion = "0037.0602.0602.0001",
                    etn = "0000000000000000",
                ),
            )
    }

    test("BusType accepts product matrix product metadata variant") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "busType": "CanExternal",
                  "busAddress": 1,
                  "viessmannIdentificationNumber": "1234567890123456",
                  "productFamily": "VC250"
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListBusTypeValue>()
        value.element shouldBe
            listOf(
                BusType(
                    busAddress = 1,
                    busType = "CanExternal",
                    viessmannIdentificationNumber = "1234567890123456",
                    productFamily = "VC250",
                ),
            )
    }

    test("PowerBalanceEntry is not captured by BusType matcher") {
        val input =
            """
            {
              "type": "array",
              "value": [
                {
                  "value": 13,
                  "unit": "watt",
                  "type": "Heatpump",
                  "busType": "CanInternal",
                  "busAddress": "1"
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ListPowerBalanceEntryValue>()
    }

    test("ElectricalEnergyMatrix accepts backend alternate scalar value shape") {
        val input =
            """
            {
              "type": "ElectricalEnergyMatrix",
              "value": [
                {
                  "ident": 1,
                  "parent": 1,
                  "type": "TariffOne",
                  "class": "Meter",
                  "value": 19,
                  "unit": "watt",
                  "busType": "CanExternal",
                  "busAddress": "97",
                  "children": [
                    {
                      "ident": 2,
                      "parent": 1,
                      "type": "Inverter",
                      "class": "Inverter",
                      "value": 0,
                      "unit": "watt",
                      "busType": "CanExternal",
                      "busAddress": "72"
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        val value = property.value.shouldBeInstanceOf<ListElectricalEnergyMatrixValue>()
        value.element.single().value shouldBe 19.0
        value.element.single().busAddress shouldBe "97"
        value.element
            .single()
            .children
            .single()
            .value shouldBe 0.0
    }

    // endregion

    // region Object dispatch

    test("OtherRoomConfiguration via unique key 'hydraulicBalance'") {
        val input =
            """
            {
              "type": "object",
              "value": {
                "hydraulicBalance": true,
                "heatupSpeed": "fast",
                "trvAlgoActive": true,
                "openPointDetection": false,
                "virtualClimateSensor": true,
                "etrvSync": false,
                "useTrvOpenWindow": true,
                "heatOnTime": false
              }
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<ObjectOtherRoomConfigurationValue>()
    }

    test("FactoryResetInfo via unique key 'day'") {
        val input =
            """
            {
              "type": "object",
              "value": {"day": 15, "month": 3, "year": 2025}
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<FactoryResetInfoValue>()
    }

    // endregion

    // region Fallback

    test("unknown object shape falls through to UnknownValue") {
        val input =
            """
            {
              "type": "array",
              "value": [{"completelyUnknownField": "whatever"}]
            }
            """.trimIndent()

        val property = json.decodeFromString(Property.serializer(), input)
        property.value.shouldBeInstanceOf<UnknownValue>()
    }
}
