package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class InvitationStatusTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                InvitationStatus.Pending,
                InvitationStatus.Accepted,
                InvitationStatus.Rejected,
                InvitationStatus.Canceled,
                InvitationStatus.Expired,
                InvitationStatus.Terminated,
            ) {
                InvitationStatus.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Pending" to InvitationStatus.Pending,
                "Accepted" to InvitationStatus.Accepted,
                "Rejected" to InvitationStatus.Rejected,
                "Canceled" to InvitationStatus.Canceled,
                "Expired" to InvitationStatus.Expired,
                "Terminated" to InvitationStatus.Terminated,
            ) { (name, expected) ->
                InvitationStatus.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                InvitationStatus.entries,
            ) { entry ->
                InvitationStatus.valueOf(entry.name) shouldBe entry
            }
        }
    })
