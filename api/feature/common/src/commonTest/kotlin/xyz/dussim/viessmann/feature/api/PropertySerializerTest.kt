package xyz.dussim.viessmann.feature.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class PropertySerializerTest :
    FunSpec({
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

        test("VentilationMessage via unique key 'count'") {
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

        // endregion
    })
