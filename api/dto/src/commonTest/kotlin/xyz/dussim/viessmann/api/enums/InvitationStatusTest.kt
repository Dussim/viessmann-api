package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val InvitationStatusTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            InvitationStatus.Pending,
            InvitationStatus.Accepted,
            InvitationStatus.Rejected,
            InvitationStatus.Canceled,
            InvitationStatus.Expired,
            InvitationStatus.Terminated,
        ).forEach { item ->
            test(item.name) {
                InvitationStatus.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Pending" to InvitationStatus.Pending,
            "Accepted" to InvitationStatus.Accepted,
            "Rejected" to InvitationStatus.Rejected,
            "Canceled" to InvitationStatus.Canceled,
            "Expired" to InvitationStatus.Expired,
            "Terminated" to InvitationStatus.Terminated,
        ).forEach { (name, expected) ->
            test(name) {
                InvitationStatus.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        InvitationStatus.entries.forEach { entry ->
            test(entry.name) {
                InvitationStatus.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
