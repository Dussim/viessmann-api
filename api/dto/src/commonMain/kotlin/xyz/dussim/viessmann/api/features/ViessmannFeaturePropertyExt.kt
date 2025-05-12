package xyz.dussim.viessmann.api.features

import xyz.dussim.viessmann.api.features.ViessmannFeatureProperty.ArrayProperty.ArrayContent
import kotlin.reflect.KProperty

inline operator fun <T> ViessmannFeatureProperty<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>,
): T = value

inline operator fun <T> ViessmannFeatureProperty<List<ArrayContent<T>>>.getValue(
    thisRef: Any?,
    property: KProperty<*>,
): List<T> = value.map { it.value }
