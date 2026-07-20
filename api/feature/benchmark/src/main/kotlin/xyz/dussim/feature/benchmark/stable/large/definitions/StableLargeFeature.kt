package xyz.dussim.feature.benchmark.stable.large.definitions

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

@GenerateFeatureImplementation("benchmark.stable.large")
interface StableLargeFeature : Feature {
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

    val p16: BooleanValue

    val p17: DoubleValue

    val p18: StableMode

    val p19: NullableStringValue?

    val p20: ListDoubleValue

    val p21: ListStringValue

    val p22: ObjectOtherRoomConfigurationValue

    val p23: ScheduleValue

    val p24: NullableBooleanValue

    val p25: NullableDoubleValue

    val p26: StringValue

    val p27: EnergyMatrixValue

    val p28: BooleanValue

    val p29: DoubleValue

    val p30: ListStringValue

    val p31: StringValue

    val p32: BooleanValue

    val p33: DoubleValue

    val p34: StableMode

    val p35: NullableStringValue?

    val p36: ListDoubleValue

    val p37: ListStringValue

    val p38: ObjectOtherRoomConfigurationValue

    val p39: ScheduleValue

    val p40: NullableBooleanValue

    val p41: NullableDoubleValue

    val p42: StringValue

    val p43: EnergyMatrixValue

    val p44: BooleanValue

    val p45: DoubleValue

    val p46: ListStringValue

    val p47: StringValue

    val p48: BooleanValue

    val p49: DoubleValue

    val p50: StableMode

    val p51: NullableStringValue?

    val p52: ListDoubleValue

    val p53: ListStringValue

    val p54: ObjectOtherRoomConfigurationValue

    val p55: ScheduleValue

    val p56: NullableBooleanValue

    val p57: NullableDoubleValue

    val p58: StringValue

    val p59: EnergyMatrixValue

    val p60: BooleanValue

    val p61: DoubleValue

    val p62: ListStringValue

    val p63: StringValue

    val c00: C00

    val c01: C01

    val c02: C02

    val c03: C03

    val c04: C04

    val c05: C05

    val c06: C06

    val c07: C07

    val c08: C08

    val c09: C09

    val c10: C10

    val c11: C11

    val c12: C12

    val c13: C13

    val c14: C14

    val c15: C15

    val c16: C16

    val c17: C17

    val c18: C18

    val c19: C19

    val c20: C20

    val c21: C21

    val c22: C22

    val c23: C23

    val c24: C24

    val c25: C25

    val c26: C26

    val c27: C27

    val c28: C28

    val c29: C29

    val c30: C30

    val c31: C31?

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

    @CommandName("c08")
    interface C08 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c09")
    interface C09 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c10")
    interface C10 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c11")
    interface C11 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c12")
    interface C12 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c13")
    interface C13 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c14")
    interface C14 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c15")
    interface C15 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c16")
    interface C16 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c17")
    interface C17 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c18")
    interface C18 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c19")
    interface C19 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c20")
    interface C20 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c21")
    interface C21 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c22")
    interface C22 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c23")
    interface C23 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c24")
    interface C24 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c25")
    interface C25 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c26")
    interface C26 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c27")
    interface C27 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c28")
    interface C28 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c29")
    interface C29 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    @CommandName("c30")
    interface C30 : Command4<Boolean, Double, String, List<Double>> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        val a2: StringConstraints

        val a3: ArrayNumberConstraints

        companion object
    }

    @CommandName("c31")
    interface C31 : Command4<JsonObject, Map<String, List<Schedule>>, EnergyMatrix, List<String>> {
        val a0: ObjectConstraints

        val a1: ScheduleConstraints

        val a2: EnergyMatrixConstraints

        val a3: ArrayStringConstraints

        companion object
    }

    companion object
}
