package xyz.dussim.viessmann.feature.api

// JS strings are compared by value with `===`, so there is nothing to canonicalize.
@PublishedApi
internal actual fun String.canonicalKey(): String = this
