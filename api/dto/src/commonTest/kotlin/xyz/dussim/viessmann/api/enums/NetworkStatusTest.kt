package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val NetworkStatusTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            NetworkStatus.Online,
            NetworkStatus.Offline,
        ).forEach { item ->
            test(item.name) {
                NetworkStatus.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Online" to NetworkStatus.Online,
            "Offline" to NetworkStatus.Offline,
        ).forEach { (name, expected) ->
            test(name) {
                NetworkStatus.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        NetworkStatus.entries.forEach { entry ->
            test(entry.name) {
                NetworkStatus.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
