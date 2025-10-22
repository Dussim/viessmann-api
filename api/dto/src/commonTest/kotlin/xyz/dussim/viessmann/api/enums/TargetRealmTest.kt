package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class TargetRealmTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                TargetRealm.Dc,
                TargetRealm.Genesis,
            ) {
                TargetRealm.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "DC" to TargetRealm.Dc,
                "Genesis" to TargetRealm.Genesis,
            ) { (name, expected) ->
                TargetRealm.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                TargetRealm.entries,
            ) { entry ->
                TargetRealm.valueOf(entry.name) shouldBe entry
            }
        }
    })
