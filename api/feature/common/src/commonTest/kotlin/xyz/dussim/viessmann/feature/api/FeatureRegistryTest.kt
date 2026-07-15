package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import xyz.dussim.viessmann.feature.api.validation.Valid
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationRule
import kotlin.time.Instant

val FeatureRegistryTest by testSuite {
    test("caching registry does not cache matcher misses as conversion misses") {
        val registry = FeatureRegistry.caching(roomTemperatureFeatures())

        registry[RoomTemperatureFeatureDescriptor, 2].feature shouldBe "rooms.2.sensors.temperature"

        registry
            .allOf(RoomTemperatureFeatureDescriptor, RoomTemperatureFeatureDescriptor.byWildcardNameThenStructure)
            .map { it.feature }
            .shouldContainExactly("rooms.1.sensors.temperature", "rooms.2.sensors.temperature")
    }

    test("caching registry allOf matcher misses do not poison later exact lookup") {
        val registry = FeatureRegistry.caching(roomTemperatureFeatures())

        registry
            .allOf(RoomTemperatureFeatureDescriptor, RoomTemperatureFeatureDescriptor.byNameThenStructure(2))
            .map { it.feature }
            .shouldContainExactly("rooms.2.sensors.temperature")

        registry[RoomTemperatureFeatureDescriptor, 1].feature shouldBe "rooms.1.sensors.temperature"
    }

    test("caching registry findOf ignores cached conversions for non-matching features") {
        val registry = FeatureRegistry.caching(roomTemperatureFeatures())

        registry
            .findOf(RoomTemperatureFeatureDescriptor, RoomTemperatureFeatureDescriptor.byNameThenStructure(1))
            ?.feature shouldBe "rooms.1.sensors.temperature"

        registry
            .findOf(RoomTemperatureFeatureDescriptor, RoomTemperatureFeatureDescriptor.byNameThenStructure(2))
            ?.feature shouldBe "rooms.2.sensors.temperature"
    }

    test("descriptor structure matchers use fail-fast validation") {
        var structureCalls = 0
        var failFastCalls = 0
        val structure =
            ValidationRule<Feature, ValidationError> {
                structureCalls++
                Valid()
            }
        val failFast =
            ValidationRule<Feature, ValidationError> {
                failFastCalls++
                Valid()
            }
        val descriptor =
            indexedFeatureDescriptor(
                wildcardName = "rooms.{N}.sensors.temperature",
                rule = structure,
                failFast = failFast,
                factory = ::RoomTemperatureFeature,
            )
        val feature = testFeature("rooms.1.sensors.temperature")

        descriptor.byStructure.matches(feature) shouldBe true
        descriptor.byWildcardNameThenStructure.matches(feature) shouldBe true
        descriptor.byNameThenStructure(1).matches(feature) shouldBe true

        structureCalls shouldBe 0
        failFastCalls shouldBe 3

        descriptor.validate(feature).isInvalid shouldBe false
        structureCalls shouldBe 1
    }
}

private class RoomTemperatureFeature(
    private val delegate: Feature,
) : Feature by delegate

private val alwaysValid = ValidationRule<Feature, ValidationError> { Valid() }

private val RoomTemperatureFeatureDescriptor =
    indexedFeatureDescriptor(
        wildcardName = "rooms.{N}.sensors.temperature",
        rule = alwaysValid,
        failFast = alwaysValid,
        factory = ::RoomTemperatureFeature,
    )

private fun roomTemperatureFeatures(): List<Feature> =
    listOf(
        testFeature("rooms.1.sensors.temperature"),
        testFeature("rooms.2.sensors.temperature"),
    )

private fun testFeature(feature: String): Feature =
    ViessmannFeature(
        feature = feature,
        isEnabled = true,
        isReady = true,
        apiVersion = 1,
        timestamp = Instant.fromEpochMilliseconds(0),
        uri = "/features/$feature",
        properties = EfficientStringKeyMap(emptyMap()),
        commands = EfficientStringKeyMap(emptyMap()),
    )
