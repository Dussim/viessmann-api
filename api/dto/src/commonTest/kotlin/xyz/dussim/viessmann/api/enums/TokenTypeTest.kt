package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class TokenTypeTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                TokenType.Invited,
                TokenType.Requested,
            ) {
                TokenType.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Invited" to TokenType.Invited,
                "Requested" to TokenType.Requested,
            ) { (name, expected) ->
                TokenType.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                TokenType.entries,
            ) { entry ->
                TokenType.valueOf(entry.name) shouldBe entry
            }
        }
    })
