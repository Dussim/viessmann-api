package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.feature.annotations.FeatureEnum
import xyz.dussim.viessmann.api.feature.annotations.GenerateFeatureImplementation
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureEnumFactory
import xyz.dussim.viessmann.feature.api.StringValue
import kotlin.jvm.JvmRecord

@GenerateFeatureImplementation("heating.boiler.pumps.internal")
interface HeatingBoilerPumpsInternalFeature : Feature.Device {
    companion object;

    @FeatureEnum
    sealed interface Status {
        data object On : Status

        data object Off : Status

        @JvmRecord
        data class Unknown(
            val value: String,
        ) : Status

        @FeatureEnum.Factory
        companion object : FeatureEnumFactory<StringValue, Status> {
            override fun invoke(propertyValue: StringValue): Status =
                when (propertyValue.element) {
                    "on" -> On
                    "off" -> Off
                    else -> Unknown(propertyValue.element)
                }
        }
    }

    val status: Status
}
