package xyz.dussim.feature.benchmark.stable.small.definitions

import xyz.dussim.viessmann.api.feature.annotations.CommandName
import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command2
import xyz.dussim.viessmann.feature.api.DoubleValue
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.NullableStringValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.StringValue

@GenerateFeatureImplementation("benchmark.stable.small")
interface StableSmallFeature : Feature {
    val p00: BooleanValue

    val p01: DoubleValue

    val p02: StableMode

    val p03: NullableStringValue?

    val c00: C00

    val c01: C01?

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
    interface C00 : Command2<Boolean, Double> {
        val a0: BooleanConstraints

        val a1: NumberConstraints

        companion object
    }

    @CommandName("c01")
    interface C01 : Command2<String, List<Double>> {
        val a0: StringConstraints

        val a1: ArrayNumberConstraints

        companion object
    }

    companion object
}
