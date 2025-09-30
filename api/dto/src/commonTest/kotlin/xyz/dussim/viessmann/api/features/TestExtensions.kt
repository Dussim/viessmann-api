package xyz.dussim.viessmann.api.features

import io.kotest.matchers.be
import io.kotest.matchers.should
import xyz.dussim.viessmann.feature.api.PropertyValue

@Suppress("UNCHECKED_CAST")
infix fun <T> PropertyValue<T>.shouldBeValue(expected: T?): T = element.should(be(expected)) as T

expect fun readFileFromResources(path: String): String
