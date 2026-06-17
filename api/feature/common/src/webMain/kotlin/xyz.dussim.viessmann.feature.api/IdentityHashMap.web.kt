package xyz.dussim.viessmann.feature.api

@Suppress("FunctionName")
actual fun <K, V> IdentityHashMap(): MutableMap<K, V> = HashMap()
