package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val HeatingTypeTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            HeatingType.None,
            HeatingType.FloorHeating,
            HeatingType.Radiators,
            HeatingType.Both,
            HeatingType.Undefined,
        ).forEach { item ->
            test(item.name) {
                HeatingType.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "None" to HeatingType.None,
            "FloorHeating" to HeatingType.FloorHeating,
            "Radiators" to HeatingType.Radiators,
            "Both" to HeatingType.Both,
            "Undefined" to HeatingType.Undefined,
        ).forEach { (name, expected) ->
            test(name) {
                HeatingType.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        HeatingType.entries.forEach { entry ->
            test(entry.name) {
                HeatingType.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
