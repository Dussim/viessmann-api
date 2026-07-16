package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val OwnershipTypeTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            OwnershipType.ResidentialEndUser,
            OwnershipType.PreCommissioning,
            OwnershipType.Oma,
            OwnershipType.CommercialV1,
            OwnershipType.CommercialV2,
            OwnershipType.None,
        ).forEach { item ->
            test(item.name) {
                OwnershipType.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "ResidentialEndUser" to OwnershipType.ResidentialEndUser,
            "PreCommissioning" to OwnershipType.PreCommissioning,
            "Oma" to OwnershipType.Oma,
            "CommercialV1" to OwnershipType.CommercialV1,
            "CommercialV2" to OwnershipType.CommercialV2,
            "None" to OwnershipType.None,
        ).forEach { (name, expected) ->
            test(name) {
                OwnershipType.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        OwnershipType.entries.forEach { entry ->
            test(entry.name) {
                OwnershipType.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
