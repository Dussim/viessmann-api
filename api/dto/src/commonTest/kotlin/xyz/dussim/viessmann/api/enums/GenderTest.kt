package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class GenderTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                Gender.Male,
                Gender.Female,
                Gender.Other,
            ) {
                Gender.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "MALE" to Gender.Male,
                "FEMALE" to Gender.Female,
                "OTHER" to Gender.Other,
            ) { (name, expected) ->
                Gender.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                Gender.entries,
            ) { entry ->
                Gender.valueOf(entry.name) shouldBe entry
            }
        }
    })
