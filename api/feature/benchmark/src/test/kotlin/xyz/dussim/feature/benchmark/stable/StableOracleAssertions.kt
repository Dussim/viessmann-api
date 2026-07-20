package xyz.dussim.feature.benchmark.stable

import xyz.dussim.viessmann.feature.api.FeatureValidationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun assertStableSuite(size: StableSize) {
    val fixtures = StableFixtureRepository.load(verifyManifest = false)
    val suiteFixtures = fixtures.fixtures.filter { it.manifest.size == size }
    assertEquals(StableCase.entries.size, suiteFixtures.size, "${size.id} fixture count")

    val mismatches =
        suiteFixtures.mapNotNull { fixture ->
            val actual = StableFixtureRepository.sha256(fixture.canonicalJson)
            if (fixture.manifest.sha256 == actual) null else "${fixture.id}=$actual"
        }
    assertTrue(mismatches.isEmpty(), "Update manifest hashes:\n${mismatches.joinToString("\n")}")

    suiteFixtures.forEach { fixture ->
        val manifest = fixture.manifest
        val aggregate = fixture.descriptor.structureValidator.validate(fixture.feature)
        val failFast = fixture.descriptor.failFastStructureValidator.validate(fixture.feature)
        val aggregateErrors = aggregate.map { it }
        val failFastErrors = failFast.map { it }

        assertEquals(!manifest.expectedValid, aggregate.isInvalid, "${fixture.id} aggregate validity")
        assertEquals(!manifest.expectedValid, failFast.isInvalid, "${fixture.id} fail-fast validity")
        assertEquals(manifest.expectedErrorCount, aggregateErrors.size, "${fixture.id} aggregate count")
        assertEquals(
            manifest.expectedCategories,
            aggregateErrors.groupingBy(StableErrorCategory::from).eachCount(),
            "${fixture.id} aggregate categories",
        )
        assertEquals(
            manifest.expectedFirstError,
            failFastErrors.firstOrNull()?.let(StableErrorCategory::from),
            "${fixture.id} first error",
        )
        assertTrue(failFastErrors.size <= 1, "${fixture.id} fail-fast returned multiple errors")

        if (manifest.expectedValid) {
            val constructed = fixture.descriptor.getOrThrow(fixture.feature)
            assertTrue(fixture.descriptor.featureClass.isInstance(constructed), "${fixture.id} factory type")
            assertNotNull(fixture.descriptor.getOrNull(fixture.feature), "${fixture.id} getOrNull")
        } else {
            assertFailsWith<FeatureValidationException>("${fixture.id} throwing factory accepted an invalid fixture") {
                fixture.descriptor.getOrThrow(fixture.feature)
            }
            assertEquals(null, fixture.descriptor.getOrNull(fixture.feature), "${fixture.id} getOrNull")
        }
    }

    assertTrue(fixtures[size, StableCase.OPTIONAL_COMMAND_ABSENT].manifest.expectedValid)
    assertFalse(fixtures[size, StableCase.OPTIONAL_COMMAND_MALFORMED].manifest.expectedValid)

    suiteFixtures.forEach { fixture ->
        assertTrue(
            fixture.descriptor.featureClass.qualifiedName.orEmpty().startsWith(
                "xyz.dussim.feature.benchmark.stable.${size.id}.definitions.Stable",
            ),
            fixture.id,
        )
        assertFalse(fixture.canonicalJson.contains("all_features"))
        assertFalse(fixture.canonicalJson.contains("xyz.dussim.viessmann.api.features.generated"))
    }
}
