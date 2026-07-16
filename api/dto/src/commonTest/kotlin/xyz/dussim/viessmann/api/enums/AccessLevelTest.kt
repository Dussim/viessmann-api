package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val AccessLevelTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            AccessLevel.Owner,
            AccessLevel.FamilyMember,
            AccessLevel.Maintainer,
            AccessLevel.Support,
            AccessLevel.Installer,
            AccessLevel.ServiceContractor,
            AccessLevel.Operator,
            AccessLevel.BuildingManager,
            AccessLevel.CommercialOwner,
            AccessLevel.Partner,
            AccessLevel.Consumer,
        ).forEach { item ->
            test(item.name) {
                AccessLevel.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Owner" to AccessLevel.Owner,
            "FamilyMember" to AccessLevel.FamilyMember,
            "Maintainer" to AccessLevel.Maintainer,
            "Support" to AccessLevel.Support,
            "Installer" to AccessLevel.Installer,
            "ServiceContractor" to AccessLevel.ServiceContractor,
            "Operator" to AccessLevel.Operator,
            "BuildingManager" to AccessLevel.BuildingManager,
            "CommercialOwner" to AccessLevel.CommercialOwner,
            "Partner" to AccessLevel.Partner,
            "Consumer" to AccessLevel.Consumer,
        ).forEach { (name, expected) ->
            test(name) {
                AccessLevel.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        AccessLevel.entries.forEach { entry ->
            test(entry.name) {
                AccessLevel.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
