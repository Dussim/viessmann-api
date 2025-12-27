@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.dussim.viessmann.feature.api.validation.booleanConstraintsClassIndex
import xyz.dussim.viessmann.feature.api.validation.numberConstraintsClassIndex
import xyz.dussim.viessmann.feature.api.validation.scheduleConstraintsClassIndex
import xyz.dussim.viessmann.feature.api.validation.stringConstraintsClassIndex
import xyz.dussim.viessmann.feature.api.validation.unknownConstraintsClassIndex
import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord

private const val BOOLEAN = "boolean"
private const val NUMBER = "number"
private const val STRING = "string"
private const val SCHEDULE = "Schedule"

@JvmRecord
@Serializable
data class Command(
    val uri: String,
    val name: String,
    val isExecutable: Boolean,
    @Serializable(with = EfficientStringKeyMap.ParametersSerializer::class)
    val params: EfficientStringKeyMap<Parameter>,
)

@JvmRecord
@ConsistentCopyVisibility
@Serializable(with = ParameterSerializer::class)
data class Parameter internal constructor(
    val type: String,
    val required: Boolean,
    val constraints: Constraints<*>,
) {
    companion object;
}

sealed interface Constraints<T> {
    companion object {
        @JvmField
        val unknownConstraintsClass = UnknownConstraints::class

        @JvmField
        val booleanConstraintsClass = BooleanConstraints::class

        @JvmField
        val numberConstraintsClass = NumberConstraints::class

        @JvmField
        val stringConstraintsClass = StringConstraints::class

        @JvmField
        val scheduleConstraintsClass = ScheduleConstraints::class
    }
}

inline val Constraints<*>.constraintsClassIndex: Int
    get() =
        when (this) {
            BooleanConstraints -> booleanConstraintsClassIndex
            is NumberConstraints -> numberConstraintsClassIndex
            is ScheduleConstraints -> scheduleConstraintsClassIndex
            is StringConstraints -> stringConstraintsClassIndex
            is UnknownConstraints -> unknownConstraintsClassIndex
        }

@JvmInline
@Serializable
value class UnknownConstraints(
    val jsonElement: JsonElement,
) : Constraints<Nothing>

@Serializable
data object BooleanConstraints : Constraints<Boolean>

@JvmRecord
@Serializable
data class NumberConstraints(
    val min: Double? = null,
    val efficientLowerBorder: Double? = null,
    val efficientUpperBorder: Double? = null,
    val max: Double? = null,
    val stepping: Double? = null,
    val enum: List<Double>? = null,
) : Constraints<Double>

@JvmRecord
@Serializable
data class StringConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val regEx: String? = null,
    val enum: List<String>? = null,
    val sameDayAllowed: Boolean? = null,
) : Constraints<String>

@JvmRecord
@Serializable
data class ScheduleConstraints(
    val modes: List<String>,
    val maxEntries: Int,
    val resolution: Int,
    val defaultMode: String,
    val overlapAllowed: Boolean,
) : Constraints<Map<String, List<Schedule>>>

@JvmName("ofBoolean")
fun Parameter.Companion.of(
    constraints: BooleanConstraints = BooleanConstraints,
    required: Boolean = true,
): Parameter = Parameter(BOOLEAN, required, constraints)

@JvmName("ofNumber")
fun Parameter.Companion.of(
    constraints: NumberConstraints = NumberConstraints(),
    required: Boolean = true,
): Parameter = Parameter(NUMBER, required, constraints)

@JvmName("ofString")
fun Parameter.Companion.of(
    constraints: StringConstraints = StringConstraints(),
    required: Boolean = true,
): Parameter = Parameter(STRING, required, constraints)

@JvmName("ofSchedules")
fun Parameter.Companion.of(
    constraints: ScheduleConstraints,
    required: Boolean = true,
): Parameter = Parameter(SCHEDULE, required, constraints)

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
                    SCHEDULE -> jsonDecoder.json.decodeFromJsonElement(ScheduleConstraints.serializer(), constraints)
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
                is NumberConstraints -> encodeSerializableElement(descriptor, 2, NumberConstraints.serializer(), constraints)
                is ScheduleConstraints -> encodeSerializableElement(descriptor, 2, ScheduleConstraints.serializer(), constraints)
                is StringConstraints -> encodeSerializableElement(descriptor, 2, StringConstraints.serializer(), constraints)
                is UnknownConstraints -> encodeSerializableElement(descriptor, 2, UnknownConstraints.serializer(), constraints)
            }
        }
    }
}
