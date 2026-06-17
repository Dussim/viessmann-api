package xyz.dussim.viessmann.feature.api

import java.util.IdentityHashMap

@Suppress("FunctionName")
actual fun <K, V> IdentityHashMap(): MutableMap<K, V> = IdentityHashMap()
