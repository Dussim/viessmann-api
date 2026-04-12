package xyz.dussim.viessmann.client.core

import io.ktor.http.ParametersBuilder

fun ParametersBuilder.appendIfNotNull(
    name: String,
    value: Any?,
) {
    if (value != null) {
        append(name, value.toString())
    }
}

fun ParametersBuilder.appendAllIfNotEmpty(
    name: String,
    values: Iterable<String>?,
) {
    values?.forEach { append(name, it) }
}
