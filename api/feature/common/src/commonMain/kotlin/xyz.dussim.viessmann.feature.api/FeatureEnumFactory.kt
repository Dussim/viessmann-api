@file:Suppress("unused", "UNCHECKED_CAST")

package xyz.dussim.viessmann.feature.api

import xyz.dussim.viessmann.feature.api.validation.propertyHash

@RequiresOptIn("Unsafe factory creation method that assumes property of given name and type exists")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class UnsafeFactoryCreationMethod

interface FeatureEnumFactory<in I : PropertyValue<*>, out T> {
    operator fun invoke(propertyValue: I): T

    @UnsafeFactoryCreationMethod
    context(feature: Feature)
    operator fun invoke(propertyName: String): T = invoke(feature.properties[propertyName, propertyHash(propertyName.hashCode(), propertyName.length)]!!.value as I)
}
