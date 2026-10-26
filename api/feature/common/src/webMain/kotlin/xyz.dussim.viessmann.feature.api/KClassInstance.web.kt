package xyz.dussim.viessmann.feature.api

import kotlin.reflect.KClass

internal actual fun KClass<*>.hasInstance(value: Any?): Boolean = isInstance(value)
