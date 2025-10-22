package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class SerialEditorTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                SerialEditor.User,
                SerialEditor.DeviceCommunication,
                SerialEditor.Supporter,
            ) {
                SerialEditor.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "User" to SerialEditor.User,
                "DeviceCommunication" to SerialEditor.DeviceCommunication,
                "Supporter" to SerialEditor.Supporter,
            ) { (name, expected) ->
                SerialEditor.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                SerialEditor.entries,
            ) { entry ->
                SerialEditor.valueOf(entry.name) shouldBe entry
            }
        }
    })
