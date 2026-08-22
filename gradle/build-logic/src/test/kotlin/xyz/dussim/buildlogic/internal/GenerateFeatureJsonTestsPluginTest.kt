package xyz.dussim.buildlogic.internal

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import xyz.dussim.buildlogic.groupFeatureJsonFiles
import java.io.File

val GenerateFeatureJsonTestsPluginTest by testSuite {
    test("merges case variants without dropping feature JSON tests") {
        val groups =
            groupFeatureJsonFiles(
                listOf(
                    File("fuelcell.status.extended.json"),
                    File("fuelCell.operating.phase.json"),
                    File("heating.configuration.mode.json"),
                ),
            )

        groups.map { it.specClassName } shouldContainExactly
            listOf("FuelCellFeaturesJsonTest", "HeatingFeaturesJsonTest")

        val fuelCellGroup = groups.first { it.specClassName == "FuelCellFeaturesJsonTest" }
        fuelCellGroup.originalGroupNames shouldContainExactly listOf("fuelCell", "fuelcell")
        fuelCellGroup.files.map { it.name } shouldContainExactly
            listOf("fuelCell.operating.phase.json", "fuelcell.status.extended.json")
    }

    test("grouping is independent of input order") {
        val input =
            listOf(
                File("fuelcell.status.extended.json"),
                File("fuelCell.operating.phase.json"),
                File("heating.configuration.mode.json"),
            )

        val forward = groupFeatureJsonFiles(input)
        val reversed = groupFeatureJsonFiles(input.reversed())

        forward.map { it.specClassName } shouldBe reversed.map { it.specClassName }
        forward.map { group -> group.files.map { it.name } } shouldBe
            reversed.map { group -> group.files.map { it.name } }
    }

    test("merges groups that sanitize to the same identifier") {
        val groups =
            groupFeatureJsonFiles(
                listOf(
                    File("foo-bar.first.json"),
                    File("foo_bar.second.json"),
                    File("other.value.json"),
                ),
            )

        groups.map { it.specClassName } shouldContainExactly
            listOf("Foo_barFeaturesJsonTest", "OtherFeaturesJsonTest")
        groups.first { it.specClassName == "Foo_barFeaturesJsonTest" }.files.map { it.name } shouldContainExactly
            listOf("foo-bar.first.json", "foo_bar.second.json")
    }
}
