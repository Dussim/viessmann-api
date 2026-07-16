package xyz.dussim.viessmann.api.enums

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.api.utils.factories.AccessLevelEntryHolder
import xyz.dussim.viessmann.api.utils.factories.AggregatedStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GatewayStateEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GatewayTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.GenderEntryHolder
import xyz.dussim.viessmann.api.utils.factories.HeatingTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.InstallationTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.InvitationStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.NetworkStatusEntryHolder
import xyz.dussim.viessmann.api.utils.factories.OwnershipTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.SerialEditorEntryHolder
import xyz.dussim.viessmann.api.utils.factories.TargetRealmEntryHolder
import xyz.dussim.viessmann.api.utils.factories.TokenTypeEntryHolder
import xyz.dussim.viessmann.api.utils.factories.ViessmannEnumEntryHolder

val ViessmannEnumTest by testSuite {
    test("All viessmann enums are unique") {
        val allEntryHolders =
            setOf(
                AccessLevelEntryHolder,
                AggregatedStatusEntryHolder,
                GenderEntryHolder,
                HeatingTypeEntryHolder,
                GatewayStateEntryHolder,
                GatewayTypeEntryHolder,
                InstallationTypeEntryHolder,
                InvitationStatusEntryHolder,
                OwnershipTypeEntryHolder,
                TargetRealmEntryHolder,
                NetworkStatusEntryHolder,
                TokenTypeEntryHolder,
                SerialEditorEntryHolder,
            )

        val allEnumsCount = allEntryHolders.sumOf { it.entries.size }

        val uniqueEnumsCount = ViessmannEnumEntryHolder.entries.size

        withClue("Sum of all entry holders should be equal to the number of unique enums in ViessmannEnum.entries") {
            uniqueEnumsCount shouldBe allEnumsCount
        }
    }
}
