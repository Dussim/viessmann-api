package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class OwnershipTypeTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                OwnershipType.ResidentialEndUser,
                OwnershipType.PreCommissioning,
                OwnershipType.Oma,
                OwnershipType.CommercialV1,
                OwnershipType.CommercialV2,
                OwnershipType.None,
            ) {
                OwnershipType.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "ResidentialEndUser" to OwnershipType.ResidentialEndUser,
                "PreCommissioning" to OwnershipType.PreCommissioning,
                "Oma" to OwnershipType.Oma,
                "CommercialV1" to OwnershipType.CommercialV1,
                "CommercialV2" to OwnershipType.CommercialV2,
                "None" to OwnershipType.None,
            ) { (name, expected) ->
                OwnershipType.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                OwnershipType.entries,
            ) { entry ->
                OwnershipType.valueOf(entry.name) shouldBe entry
            }
        }
    })
