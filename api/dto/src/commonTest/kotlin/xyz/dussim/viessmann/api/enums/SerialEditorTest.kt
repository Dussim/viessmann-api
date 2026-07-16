package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val SerialEditorTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            SerialEditor.User,
            SerialEditor.DeviceCommunication,
            SerialEditor.Supporter,
        ).forEach { item ->
            test(item.name) {
                SerialEditor.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "User" to SerialEditor.User,
            "DeviceCommunication" to SerialEditor.DeviceCommunication,
            "Supporter" to SerialEditor.Supporter,
        ).forEach { (name, expected) ->
            test(name) {
                SerialEditor.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        SerialEditor.entries.forEach { entry ->
            test(entry.name) {
                SerialEditor.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
