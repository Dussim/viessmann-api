package xyz.dussim.viessmann.feature.api

interface FeatureEnumFactory<in I : PropertyValue<*>, out T> {
    operator fun invoke(propertyValue: I): T
}
