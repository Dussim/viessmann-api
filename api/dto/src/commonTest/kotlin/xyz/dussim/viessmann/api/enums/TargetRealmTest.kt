package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val TargetRealmTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            TargetRealm.Dc,
            TargetRealm.Genesis,
        ).forEach { item ->
            test(item.name) {
                TargetRealm.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "DC" to TargetRealm.Dc,
            "Genesis" to TargetRealm.Genesis,
        ).forEach { (name, expected) ->
            test(name) {
                TargetRealm.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        TargetRealm.entries.forEach { entry ->
            test(entry.name) {
                TargetRealm.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
