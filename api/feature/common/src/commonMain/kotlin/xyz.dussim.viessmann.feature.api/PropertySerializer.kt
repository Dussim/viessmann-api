package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private data class ArrayElementMatcher<T>(
    val uniqueKey: String? = null,
    val requiredKeys: List<String>,
    val excludedKeys: List<String> = emptyList(),
    val serializer: KSerializer<T>,
    val wrapper: (List<T>) -> PropertyValue<*>,
)

private val ARRAY_ELEMENT_MATCHERS: List<ArrayElementMatcher<*>> =
    listOf(
        // Unique-key matchers first (O(1) lookup path)
        ArrayElementMatcher(
            uniqueKey = "audiences",
            requiredKeys = listOf("errorCode", "timestamp", "accessLevel", "priority", "audiences"),
            serializer = DeviceError.serializer(),
            wrapper = ::ListDeviceErrorValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "device",
            requiredKeys = listOf("device", "value"),
            serializer = ZigbeeDeviceStatus.serializer(),
            wrapper = ::ListZigbeeDeviceStatusValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "heatingCircuit",
            requiredKeys = listOf("deviceId", "heatingCircuit"),
            serializer = RoomActor.serializer(),
            wrapper = ::ListRoomActorValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "voltageValue",
            requiredKeys = listOf("voltageValue", "cellBalance", "functionStatus", "safetyStatus"),
            serializer = OperatingDataCellsDetail.serializer(),
            wrapper = ::ListOperatingDataCellsDetailValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "memberId",
            requiredKeys = listOf("id", "role", "status", "memberId"),
            serializer = EnergyChargedDevice.serializer(),
            wrapper = ::ListEnergyChargedDeviceValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "lowerBorder",
            requiredKeys = listOf("level", "lowerBorder", "upperBorder"),
            serializer = SensorValue.serializer(),
            wrapper = ::ListSensorValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "ssid",
            requiredKeys = listOf("ssid", "signalStrength"),
            serializer = WifiNetwork.serializer(),
            wrapper = ::ListWifiNetworkValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "count",
            requiredKeys = listOf("timestamp", "errorCode", "status", "count", "priority"),
            serializer = VentilationMessage.serializer(),
            wrapper = ::ListVentilationMessageValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "serialNumber",
            requiredKeys = listOf("index", "manufacturer", "model", "serialNumber"),
            serializer = SolarlogDevice.serializer(),
            wrapper = ::ListSolarlogDeviceValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "tariff1",
            requiredKeys = listOf("ident", "parent", "type", "class", "tariff1", "tariff2", "unit"),
            serializer = ElectricalEnergyMatrix.serializer(),
            wrapper = ::ListElectricalEnergyMatrixValue,
        ),
        ArrayElementMatcher(
            uniqueKey = "stateMachine",
            requiredKeys = listOf("timestamp", "actor", "status", "event", "circuit", "stateMachine", "additionalInfo"),
            serializer = LogBookEntry.serializer(),
            wrapper = ::ListLogBookEntryValue,
        ),
        // Non-unique key matchers (need excludedKeys to disambiguate)
        ArrayElementMatcher(
            requiredKeys = listOf("type", "brand", "model", "id", "ski"),
            serializer = EebusDevice.serializer(),
            wrapper = ::ListEebusDeviceValue,
        ),
        ArrayElementMatcher(
            requiredKeys = listOf("type", "id", "ski"),
            excludedKeys = listOf("brand", "model"),
            serializer = EebusServicePartner.serializer(),
            wrapper = ::ListEebusServicePartnerValue,
        ),
        ArrayElementMatcher(
            requiredKeys = listOf("deviceObjectProperty", "deviceFunction", "softwareVersion", "hardwareVersion", "etn"),
            excludedKeys = listOf("busAddress"),
            serializer = DeviceInformation.serializer(),
            wrapper = ::ListDeviceInformationValue,
        ),
        ArrayElementMatcher(
            requiredKeys = listOf("busAddress", "busType", "deviceObjectProperty"),
            serializer = BusType.serializer(),
            wrapper = ::ListBusTypeValue,
        ),
        ArrayElementMatcher(
            requiredKeys = listOf("timestamp", "errorCode", "accessLevel", "priority"),
            excludedKeys = listOf("audiences", "busAddress", "busType"),
            serializer = FuelCellError.serializer(),
            wrapper = ::ListFuelCellErrorValue,
        ),
        // Generic fallback — PowerBalanceEntry has no unique keys
        ArrayElementMatcher(
            requiredKeys = listOf("value", "unit", "type"),
            serializer = PowerBalanceEntry.serializer(),
            wrapper = ::ListPowerBalanceEntryValue,
        ),
    )

