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

fun ParametersBuilder.appendIfNotNull(vararg parameters: Pair<String, Any?>) {
    parameters.forEach { (name, value) ->
        appendIfNotNull(name, value)
    }
}

fun ParametersBuilder.appendAllIfNotEmpty(
    name: String,
    values: Iterable<String>?,
) {
    values?.forEach { append(name, it) }
}

fun ParametersBuilder.appendAllIfNotEmpty(vararg parameters: Pair<String, Iterable<String>?>) {
    parameters.forEach { (name, values) ->
        appendAllIfNotEmpty(name, values)
    }
}
