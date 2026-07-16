package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val InstallationTypeTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            InstallationType.Residential,
            InstallationType.Commercial,
        ).forEach { item ->
            test(item.name) {
                InstallationType.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Residential" to InstallationType.Residential,
            "Commercial" to InstallationType.Commercial,
        ).forEach { (name, expected) ->
            test(name) {
                InstallationType.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        InstallationType.entries.forEach { entry ->
            test(entry.name) {
                InstallationType.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
