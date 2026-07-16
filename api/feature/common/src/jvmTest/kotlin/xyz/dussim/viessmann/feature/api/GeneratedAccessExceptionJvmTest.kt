@file:OptIn(ViessmannApiInternalExceptionUsage::class)

package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.feature.api.validation.propertyHash

val GeneratedAccessExceptionJvmTest by testSuite {
    fun hash(name: String): Long = propertyHash(name.hashCode(), name.length)

    test("GeneratedAccessException singleton is stackless on JVM") {
        val exception =
            shouldThrow<GeneratedAccessException> {
                throw GeneratedAccessException
            }

        exception shouldBe GeneratedAccessException
        exception.stackTrace.size shouldBe 0
        exception.cause shouldBe null
    }

    test("GeneratedAccessException thrown from access helpers is stackless on JVM") {
        val properties = EfficientStringKeyMap<Property>(emptyMap())
        val exception =
            shouldThrow<GeneratedAccessException> {
                properties.requireProperty("temperature", hash("temperature"))
            }

        exception shouldBe GeneratedAccessException
        exception.stackTrace.size shouldBe 0
    }
}
