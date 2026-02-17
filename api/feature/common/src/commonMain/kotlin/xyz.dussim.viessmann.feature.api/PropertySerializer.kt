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

                        decoder.decodeOrNull(array, String.serializer(), ::ListStringValue)
                            ?: decoder.decodeOrNull(array, Double.serializer(), ::ListDoubleValue)
                            ?: decoder.decodeOrNull(array, DeviceError.serializer(), ::ListDeviceErrorValue)
                            ?: decoder.decodeOrNull(array, ZigbeeDeviceStatus.serializer(), ::ListZigbeeDeviceStatusValue)
                            ?: decoder.decodeOrNull(array, RoomActor.serializer(), ::ListRoomActorValue)
                            ?: decoder.decodeOrNull(array, BusType.serializer(), ::ListBusTypeValue)
                            ?: decoder.decodeOrNull(array, LogBookEntry.serializer(), ::ListLogBookEntryValue)
                            ?: decoder.decodeOrNull(array, EebusDevice.serializer(), ::ListEebusDeviceValue)
                            ?: decoder.decodeOrNull(array, EebusServicePartner.serializer(), ::ListEebusServicePartnerValue)
                            ?: decoder.decodeOrNull(array, ElectricalEnergyMatrix.serializer(), ::ListElectricalEnergyMatrixValue)
                            ?: decoder.decodeOrNull(array, OperatingDataCellsDetail.serializer(), ::ListOperatingDataCellsDetailValue)
                            ?: decoder.decodeOrNull(array, EnergyChargedDevice.serializer(), ::ListEnergyChargedDeviceValue)
                            ?: decoder.decodeOrNull(array, DeviceInformation.serializer(), ::ListDeviceInformationValue)
                            ?: decoder.decodeOrNull(array, PowerBalanceEntry.serializer(), ::ListPowerBalanceEntryValue)
                            ?: decoder.decodeOrNull(array, FuelCellError.serializer(), ::ListFuelCellErrorValue)
                            ?: decoder.decodeOrNull(array, WifiNetwork.serializer(), ::ListWifiNetworkValue)
                            ?: decoder.decodeOrNull(array, VentilationMessage.serializer(), ::ListVentilationMessageValue)
                            ?: UnknownValue(value)
                    }

                    OBJECT -> {
                        val obj = value as? JsonObject

                        decoder.decodeOrNull(obj, OtherRoomConfiguration.serializer(), ::ObjectOtherRoomConfigurationValue)
                            ?: decoder.decodeOrNull(obj, Logs.serializer(), ::LogsValue)
                            ?: decoder.decodeOrNull(obj, ProductInfo.serializer(), ::ProductInfoValue)
                            ?: decoder.decodeOrNull(obj, FactoryResetInfo.serializer(), ::FactoryResetInfoValue)
                            ?: UnknownValue(value)
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
}
