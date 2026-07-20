@file:Suppress("unused")

package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import xyz.dussim.viessmann.feature.api.validation.ARRAY_BOOLEAN_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ARRAY_EMPTY_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ARRAY_NUMBER_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ARRAY_OBJECT_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ARRAY_STRING_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ARRAY_UNKNOWN_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.BOOLEAN_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.ENERGY_MATRIX_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.NUMBER_CONSTRAINTS_CLASS_INDEX
import xyz.dussim.viessmann.feature.api.validation.OBJECT_CONSTRAINTS_CLASS_INDEX
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

sealed interface Constraints<out T> {
    val constraintsClassIndex: Int
}

@JvmRecord
@Serializable
data class UnknownConstraints(
    val jsonElement: JsonElement,
) : Constraints<JsonElement> {
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

@Serializable
data object EnergyMatrixConstraints : Constraints<EnergyMatrix> {
    override val constraintsClassIndex get() = ENERGY_MATRIX_CONSTRAINTS_CLASS_INDEX
}

sealed interface ArrayConstraints

@JvmRecord
@Serializable
data class ArrayEmptyConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
) : ArrayConstraints,
    Constraints<List<Nothing>> {
    override val constraintsClassIndex get() = ARRAY_EMPTY_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ArrayNumberConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val enum: List<Double>? = null,
) : ArrayConstraints,
    Constraints<List<Double>> {
    override val constraintsClassIndex get() = ARRAY_NUMBER_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ArrayStringConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val enum: List<String>? = null,
) : ArrayConstraints,
    Constraints<List<String>> {
    override val constraintsClassIndex get() = ARRAY_STRING_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ArrayBooleanConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val enum: List<Boolean>? = null,
) : ArrayConstraints,
    Constraints<List<Boolean>> {
    override val constraintsClassIndex get() = ARRAY_BOOLEAN_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ArrayObjectConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val enum: List<JsonObject>? = null,
) : ArrayConstraints,
    Constraints<List<JsonObject>> {
    override val constraintsClassIndex get() = ARRAY_OBJECT_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ArrayUnknownConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val enum: List<JsonElement>? = null,
) : ArrayConstraints,
    Constraints<List<JsonElement>> {
    override val constraintsClassIndex get() = ARRAY_UNKNOWN_CONSTRAINTS_CLASS_INDEX
}

@JvmRecord
@Serializable
data class ObjectConstraints(
    val minProperties: Int? = null,
    val maxProperties: Int? = null,
    val required: List<String>? = null,
    val additionalProperties: JsonObject? = null,
) : Constraints<JsonObject> {
    override val constraintsClassIndex get() = OBJECT_CONSTRAINTS_CLASS_INDEX
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

@JvmName("ofArrayEmpty")
fun Parameter.Companion.of(
    constraints: ArrayEmptyConstraints = ArrayEmptyConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayEmpty")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, ArrayEmptyConstraints(minLength, maxLength))

@JvmName("ofArrayNumber")
fun Parameter.Companion.of(
    constraints: ArrayNumberConstraints = ArrayNumberConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayNumber")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    vararg enum: Double,
): Parameter = Parameter(ARRAY, required, ArrayNumberConstraints(minLength, maxLength, enum.toList()))

@JvmName("ofArrayString")
fun Parameter.Companion.of(
    constraints: ArrayStringConstraints = ArrayStringConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayString")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    vararg enum: String,
): Parameter = Parameter(ARRAY, required, ArrayStringConstraints(minLength, maxLength, enum.toList()))

@JvmName("ofArrayBoolean")
fun Parameter.Companion.of(
    constraints: ArrayBooleanConstraints = ArrayBooleanConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayBoolean")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    vararg enum: Boolean,
): Parameter = Parameter(ARRAY, required, ArrayBooleanConstraints(minLength, maxLength, enum.toList()))

@JvmName("ofArrayObject")
fun Parameter.Companion.of(
    constraints: ArrayObjectConstraints = ArrayObjectConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayObject")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    vararg enum: JsonObject,
): Parameter = Parameter(ARRAY, required, ArrayObjectConstraints(minLength, maxLength, enum.toList()))

@JvmName("ofArrayUnknown")
fun Parameter.Companion.of(
    constraints: ArrayUnknownConstraints = ArrayUnknownConstraints(),
    required: Boolean = true,
): Parameter = Parameter(ARRAY, required, constraints)

@JvmName("ofArrayUnknown")
fun Parameter.Companion.of(
    minLength: Int? = null,
    maxLength: Int? = null,
    required: Boolean = true,
    vararg enum: JsonElement,
): Parameter = Parameter(ARRAY, required, ArrayUnknownConstraints(minLength, maxLength, enum.toList()))

@JvmName("ofObject")
fun Parameter.Companion.of(
    constraints: ObjectConstraints = ObjectConstraints(),
    required: Boolean = true,
): Parameter = Parameter(OBJECT, required, constraints)

@JvmName("ofSchedules")
fun Parameter.Companion.of(
    constraints: ScheduleConstraints,
    required: Boolean = true,
): Parameter = Parameter(SCHEDULE, required, constraints)

@JvmName("ofEnergyMatrix")
fun Parameter.Companion.of(
    constraints: EnergyMatrixConstraints = EnergyMatrixConstraints,
    required: Boolean = true,
): Parameter = Parameter(ENERGY_MATRIX, required, constraints)

fun Constraints<*>.toArrayNumberConstraintsOrThrow(): ArrayNumberConstraints = requireArrayConstraintsOrPromoteEmpty(ArrayNumberConstraints::class, ::ArrayNumberConstraints)

fun Constraints<*>.toArrayStringConstraintsOrThrow(): ArrayStringConstraints = requireArrayConstraintsOrPromoteEmpty(ArrayStringConstraints::class, ::ArrayStringConstraints)

fun Constraints<*>.toArrayBooleanConstraintsOrThrow(): ArrayBooleanConstraints = requireArrayConstraintsOrPromoteEmpty(ArrayBooleanConstraints::class, ::ArrayBooleanConstraints)

fun Constraints<*>.toArrayObjectConstraintsOrThrow(): ArrayObjectConstraints = requireArrayConstraintsOrPromoteEmpty(ArrayObjectConstraints::class, ::ArrayObjectConstraints)

fun Constraints<*>.toArrayUnknownConstraintsOrThrow(): ArrayUnknownConstraints = requireArrayConstraintsOrPromoteEmpty(ArrayUnknownConstraints::class, ::ArrayUnknownConstraints)
