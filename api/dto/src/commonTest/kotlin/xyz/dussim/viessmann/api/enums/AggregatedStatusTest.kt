package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

val AggregatedStatusTest by testSuite {
    testSuite("EntryHolder contains all strict values") {
        listOf(
            AggregatedStatus.Error,
            AggregatedStatus.Offline,
            AggregatedStatus.Maintenance,
            AggregatedStatus.WorksProperly,
            AggregatedStatus.RemoteDiagnosticSession,
            AggregatedStatus.NbIotConnected,
        ).forEach { item ->
            test(item.name) {
                AggregatedStatus.entries shouldContain item
            }
        }
    }

    testSuite("InstanceFactory produces correct values") {
        listOf(
            "Error" to AggregatedStatus.Error,
            "Offline" to AggregatedStatus.Offline,
            "Maintenance" to AggregatedStatus.Maintenance,
            "WorksProperly" to AggregatedStatus.WorksProperly,
            "RemoteDiagnosticSession" to AggregatedStatus.RemoteDiagnosticSession,
            "NbIotConnected" to AggregatedStatus.NbIotConnected,
        ).forEach { (name, expected) ->
            test(name) {
                AggregatedStatus.valueOf(name) shouldBe expected
            }
        }
    }

    testSuite("EntryHolder entries should all be reconstructed correctly") {
        AggregatedStatus.entries.forEach { entry ->
            test(entry.name) {
                AggregatedStatus.valueOf(entry.name) shouldBe entry
            }
        }
    }
}
