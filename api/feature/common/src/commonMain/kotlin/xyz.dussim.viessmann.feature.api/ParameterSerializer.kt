package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal data object ParameterSerializer : KSerializer<Parameter> {
    private val emptyObject = buildJsonObject { }

    override val descriptor =
        buildClassSerialDescriptor("xyz.dussim.viessmann.feature.api.Parameter") {
            element<String>("type")
            element<Boolean>("required")
            element<JsonElement>("constraints")
        }

    override fun deserialize(decoder: Decoder): Parameter {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement().jsonObject
        val type = element.getValue("type").jsonPrimitive.content
        val required = element.getValue("required").jsonPrimitive.boolean
        val constraints =
            (element["constraints"] as? JsonObject) ?: return Parameter(
                type = type,
                required = required,
                constraints = UnknownConstraints(element.getValue("constraints")),
            )

        return Parameter(
            type = type,
            required = required,
            constraints =
                when (type) {
                    BOOLEAN -> BooleanConstraints
                    NUMBER -> jsonDecoder.json.decodeFromJsonElement(NumberConstraints.serializer(), constraints)
                    ARRAY -> jsonDecoder.decodeArrayConstraints(constraints)
                    OBJECT -> jsonDecoder.json.decodeFromJsonElement(ObjectConstraints.serializer(), constraints)
                    SCHEDULE -> jsonDecoder.json.decodeFromJsonElement(ScheduleConstraints.serializer(), constraints)
                    ENERGY_MATRIX -> EnergyMatrixConstraints
                    STRING -> jsonDecoder.json.decodeFromJsonElement(StringConstraints.serializer(), constraints)
                    else -> UnknownConstraints(constraints)
                },
        )
    }

    override fun serialize(
        encoder: Encoder,
        value: Parameter,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.type)
            encodeBooleanElement(descriptor, 1, value.required)
            when (val constraints = value.constraints) {
                BooleanConstraints -> encodeSerializableElement(descriptor, 2, JsonObject.serializer(), emptyObject)
                is ArrayEmptyConstraints -> encodeSerializableElement(descriptor, 2, ArrayEmptyConstraints.serializer(), constraints)
                is ArrayNumberConstraints -> encodeSerializableElement(descriptor, 2, ArrayNumberConstraints.serializer(), constraints)
                is ArrayStringConstraints -> encodeSerializableElement(descriptor, 2, ArrayStringConstraints.serializer(), constraints)
                is ArrayBooleanConstraints -> encodeSerializableElement(descriptor, 2, ArrayBooleanConstraints.serializer(), constraints)
                is ArrayObjectConstraints -> encodeSerializableElement(descriptor, 2, ArrayObjectConstraints.serializer(), constraints)
                is ArrayUnknownConstraints -> encodeSerializableElement(descriptor, 2, ArrayUnknownConstraints.serializer(), constraints)
                is NumberConstraints -> encodeSerializableElement(descriptor, 2, NumberConstraints.serializer(), constraints)
                is ObjectConstraints -> encodeSerializableElement(descriptor, 2, ObjectConstraints.serializer(), constraints)
                is ScheduleConstraints -> encodeSerializableElement(descriptor, 2, ScheduleConstraints.serializer(), constraints)
                EnergyMatrixConstraints -> encodeSerializableElement(descriptor, 2, JsonObject.serializer(), emptyObject)
                is StringConstraints -> encodeSerializableElement(descriptor, 2, StringConstraints.serializer(), constraints)
                is UnknownConstraints -> encodeSerializableElement(descriptor, 2, UnknownConstraints.serializer(), constraints)
            }
        }
    }

    private fun JsonDecoder.decodeArrayConstraints(constraints: JsonObject): Constraints<*> {
        val enumValues = constraints["enum"] as? JsonArray ?: return json.decodeFromJsonElement(ArrayEmptyConstraints.serializer(), constraints)

        if (enumValues.isEmpty()) {
            return json.decodeFromJsonElement(ArrayUnknownConstraints.serializer(), constraints)
        }

        return when {
            enumValues.all { element ->
                element is JsonPrimitive && element.isString
            } -> {
                json.decodeFromJsonElement(ArrayStringConstraints.serializer(), constraints)
            }

            enumValues.all { element ->
                element is JsonPrimitive && !element.isString && element.booleanOrNull != null
            } -> {
                json.decodeFromJsonElement(ArrayBooleanConstraints.serializer(), constraints)
            }

            enumValues.all { element ->
                element is JsonPrimitive && !element.isString && element.doubleOrNull != null
            } -> {
                json.decodeFromJsonElement(ArrayNumberConstraints.serializer(), constraints)
            }

            enumValues.all { element ->
                element is JsonObject
            } -> {
                json.decodeFromJsonElement(ArrayObjectConstraints.serializer(), constraints)
            }

            else -> {
                json.decodeFromJsonElement(ArrayUnknownConstraints.serializer(), constraints)
            }
        }
    }
}
