package xyz.dussim.viessmann.feature.api

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import xyz.dussim.viessmann.feature.api.validation.ExpectedActualClass
import xyz.dussim.viessmann.feature.api.validation.Valid
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import xyz.dussim.viessmann.feature.api.validation.ValidationResult
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

    test("single-pass descriptor converts in one pass and throws only from getOrThrow") {
        var ruleCalls = 0
        var createCalls = 0
        val invalidRule =
            ValidationRule<Feature, ValidationError> {
                ruleCalls++
                ValidationResult.Invalid(ValidationError.MissingComponent("temperature", ExpectedActualClass.of(0)))
            }
        val accepted = testFeature("rooms.1.sensors.temperature")
        val descriptor =
            indexedFeatureDescriptor(
                wildcardName = "rooms.{N}.sensors.temperature",
                rule = invalidRule,
                featureType = "RoomTemperatureFeature",
            ) { feature ->
                createCalls++
                if (feature === accepted) RoomTemperatureFeature(feature) else null
            }
        val rejected = testFeature("rooms.2.sensors.temperature")

        descriptor.getOrNull(accepted).shouldNotBeNull()
        descriptor.getOrNull(rejected) shouldBe null
        createCalls shouldBe 2
        ruleCalls shouldBe 0

        val exception = shouldThrow<FeatureValidationException> { descriptor.getOrThrow(rejected) }
        exception.featureClass shouldBe "RoomTemperatureFeature"
        ruleCalls shouldBe 0
        exception.validationResult.isInvalid shouldBe true
        ruleCalls shouldBe 1
    }

    test("descriptor getOrNull rejects invalid structure without invoking the throwing factory") {
        var factoryCalls = 0
        val invalidRule =
            ValidationRule<Feature, ValidationError> {
                ValidationResult.Invalid(
                    ValidationError.MissingComponent("temperature", ExpectedActualClass.of(0)),
                )
            }
        val descriptor =
            staticFeatureDescriptor(
                wildcardName = "rooms.temperature",
                rule = invalidRule,
                factory = { feature ->
                    factoryCalls++
                    RoomTemperatureFeature(feature)
                },
            )

        descriptor.getOrNull(testFeature("rooms.temperature")) shouldBe null
        factoryCalls shouldBe 0
    }

    test("descriptor getOrNull constructs valid features and passes through typed features") {
        var matcherCalls = 0
        var factoryCalls = 0
        val validRule =
            ValidationRule<Feature, ValidationError> {
                matcherCalls++
                Valid()
            }
        val descriptor =
            staticFeatureDescriptor(
                wildcardName = "rooms.temperature",
                rule = validRule,
                factory = { feature ->
                    factoryCalls++
                    RoomTemperatureFeature(feature)
                },
            )
        val generic = testFeature("rooms.temperature")
        val converted = descriptor.getOrNull(generic)

        converted?.feature shouldBe generic.feature
        matcherCalls shouldBe 1
        factoryCalls shouldBe 1

        descriptor.getOrNull(converted!!) shouldBe converted
        matcherCalls shouldBe 1
        factoryCalls shouldBe 1
    }

    test("indexed descriptor reuses matchers for indices 0 to 15 and creates them beyond") {
        val descriptor = RoomTemperatureFeatureDescriptor

        descriptor.byNameThenStructure(0) shouldBeSameInstanceAs descriptor.byNameThenStructure(0)
        descriptor.byNameThenStructure(15) shouldBeSameInstanceAs descriptor.byNameThenStructure(15)
        descriptor.byName(15) shouldBeSameInstanceAs descriptor.byName(15)
        descriptor.byNameThenStructure(1) shouldNotBeSameInstanceAs descriptor.byNameThenStructure(2)
        descriptor.byNameThenStructure(16) shouldNotBeSameInstanceAs descriptor.byNameThenStructure(16)
        descriptor.byName(16) shouldNotBeSameInstanceAs descriptor.byName(16)

        for (index in listOf(0, 1, 15, 16, 120)) {
            val matching = testFeature("rooms.$index.sensors.temperature")
            val other = testFeature("rooms.${index + 1}.sensors.temperature")

            descriptor.byName(index).matches(matching) shouldBe true
            descriptor.byName(index).matches(other) shouldBe false
            descriptor.byNameThenStructure(index).matches(matching) shouldBe true
            descriptor.byNameThenStructure(index).matches(other) shouldBe false
        }
    }

    for ((kind, createRegistry) in registryFactories()) {
        test("$kind registry runs a descriptor structure rule once per feature") {
            var failFastCalls = 0
            val failFast =
                ValidationRule<Feature, ValidationError> {
                    failFastCalls++
                    Valid()
                }
            val descriptor =
                indexedFeatureDescriptor(
                    wildcardName = "rooms.{N}.sensors.temperature",
                    rule = alwaysValid,
                    failFast = failFast,
                    factory = ::RoomTemperatureFeature,
                )
            val registry = createRegistry(roomTemperatureFeatures())

            val first = registry[descriptor, 2]
            first.feature shouldBe "rooms.2.sensors.temperature"
            failFastCalls shouldBe 1

            registry[descriptor, 2] shouldBeSameInstanceAs first
            registry.firstOf(descriptor, descriptor.byNameThenStructure(2)) shouldBeSameInstanceAs first
            registry.findOf(descriptor, descriptor.byNameThenStructure(2)) shouldBeSameInstanceAs first
            failFastCalls shouldBe 1

            registry.allOf(descriptor, descriptor.byWildcardNameThenStructure).map { it.feature } shouldContainExactly
                listOf("rooms.1.sensors.temperature", "rooms.2.sensors.temperature")
            failFastCalls shouldBe 2
            registry[descriptor].feature shouldBe "rooms.1.sensors.temperature"
            failFastCalls shouldBe 2
        }

        test("$kind registry remembers a rejecting structure rule without converting") {
            var failFastCalls = 0
            var factoryCalls = 0
            val rejecting =
                ValidationRule<Feature, ValidationError> {
                    failFastCalls++
                    ValidationResult.Invalid(ValidationError.MissingComponent("temperature", ExpectedActualClass.of(0)))
                }
            val descriptor =
                indexedFeatureDescriptor(
                    wildcardName = "rooms.{N}.sensors.temperature",
                    rule = rejecting,
                    failFast = rejecting,
                    factory = { feature ->
                        factoryCalls++
                        RoomTemperatureFeature(feature)
                    },
                )
            val registry = createRegistry(roomTemperatureFeatures())

            registry.findOf(descriptor, descriptor.byNameThenStructure(1)) shouldBe null
            shouldThrow<NoSuchElementException> { registry[descriptor, 1] }
            failFastCalls shouldBe 1
            factoryCalls shouldBe 0
        }

        test("$kind registry converts through a single-pass descriptor without a separate structure pass") {
            var ruleCalls = 0
            var createCalls = 0
            val rule =
                ValidationRule<Feature, ValidationError> {
                    ruleCalls++
                    Valid()
                }
            val descriptor =
                indexedFeatureDescriptor(
                    wildcardName = "rooms.{N}.sensors.temperature",
                    rule = rule,
                    featureType = "RoomTemperatureFeature",
                ) { feature ->
                    createCalls++
                    if (feature.feature == "rooms.2.sensors.temperature") RoomTemperatureFeature(feature) else null
                }
            val registry = createRegistry(roomTemperatureFeatures())

            val converted = registry[descriptor, 2]
            converted.feature shouldBe "rooms.2.sensors.temperature"
            registry[descriptor, 2] shouldBeSameInstanceAs converted
            createCalls shouldBe 1

            registry.findOf(descriptor, descriptor.byNameThenStructure(1)) shouldBe null
            shouldThrow<NoSuchElementException> { registry[descriptor, 1] }
            createCalls shouldBe 2

            registry.allOf(descriptor, descriptor.byWildcardNameThenStructure) shouldContainExactly listOf(converted)
            registry[descriptor] shouldBeSameInstanceAs converted
            createCalls shouldBe 2
            ruleCalls shouldBe 0
        }

        test("$kind registry still validates features that already have the descriptor type") {
            var ruleCalls = 0
            var createCalls = 0
            val rejecting =
                ValidationRule<Feature, ValidationError> {
                    ruleCalls++
                    ValidationResult.Invalid(ValidationError.MissingComponent("temperature", ExpectedActualClass.of(0)))
                }
            val descriptor =
                indexedFeatureDescriptor(
                    wildcardName = "rooms.{N}.sensors.temperature",
                    rule = rejecting,
                    featureType = "RoomTemperatureFeature",
                ) { feature ->
                    createCalls++
                    feature as? RoomTemperatureFeature
                }
            val registry = createRegistry(listOf(RoomTemperatureFeature(testFeature("rooms.1.sensors.temperature"))))

            registry.findOf(descriptor, descriptor.byNameThenStructure(1)) shouldBe null
            shouldThrow<NoSuchElementException> { registry[descriptor] }
            ruleCalls shouldBe 1
            createCalls shouldBe 0
        }
    }
}

// A function rather than a property: the suite body above may run before later top-level properties are initialized.
private fun registryFactories(): List<Pair<String, (List<Feature>) -> FeatureRegistry>> =
    listOf(
        "indexed" to { features: List<Feature> -> FeatureRegistry.indexed(features) },
        "caching" to { features: List<Feature> -> FeatureRegistry.caching(features) },
    )

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
