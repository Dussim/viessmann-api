package xyz.dussim.viessmann.feature.api

import kotlin.reflect.KClass

/**
 * `Class.isInstance` is a JIT intrinsic. Callers never pass primitive or function classes, which are the only cases
 * `KClass.isInstance` handles differently.
 */
internal actual fun KClass<*>.hasInstance(value: Any?): Boolean = java.isInstance(value)
