package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.BooleanConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.NumberConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.ScheduleConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureCommandParamConstraints.StringConstraints
import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ScheduleProperty.ScheduleEntry
import kotlin.reflect.cast

inline fun <reified T : ViessmannFeatureCommandParamConstraints<*>> ViessmannFeature.constraint(
    command: String,
    param: String,
): T {
    if (T::class == ViessmannFeatureCommandParamConstraints::class) {
        error("Cannot get constraints for generic type ViessmannFeatureCommandParamConstraints, use subclass")
    }

    val viessmannFeatureCommand =
        requireNotNull(commands[command]) {
            "Command $command not found in feature $feature, available commands: ${commands.keys}"
        }

    val viessmannFeatureCommandParam =
        requireNotNull(viessmannFeatureCommand.params[param]) {
            "Parameter $param not found in command $command, available parameters: ${viessmannFeatureCommand.params.keys}"
        }

    return T::class.cast(viessmannFeatureCommandParam.constraints)
}

inline fun <reified T : ViessmannFeatureCommandParamConstraints<*>> ViessmannFeature.constraint(param: String) =
    ViessmannFeatureCommandParamConstraintsProvider { command ->
        constraint<T>(command, param)
    }

fun ViessmannFeature.stringConstraints(param: String) = constraint<StringConstraints>(param)

fun ViessmannFeature.numberConstraints(param: String) = constraint<NumberConstraints>(param)

fun ViessmannFeature.scheduleConstraints(param: String) = constraint<ScheduleConstraints>(param)

fun ViessmannFeature.booleanConstraints(param: String) = constraint<BooleanConstraints>(param)

val ViessmannFeatureCommandParamConstraints<String>.minLength get() = (this as StringConstraints).minLength
val ViessmannFeatureCommandParamConstraints<String>.maxLength get() = (this as StringConstraints).maxLength
val ViessmannFeatureCommandParamConstraints<String>.regEx get() = (this as StringConstraints).regEx
val ViessmannFeatureCommandParamConstraints<String>.enum get() = (this as StringConstraints).enum

val ViessmannFeatureCommandParamConstraints<Double>.min get() = (this as NumberConstraints).min
val ViessmannFeatureCommandParamConstraints<Double>.max get() = (this as NumberConstraints).max
val ViessmannFeatureCommandParamConstraints<Double>.stepping get() = (this as NumberConstraints).stepping

val ViessmannFeatureCommandParamConstraints<Map<String, List<ScheduleEntry>>>.modes get() = (this as ScheduleConstraints).modes
val ViessmannFeatureCommandParamConstraints<Map<String, List<ScheduleEntry>>>.maxEntries get() = (this as ScheduleConstraints).maxEntries
val ViessmannFeatureCommandParamConstraints<Map<String, List<ScheduleEntry>>>.resolution get() = (this as ScheduleConstraints).resolution
val ViessmannFeatureCommandParamConstraints<Map<String, List<ScheduleEntry>>>.defaultMode get() = (this as ScheduleConstraints).defaultMode
val ViessmannFeatureCommandParamConstraints<Map<String, List<ScheduleEntry>>>.overlapAllowed get() = (this as ScheduleConstraints).overlapAllowed
