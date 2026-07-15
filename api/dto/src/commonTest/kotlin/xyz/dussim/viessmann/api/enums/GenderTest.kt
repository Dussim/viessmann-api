package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val GenderTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            Gender.Male,
            Gender.Female,
            Gender.Other,
        ).forEach { item ->
            test(item.name) {
                Gender.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "MALE" to Gender.Male,
            "FEMALE" to Gender.Female,
            "OTHER" to Gender.Other,
        ).forEach { (name, expected) ->
            test(name) {
                Gender.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        Gender.entries.forEach { entry ->
            test(entry.name) {
                Gender.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
