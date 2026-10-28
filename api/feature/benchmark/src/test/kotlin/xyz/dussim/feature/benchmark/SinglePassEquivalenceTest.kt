package xyz.dussim.feature.benchmark

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.serialization.json.Json
import xyz.dussim.viessmann.api.features.generated.Descriptors
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureValidationException
import xyz.dussim.viessmann.feature.api.ViessmannFeature

/**
 * Generated `createOrNull` factories replace "validate, then construct", so they must accept exactly what validation
 * accepts. Checks every real descriptor against every real feature from `all_features.json`, which covers far more
 * shapes (absent optionals, empty lists, nullable values, array constraints, extra parameters) than the stable
 * fixtures.
 */
val SinglePassEquivalenceTest by testSuite {
    test("getOrNull and getOrThrow succeed exactly when validation passes for every real feature and descriptor") {
        val features = loadAllFeatures()
        features.shouldNotBeEmpty()

        val mismatches = mutableListOf<String>()
        var converted = 0
        for (descriptor in Descriptors.all) {
            for (feature in features) {
                val failFastValid = !descriptor.failFastStructureValidator.validate(feature).isInvalid
                val aggregateValid = !descriptor.validate(feature).isInvalid
                val orNull = descriptor.getOrNull(feature)
                val orThrow =
                    try {
                        descriptor.getOrThrow(feature)
                    } catch (_: FeatureValidationException) {
                        null
                    }
                if (orNull != null) converted++
                if (failFastValid != aggregateValid || failFastValid != (orNull != null) || failFastValid != (orThrow != null)) {
                    mismatches +=
                        "${descriptor.wildcardName} <- ${feature.feature}: failFast=$failFastValid aggregate=$aggregateValid " +
                        "getOrNull=${orNull != null} getOrThrow=${orThrow != null}"
                }
            }
        }

        mismatches.take(20).shouldBeEmpty()
        converted shouldBeGreaterThan 0
    }
}

private fun loadAllFeatures(): List<Feature> {
    val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            useAlternativeNames = false
        }
    val resource =
        Thread.currentThread().contextClassLoader.getResource("all_features.json")
            ?: error("all_features.json not found on the test classpath; it is generated from the OpenAPI input.")
    return json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), resource.readText()).data
}
