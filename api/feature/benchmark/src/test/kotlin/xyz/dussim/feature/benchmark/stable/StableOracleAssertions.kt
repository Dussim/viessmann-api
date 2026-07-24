package xyz.dussim.feature.benchmark.stable

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldHaveAtMostSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.instanceOf
import xyz.dussim.viessmann.feature.api.FeatureValidationException

fun assertStableSuite(size: StableSize) {
    val fixtures = StableFixtureRepository.load(verifyManifest = false)
    val suiteFixtures = fixtures.fixtures.filter { it.manifest.size == size }
    StableCase.entries shouldBeSameSizeAs suiteFixtures

    val mismatches =
        suiteFixtures.mapNotNull { fixture ->
            val actual = StableFixtureRepository.sha256(fixture.canonicalJson)
            if (fixture.manifest.sha256 == actual) null else "${fixture.id}=$actual"
        }
    mismatches.shouldBeEmpty()

    suiteFixtures.forEach { fixture ->
        val manifest = fixture.manifest
        val aggregate = fixture.descriptor.structureValidator.validate(fixture.feature)
        val failFast = fixture.descriptor.failFastStructureValidator.validate(fixture.feature)
        val aggregateErrors = aggregate.map { it }
        val failFastErrors = failFast.map { it }

        aggregate.isInvalid shouldBe !manifest.expectedValid
        failFast.isInvalid shouldBe !manifest.expectedValid
        aggregateErrors shouldHaveSize manifest.expectedErrorCount
        aggregateErrors.groupingBy(StableErrorCategory::from).eachCount() shouldContainExactly manifest.expectedCategories
        failFastErrors.firstOrNull()?.let(StableErrorCategory::from) shouldBe manifest.expectedFirstError
        failFastErrors shouldHaveAtMostSize 1

        if (manifest.expectedValid) {
            val constructed = fixture.descriptor.getOrThrow(fixture.feature)
            constructed shouldBe instanceOf(fixture.descriptor.featureClass)
            fixture.descriptor.getOrNull(fixture.feature).shouldNotBeNull()
        } else {
            shouldThrow<FeatureValidationException> {
                fixture.descriptor.getOrThrow(fixture.feature)
            }
            fixture.descriptor.getOrNull(fixture.feature).shouldBeNull()
        }
    }

    fixtures[size, StableCase.OPTIONAL_COMMAND_ABSENT].manifest.expectedValid.shouldBeTrue()
    fixtures[size, StableCase.OPTIONAL_COMMAND_MALFORMED].manifest.expectedValid.shouldBeFalse()

    suiteFixtures.forEach { fixture ->
        fixture.descriptor.featureClass.qualifiedName
            .shouldNotBeNull()
            .shouldStartWith("xyz.dussim.feature.benchmark.stable.${size.id}.definitions.Stable")
        fixture.canonicalJson shouldNotContain "all_features"
        fixture.canonicalJson shouldNotContain "xyz.dussim.viessmann.api.features.generated"
    }
}
