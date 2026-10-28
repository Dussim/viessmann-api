package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

val CanonicalKeyJvmTest by testSuite {
    test("canonicalKey returns the literal instance for a runtime-built string") {
        val runtime = buildString { append("canonical-key-literal") }
        (runtime === "canonical-key-literal") shouldBe false

        runtime.canonicalKey() shouldBeSameInstanceAs "canonical-key-literal"
    }
}