// Pre-computed index: uniqueKey → matcher (for O(1) fast path)
private val UNIQUE_KEY_INDEX: Map<String, ArrayElementMatcher<*>> =
    ARRAY_ELEMENT_MATCHERS
        .filter { it.uniqueKey != null }
        .associateBy { it.uniqueKey!! }

private data class ObjectElementMatcher<T>(
    val uniqueKey: String? = null,
    val requiredKeys: List<String>,
    val serializer: KSerializer<T>,
    val wrapper: (T) -> PropertyValue<*>,
)

private val OBJECT_ELEMENT_MATCHERS: List<ObjectElementMatcher<*>> =
    listOf(
        ObjectElementMatcher(
            uniqueKey = "hydraulicBalance",
            requiredKeys = listOf("hydraulicBalance"),
            serializer = OtherRoomConfiguration.serializer(),
            wrapper = ::ObjectOtherRoomConfigurationValue,
        ),
        ObjectElementMatcher(
            uniqueKey = "logs",
            requiredKeys = listOf("logs", "default"),
            serializer = Logs.serializer(),
            wrapper = ::LogsValue,
        ),
        ObjectElementMatcher(
            uniqueKey = "viessmannIdentificationNumber",
            requiredKeys = listOf("busType", "busAddress", "viessmannIdentificationNumber", "productFamily"),
            serializer = ProductInfo.serializer(),
            wrapper = ::ProductInfoValue,
        ),
        ObjectElementMatcher(
            uniqueKey = "day",
            requiredKeys = listOf("day", "month", "year"),
            serializer = FactoryResetInfo.serializer(),
            wrapper = ::FactoryResetInfoValue,
        ),
    )

@OptIn(ExperimentalSerializationApi::class)
internal data object PropertySerializer : KSerializer<Property> {
    override val descriptor =
        buildClassSerialDescriptor("xyz.dussim.viessmann.feature.api.Property") {
            element<String>("type")
            element<JsonElement>("value")
            element<String?>("unit")
        }

