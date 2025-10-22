package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class NetworkStatusTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                NetworkStatus.Online,
                NetworkStatus.Offline,
            ) {
                NetworkStatus.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Online" to NetworkStatus.Online,
                "Offline" to NetworkStatus.Offline,
            ) { (name, expected) ->
                NetworkStatus.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                NetworkStatus.entries,
            ) { entry ->
                NetworkStatus.valueOf(entry.name) shouldBe entry
            }
        }
    })
