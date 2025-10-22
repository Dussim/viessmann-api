package xyz.dussim.viessmann.api.enums

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class AggregatedStatusTest :
    FunSpec({
        context("EntryHolder contains all strict values") {
            withData(
                nameFn = { it.name },
                AggregatedStatus.Error,
                AggregatedStatus.Offline,
                AggregatedStatus.Maintenance,
                AggregatedStatus.WorksProperly,
                AggregatedStatus.RemoteDiagnosticSession,
                AggregatedStatus.NbIotConnected,
            ) {
                AggregatedStatus.entries shouldContain it
            }
        }

        context("InstanceFactory produces correct values") {
            withData(
                nameFn = { (name, _) -> name },
                "Error" to AggregatedStatus.Error,
                "Offline" to AggregatedStatus.Offline,
                "Maintenance" to AggregatedStatus.Maintenance,
                "WorksProperly" to AggregatedStatus.WorksProperly,
                "RemoteDiagnosticSession" to AggregatedStatus.RemoteDiagnosticSession,
                "NbIotConnected" to AggregatedStatus.NbIotConnected,
            ) { (name, expected) ->
                AggregatedStatus.valueOf(name) shouldBe expected
            }
        }

        context("EntryHolder entries should all be reconstructed correctly") {
            withData(
                nameFn = { it.name },
                AggregatedStatus.entries,
            ) { entry ->
                AggregatedStatus.valueOf(entry.name) shouldBe entry
            }
        }
    })
