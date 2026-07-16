package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val GatewayStateTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            GatewayState.Produced,
            GatewayState.Registered,
        ).forEach { item ->
            test(item.name) {
                GatewayState.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Produced" to GatewayState.Produced,
            "Registered" to GatewayState.Registered,
        ).forEach { (name, expected) ->
            test(name) {
                GatewayState.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        GatewayState.entries.forEach { entry ->
            test(entry.name) {
                GatewayState.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
