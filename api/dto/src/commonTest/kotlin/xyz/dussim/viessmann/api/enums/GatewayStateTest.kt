package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class GatewayStateTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                GatewayState.Produced,
                GatewayState.Registered,
            ) {
                GatewayState.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Produced" to GatewayState.Produced,
                "Registered" to GatewayState.Registered,
            ) { (name, expected) ->
                GatewayState.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                GatewayState.entries,
            ) { entry ->
                GatewayState.valueOf(entry.name) shouldBe entry
            }
        }
    })
