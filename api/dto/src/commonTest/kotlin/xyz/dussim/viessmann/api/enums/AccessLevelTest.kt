package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class AccessLevelTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
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
            ) {
                AccessLevel.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
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
            ) { (name, expected) ->
                AccessLevel.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                AccessLevel.entries,
            ) { entry ->
                AccessLevel.valueOf(entry.name) shouldBe entry
            }
        }
    })
