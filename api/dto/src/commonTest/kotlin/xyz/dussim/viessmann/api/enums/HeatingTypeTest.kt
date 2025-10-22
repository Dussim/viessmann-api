package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class HeatingTypeTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                HeatingType.None,
                HeatingType.FloorHeating,
                HeatingType.Radiators,
                HeatingType.Both,
                HeatingType.Undefined,
            ) {
                HeatingType.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "None" to HeatingType.None,
                "FloorHeating" to HeatingType.FloorHeating,
                "Radiators" to HeatingType.Radiators,
                "Both" to HeatingType.Both,
                "Undefined" to HeatingType.Undefined,
            ) { (name, expected) ->
                HeatingType.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                HeatingType.entries,
            ) { entry ->
                HeatingType.valueOf(entry.name) shouldBe entry
            }
        }
    })
