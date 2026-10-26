package xyz.dussim.viessmann.feature.api

import kotlin.reflect.KClass

/**
 * [KClass.isInstance] for the non-primitive, non-function classes used by generated accessors and descriptors.
 * On the JVM, `KClass.isInstance` without kotlin-reflect looks the class up in a map of function classes before it
 * checks the instance; this skips that lookup.
 */
internal expect fun KClass<*>.hasInstance(value: Any?): Boolean
