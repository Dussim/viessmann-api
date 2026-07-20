package xyz.dussim.feature.benchmark.stable.medium.definitions

import kotlinx.serialization.json.JsonObject
import xyz.dussim.viessmann.api.feature.annotations.CommandName
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command0
import xyz.dussim.viessmann.feature.api.Command4
import xyz.dussim.viessmann.feature.api.Command8
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.EnergyMatrix
import xyz.dussim.viessmann.feature.api.EnergyMatrixConstraints
import xyz.dussim.viessmann.feature.api.EnergyMatrixValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.ListDoubleValue
import xyz.dussim.viessmann.feature.api.ListStringValue
import xyz.dussim.viessmann.feature.api.NullableBooleanValue
import xyz.dussim.viessmann.feature.api.NullableDoubleValue
import xyz.dussim.viessmann.feature.api.NullableStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectConstraints
import xyz.dussim.viessmann.feature.api.ObjectOtherRoomConfigurationValue
import xyz.dussim.viessmann.feature.api.Schedule
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.ScheduleValue
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue

@GenerateFeatureImplementation("benchmark.stable.medium")
interface StableMediumFeature : Feature {
    val p00: BooleanValue

    val p01: DoubleValue

    val p02: StableMode

    val p03: NullableStringValue?

    val p04: ListDoubleValue

    val p05: ListStringValue

    val p06: ObjectOtherRoomConfigurationValue

    val p07: ScheduleValue

    val p08: NullableBooleanValue

    val p09: NullableDoubleValue

    val p10: StringValue

    val p11: EnergyMatrixValue

    val p12: BooleanValue

    val p13: DoubleValue

    val p14: ListStringValue

    val p15: StringValue

    val c00: C00

    val c01: C01

    val c02: C02

    val c03: C03

    val c04: C04

    val c05: C05

    val c06: C06

    val c07: C07?

    @FeatureEnum
    enum class StableMode {
        IDLE,
        ACTIVE,
        ;

        @FeatureEnum.Factory
        companion object : FeatureEnumFactory<StringValue, StableMode> {
            override fun invoke(propertyValue: StringValue): StableMode = valueOf(propertyValue.element.uppercase())
        }
    }

    @CommandName("c00")
    interface C00 : Command0 {
        companion object
    }

    @CommandName("c01")
    interface C01 : Command8<Boolean, Double, String, List<Double>, JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        val a4: ObjectConstraints

        val a5: ScheduleConstraints

        val a6: EnergyMatrixConstraints

        val a7: ArrayStringConstraints

        companion object
    }

    @CommandName("c02")
    interface C02 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c03")
    interface C03 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c04")
    interface C04 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c05")
    interface C05 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c06")
    interface C06 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c07")
    interface C07 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    companion object
}