    override fun serialize(
        encoder: Encoder,
        value: Property,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.type)
            when (val propertyValue = value.value) {
                is BooleanValue -> {
                    encodeBooleanElement(descriptor, 1, propertyValue.element)
                }

                is DoubleValue -> {
                    encodeDoubleElement(descriptor, 1, propertyValue.element)
                }

                is StringValue -> {
                    encodeStringElement(descriptor, 1, propertyValue.element)
                }

                is ListDoubleValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(Double.serializer()),
                        propertyValue.element,
                    )
                }

                is ListStringValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(String.serializer()),
                        propertyValue.element,
                    )
                }

                is UnknownValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        JsonElement.serializer(),
                        propertyValue.element,
                    )
                }

                is ListDeviceErrorValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(DeviceError.serializer()),
                        propertyValue.element,
                    )
                }

                is ListRoomActorValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(RoomActor.serializer()),
                        propertyValue.element,
                    )
                }

                is ListZigbeeDeviceStatusValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(ZigbeeDeviceStatus.serializer()),
                        propertyValue.element,
                    )
                }

                is ObjectOtherRoomConfigurationValue -> {
                    encodeSerializableElement(descriptor, 1, OtherRoomConfiguration.serializer(), propertyValue.element)
                }

                is ListDeviceValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(Device.serializer()),
                        propertyValue.element,
                    )
                }

                is ScheduleValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        MapSerializer(String.serializer(), ListSerializer(Schedule.serializer())),
                        propertyValue.element,
                    )
                }

                is ListBusTypeValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(BusType.serializer()),
                        propertyValue.element,
                    )
                }

                is EnergyMatrixValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        EnergyMatrix.serializer(),
                        propertyValue.element,
                    )
                }

                is LogsValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        Logs.serializer(),
                        propertyValue.element,
                    )
                }

                is ListLogBookEntryValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(LogBookEntry.serializer()),
                        propertyValue.element,
                    )
                }

                is ProductInfoValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ProductInfo.serializer(),
                        propertyValue.element,
                    )
                }

                is FactoryResetInfoValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        FactoryResetInfo.serializer(),
                        propertyValue.element,
                    )
                }

                is ListEebusDeviceValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(EebusDevice.serializer()),
                        propertyValue.element,
                    )
                }

                is ListEebusServicePartnerValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(EebusServicePartner.serializer()),
                        propertyValue.element,
                    )
                }

                is ListElectricalEnergyMatrixValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(ElectricalEnergyMatrix.serializer()),
                        propertyValue.element,
                    )
                }

                is ListOperatingDataCellsDetailValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(OperatingDataCellsDetail.serializer()),
                        propertyValue.element,
                    )
                }

                is ListEnergyChargedDeviceValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(EnergyChargedDevice.serializer()),
                        propertyValue.element,
                    )
                }

                is ListDeviceInformationValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(DeviceInformation.serializer()),
                        propertyValue.element,
                    )
                }

                is ListSensorValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(SensorValue.serializer()),
                        propertyValue.element,
                    )
                }

                is ListPowerBalanceEntryValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(PowerBalanceEntry.serializer()),
                        propertyValue.element,
                    )
                }

                is ListFuelCellErrorValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(FuelCellError.serializer()),
                        propertyValue.element,
                    )
                }

                is ListWifiNetworkValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(WifiNetwork.serializer()),
                        propertyValue.element,
                    )
                }

                is ListVentilationMessageValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(VentilationMessage.serializer()),
                        propertyValue.element,
                    )
                }

                is ListSolarlogDeviceValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(SolarlogDevice.serializer()),
                        propertyValue.element,
                    )
                }

                is TestResultValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        TestResult.serializer(),
                        propertyValue.element,
                    )
                }

                ListEmptyValue -> {
                    encodeSerializableElement(
                        descriptor,
                        1,
                        ListSerializer(NothingSerializer()),
                        emptyList(),
                    )
                }
            }
            encodeNullableSerializableElement(descriptor, 2, String.serializer(), value.unit)
        }
    }

    override fun deserialize(decoder: Decoder): Property {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement().jsonObject
        val type = element.getValue("type").jsonPrimitive.content
        val value = element.getValue("value")
        val unit = element["unit"]?.jsonPrimitive?.content

        return Property(
            type = type,
            value =
                when (type) {
                    BOOLEAN -> {
                        BooleanValue(value.jsonPrimitive.boolean)
                    }

                    NUMBER -> {
                        DoubleValue(value.jsonPrimitive.double)
                    }

                    STRING -> {
                        StringValue(value.jsonPrimitive.content)
                    }

                    ARRAY -> {
                        val array = value as? JsonArray

                        if (array == null) {
                            UnknownValue(value)
                        } else if (array.isEmpty()) {
                            ListEmptyValue
                        } else {
                            // Try primitives first (cheapest check)
                            decoder.decodeOrNull(array, String.serializer(), ::ListStringValue)
                                ?: decoder.decodeOrNull(array, Double.serializer(), ::ListDoubleValue)
                                // Then key-based object dispatch
                                ?: decodeArrayByKeyInspection(decoder, array)
                                ?: UnknownValue(value)
                        }
                    }

                    OBJECT -> {
                        val obj = value as? JsonObject

                        if (obj == null) {
                            UnknownValue(value)
                        } else {
                            decodeObjectByKeyInspection(decoder, obj)
                                ?: UnknownValue(value)
                        }
                    }

                    DEVICE_LIST -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(array, Device.serializer(), ::ListDeviceValue)
                            ?: UnknownValue(value)
                    }

                    SCHEDULE -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(
                            obj,
                            MapSerializer(String.serializer(), ListSerializer(Schedule.serializer())),
                            ::ScheduleValue,
                        )
                            ?: UnknownValue(value)
                    }

                    ENERGY_MATRIX -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(
                            obj,
                            EnergyMatrix.serializer(),
                            ::EnergyMatrixValue,
                        )
                            ?: UnknownValue(value)
                    }

                    CO2_VALUES, AIR_QUALITY_VALUES -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(
                            array,
                            SensorValue.serializer(),
                            ::ListSensorValue,
                        )
                            ?: UnknownValue(value)
                    }

                    CONSOLIDATOR_VALUE_LIST -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(
                            array,
                            EnergyChargedDevice.serializer(),
                            ::ListEnergyChargedDeviceValue,
                        )
                            ?: UnknownValue(value)
                    }

                    ELECTRICAL_ENERGY_MATRIX -> {
                        val array = value as? JsonArray

                        decoder.decodeOrNull(
                            array,
                            ElectricalEnergyMatrix.serializer(),
                            ::ListElectricalEnergyMatrixValue,
                        )
                            ?: UnknownValue(value)
                    }

                    TEST_RESULT -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(
                            obj,
                            TestResult.serializer(),
                            ::TestResultValue,
                        )
                            ?: UnknownValue(value)
                    }

                    else -> {
                        UnknownValue(value)
                    }
                },
            unit = unit,
        )
    }

    private fun <T> JsonDecoder.decodeOrNull(
        array: JsonArray?,
        serializer: KSerializer<T>,
        provider: (List<T>) -> PropertyValue<*>,
    ): PropertyValue<*>? {
        if (array == null) {
            return null
        }

        if (array.isEmpty()) {
            return ListEmptyValue
        }

        return try {
            provider(json.decodeFromJsonElement(ListSerializer(serializer), array))
        } catch (_: Exception) {
            return null
        }
    }

    private fun <T> JsonDecoder.decodeOrNull(
        obj: JsonObject?,
        serializer: KSerializer<T>,
        provider: (T) -> PropertyValue<*>,
    ): PropertyValue<*>? {
        if (obj == null) {
            return null
        }

        return try {
            provider(json.decodeFromJsonElement(serializer, obj))
        } catch (_: Exception) {
            return null
        }
    }

    private fun <T> matchAndDecode(
        decoder: JsonDecoder,
        array: JsonArray,
        matcher: ArrayElementMatcher<T>,
    ): PropertyValue<*>? =
        try {
            matcher.wrapper(decoder.json.decodeFromJsonElement(ListSerializer(matcher.serializer), array))
        } catch (_: Exception) {
            null
        }

    private fun decodeArrayByKeyInspection(
        decoder: JsonDecoder,
        array: JsonArray,
    ): PropertyValue<*>? {
        val firstElement = array.firstOrNull() ?: return ListEmptyValue

        // Primitive arrays — no key inspection possible
        if (firstElement !is JsonObject) return null

        val keys = firstElement.keys

        // Fast path: check if any unique key matches
        for (key in keys) {
            val matcher = UNIQUE_KEY_INDEX[key]
            if (matcher != null && keys.containsAll(matcher.requiredKeys)) {
                return matchAndDecode(decoder, array, matcher)
            }
        }

        // Second pass: required + excluded key matching
        for (matcher in ARRAY_ELEMENT_MATCHERS) {
            if (matcher.uniqueKey != null) continue // already tried in fast path
            if (keys.containsAll(matcher.requiredKeys) &&
                (matcher.excludedKeys.isEmpty() || matcher.excludedKeys.none { it in keys })
            ) {
                return matchAndDecode(decoder, array, matcher)
            }
        }

        return null
    }

    private fun <T> decodeObject(
        decoder: JsonDecoder,
        obj: JsonObject,
        matcher: ObjectElementMatcher<T>,
    ): PropertyValue<*>? =
        try {
            matcher.wrapper(decoder.json.decodeFromJsonElement(matcher.serializer, obj))
        } catch (_: Exception) {
            null
        }

    private fun decodeObjectByKeyInspection(
        decoder: JsonDecoder,
        obj: JsonObject,
    ): PropertyValue<*>? {
        val keys = obj.keys
        for (matcher in OBJECT_ELEMENT_MATCHERS) {
            if (keys.containsAll(matcher.requiredKeys)) {
                return decodeObject(decoder, obj, matcher)
            }
        }
        return null
    }
}
