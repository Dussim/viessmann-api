package xyz.dussim.viessmann.feature.api

/**
 * Interned strings share the instances the JVM uses for string literals, so lookups with literal keys from
 * generated rules hit the reference fast path instead of a content comparison.
 */
@PublishedApi
internal actual fun String.canonicalKey(): String = intern()
