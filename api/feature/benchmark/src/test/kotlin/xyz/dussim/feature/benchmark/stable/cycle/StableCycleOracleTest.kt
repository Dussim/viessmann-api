package xyz.dussim.feature.benchmark.stable.cycle

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import xyz.dussim.viessmann.feature.api.Feature

val StableCycleOracleTest by testSuite {
    test("stable cycle has one response per step") {
        StableCycleFixture.responses().keys.toList() shouldContainExactly StableCycleStep.entries
    }

    test("every stable cycle response decodes to its expected features and resolves its expected outcome") {
        val responses = StableCycleFixture.responses()

        for (step in StableCycleStep.entries) {
            withClue(step.id) {
                val features = StableCycleFixture.decode(responses.getValue(step))

                features shouldHaveSize step.expectedFeatureCount
                features.map { it.feature }.toSet() shouldHaveSize step.expectedFeatureCount
                StableCycleFixture.consume(features) shouldBe step.expected
            }
        }
    }

    test("stable cycle steps differ from the base response except for re-enabling") {
        val responses = StableCycleFixture.responses()
        val base = StableCycleFixture.decode(responses.getValue(StableCycleStep.BASE))

        fun content(step: StableCycleStep) = StableCycleFixture.decode(responses.getValue(step)).map { it.withoutTimestamp() }

        val baseContent = base.map { it.withoutTimestamp() }
        for (step in StableCycleStep.entries) {
            withClue(step.id) {
                when (step) {
                    StableCycleStep.BASE, StableCycleStep.FEATURES_ENABLED -> content(step) shouldBe baseContent
                    else -> content(step) shouldNotBe baseContent
                }
            }
        }
        responses.getValue(StableCycleStep.FEATURES_ENABLED) shouldNotBe responses.getValue(StableCycleStep.BASE)
    }

    test("stable cycle responses do not depend on the OpenAPI input") {
        for (response in StableCycleFixture.responses().values) {
            response shouldNotContain "all_features"
            response shouldNotContain "xyz.dussim.viessmann.api.features.generated"
        }
    }
}

private data class FeatureContent(
    val feature: String,
    val isEnabled: Boolean,
    val properties: Map<String, Any?>,
    val commands: Map<String, Any?>,
)

private fun Feature.withoutTimestamp() =
    FeatureContent(
        feature = feature,
        isEnabled = isEnabled,
        properties = properties.toMap(),
        commands = commands.toMap(),
    )
