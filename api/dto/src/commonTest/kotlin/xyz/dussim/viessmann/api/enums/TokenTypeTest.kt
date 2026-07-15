package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val TokenTypeTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            TokenType.Invited,
            TokenType.Requested,
        ).forEach { item ->
            test(item.name) {
                TokenType.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Invited" to TokenType.Invited,
            "Requested" to TokenType.Requested,
        ).forEach { (name, expected) ->
            test(name) {
                TokenType.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        TokenType.entries.forEach { entry ->
            test(entry.name) {
                TokenType.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
