@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import xyz.dussim.viessmann.feature.api.validation.BOOLEAN_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.NUMBER_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.SCHEDULE_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.STRING_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.UNKNOWN_CONSTRAINTS_CLASS_INDEX
import kotlin.jvm.JvmName
import kotlin.jvm.JvmRecord

@JvmRecord
@Serializable
data class Command(
    val uri: String,
    val name: String,
    val isExecutable: Boolean,
    @Serializable(with = ParametersSerializer::class)
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
    val constraintsClassIndex: Int
}

@JvmRecord
@Serializable
data class UnknownConstraints(
    val jsonElement: JsonElement,
) : Constraints<Nothing> {
    override val constraintsClassIndex get() = UNKNOWN_CONSTRAINTS_CLASS_INDEX
}

@Serializable
data object BooleanConstraints : Constraints<Boolean> {
    override val constraintsClassIndex get() = BOOLEAN_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class NumberConstraints(
    val min: Double? = null,
    val efficientLowerBorder: Double? = null,
    val efficientUpperBorder: Double? = null,
    val max: Double? = null,
    val stepping: Double? = null,
    val enum: List<Double>? = null,
) : Constraints<Double> {
    override val constraintsClassIndex get() = NUMBER_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class StringConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val regEx: String? = null,
    val enum: List<String>? = null,
    val sameDayAllowed: Boolean? = null,
) : Constraints<String> {
    override val constraintsClassIndex get() = STRING_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ScheduleConstraints(
    val modes: List<String>,
    val maxEntries: Int,
    val resolution: Int,
    val defaultMode: String,
    val overlapAllowed: Boolean,
) : Constraints<Map<String, List<Schedule>>> {
    override val constraintsClassIndex get() = SCHEDULE_CONSTRAINTS_CLASS_INDEX
}

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
