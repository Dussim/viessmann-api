package xyz.dussim.viessmann.api.features

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.BooleanConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.NumberConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleMap
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.StringConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.DeviceListProperty.Device
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ScheduleProperty.ScheduleEntry
import xyz.dussim.viessmann.api.features.internal.ArrayPropertySerializer
import xyz.dussim.viessmann.api.features.internal.ObjectPropertySerializer
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.round

interface ViessmannFeature {
    val feature: String

    val isEnabled: Boolean
    val isReady: Boolean

    val apiVersion: Int
    val timestamp: Instant
    val uri: String // TODO multiplatform uri?

    val properties: Map<String, ViessmannFeatureProperty<*>>
    val commands: Map<String, ViessmannFeatureCommand>

    val deviceId: String?
    val gatewayId: String?
    val isActive: Boolean?

    interface Device : ViessmannFeature {
        override val deviceId: String
        override val gatewayId: String
    }

    interface Gateway : ViessmannFeature {
        override val gatewayId: String
    }

    interface Geofencing : ViessmannFeature {
        override val isActive: Boolean
    }
}

@Serializable
sealed interface ViessmannFeatureProperty<T> {
    val value: T
    val unit: String?

    @Serializable
    @SerialName("boolean")
    data class BooleanProperty(
        override val value: Boolean,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<Boolean>

    @Serializable
    @SerialName("number")
    data class NumberProperty(
        override val value: Double,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<Double>

    @Serializable
    @SerialName("string")
    data class StringProperty(
        override val value: String,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<String>

    @Serializable(with = ArrayPropertySerializer::class)
    data class ArrayProperty(
        override val value: List<ArrayContent<*>>,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<List<ArrayContent<*>>> {
        @Serializable
        sealed interface ArrayContent<T> {
            val value: T

            @JvmInline
            @Serializable
            value class DoubleArrayContent(
                override val value: Double,
            ) : ArrayContent<Double>

            @JvmInline
            @Serializable
            value class StringArrayContent(
                override val value: String,
            ) : ArrayContent<String>

            @JvmInline
            @Serializable
            value class DeviceErrorContent(
                override val value: DeviceError,
            ) : ArrayContent<DeviceError>

            @JvmInline
            @Serializable
            value class ZigbeeDeviceStatusContent(
                override val value: ZigbeeDeviceStatus,
            ) : ArrayContent<ZigbeeDeviceStatus>

            @JvmInline
            @Serializable
            value class RoomActorContent(
                override val value: RoomActor,
            ) : ArrayContent<RoomActor>

            @Serializable
            data class DeviceError(
                val errorCode: String,
                val timestamp: Instant,
                val accessLevel: String,
                val priority: String,
                val audiences: List<String>,
            )

            @Serializable
            data class ZigbeeDeviceStatus(
                val device: String,
                val value: String,
            )

            @Serializable
            data class RoomActor(
                val deviceId: String,
                val heatingCircuit: Int,
            )
        }
    }

    @Serializable
    @SerialName("DeviceList")
    data class DeviceListProperty(
        override val value: List<Device>,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<List<Device>> {
        @Serializable
        data class Device(
            val id: String,
            val fingerprint: String,
            val modelId: String,
            val modelVersion: String,
            val type: String,
            val name: String,
            val roles: List<String>,
            val status: String,
        )
    }

    @Serializable
    @SerialName("Schedule")
    data class ScheduleProperty(
        override val value: ScheduleMap,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<ScheduleMap> {
        @Serializable
        data class ScheduleEntry(
            val start: String,
            val end: String,
            val mode: String,
            val position: Int,
            val active: Boolean = true,
        )
    }

    @Serializable(with = ObjectPropertySerializer::class)
    data class ObjectProperty(
        override val value: ObjectContent<*>,
        override val unit: String? = null,
    ) : ViessmannFeatureProperty<ObjectProperty.ObjectContent<*>> {
        @Serializable
        sealed interface ObjectContent<T> {
            val value: T

            @JvmInline
            @Serializable
            value class OtherRoomConfigurationContent(
                override val value: OtherRoomConfiguration,
            ) : ObjectContent<OtherRoomConfiguration>

            @Serializable
            data class OtherRoomConfiguration(
                val hydraulicBalance: Boolean,
                val heatupSpeed: String,
                val trvAlgoActive: Boolean,
                val openPointDetection: Boolean,
                val virtualClimateSensor: Boolean,
                val etrvSync: Boolean,
                val useTrvOpenWindow: Boolean,
                val heatOnTime: Boolean,
            )
        }
    }
}

@Serializable
data class ViessmannFeatureCommand(
    val uri: String,
    val name: String,
    val isExecutable: Boolean,
    val params: Map<String, ViessmannFeatureCommandParam<*>>,
)

@Serializable
sealed interface ViessmannFeatureCommandParam<T : ViessmannFeatureCommandParamConstraints<*>> {
    val required: Boolean
    val constraints: T

    @Serializable
    @SerialName("boolean")
    data class BooleanParam(
        override val required: Boolean,
        override val constraints: BooleanConstraints,
    ) : ViessmannFeatureCommandParam<BooleanConstraints>

    @Serializable
    @SerialName("number")
    data class NumberParam(
        override val required: Boolean,
        @Serializable(with = NumberConstraintsSafeSerializer::class)
        override val constraints: NumberConstraints,
    ) : ViessmannFeatureCommandParam<NumberConstraints>

    @Serializable
    @SerialName("string")
    data class StringParam(
        override val required: Boolean,
        override val constraints: StringConstraints,
    ) : ViessmannFeatureCommandParam<StringConstraints>

    @Serializable
    @SerialName("Schedule")
    data class ScheduleParam(
        override val required: Boolean,
        override val constraints: ScheduleConstraints,
    ) : ViessmannFeatureCommandParam<ScheduleConstraints>
}

@Serializable
sealed interface ViessmannFeatureCommandParamConstraints<T> {
    fun validate(param: T): Boolean

    @Serializable
    data object BooleanConstraints : ViessmannFeatureCommandParamConstraints<Boolean> {
        override fun validate(param: Boolean): Boolean = true
    }

    @Serializable
    data class NumberConstraints(
        val min: Double,
        val max: Double,
        val stepping: Double,
    ) : ViessmannFeatureCommandParamConstraints<Double> {
        @Suppress("MagicNumber")
        override fun validate(param: Double): Boolean {
            // Handle edge cases or invalid inputs
            if (stepping <= 0 || param < min || param > max) {
                return false
            }

            // Calculate how many steps from min to the number
            val steps = (param - min) / stepping

            // Check if steps is (very close to) an integer
            // Using epsilon comparison to handle floating-point precision issues
            val epsilon = 1e-10
            val stepsRounded = round(steps)

            return abs(steps - stepsRounded) < epsilon
        }
    }

    @Serializable
    data class StringConstraints(
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val regEx: String? = null,
        val enum: List<String>? = null,
    ) : ViessmannFeatureCommandParamConstraints<String> {
        override fun validate(param: String): Boolean =
            enum?.contains(param) ?: true &&
                    (minLength == null || param.length >= minLength) &&
                    (maxLength == null || param.length <= maxLength) &&
                    (regEx == null || param.matches(Regex(regEx)))
    }

    @Serializable
    data class ScheduleConstraints(
        val modes: List<String>,
        val maxEntries: Int,
        val resolution: Int,
        val defaultMode: String,
        val overlapAllowed: Boolean,
    ) : ViessmannFeatureCommandParamConstraints<ScheduleMap> {
        override fun validate(param: ScheduleMap): Boolean {
            // Check that all modes are present in the schedule
            // Check that there are no overlapping entries
            // Check that the default mode is present in the schedule
            // Check that the default mode is not disabled
            // Check that the default mode is not in the future
            // Check that the default mode is not in the past
            // Check that the resolution is valid
            // Check that the maxEntries is valid
            // Check that the modes are valid
            TODO("Not yet implemented, complicated logic required")
        }
    }

    @Serializable
    @JvmInline
    value class ScheduleMap(
        val map: Map<String, List<ScheduleEntry>>,
    ) : Map<String, List<ScheduleEntry>> by map
}

/**
 * In feature `rooms.others` I encountered a situation where number constraints were empty enum string array
 * This serializer tries to circumvent this stupid situation
 */
data object NumberConstraintsSafeSerializer : KSerializer<NumberConstraints> {
    private val INVALID_CONSTRAINTS =
        NumberConstraints(
            Double.NaN,
            Double.NaN,
            Double.NaN,
        )

    override val descriptor = NumberConstraints.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: NumberConstraints,
    ) {
        NumberConstraints.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NumberConstraints =
        try {
            decoder.decodeSerializableValue(NumberConstraints.serializer())
        } catch (_: SerializationException) {
            INVALID_CONSTRAINTS
        }
}
