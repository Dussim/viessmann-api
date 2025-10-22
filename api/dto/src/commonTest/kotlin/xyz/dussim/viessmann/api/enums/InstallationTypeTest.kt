package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class InstallationTypeTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                InstallationType.Residential,
                InstallationType.Commercial,
            ) {
                InstallationType.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Residential" to InstallationType.Residential,
                "Commercial" to InstallationType.Commercial,
            ) { (name, expected) ->
                InstallationType.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                InstallationType.entries,
            ) { entry ->
                InstallationType.valueOf(entry.name) shouldBe entry
            }
        }
    })
