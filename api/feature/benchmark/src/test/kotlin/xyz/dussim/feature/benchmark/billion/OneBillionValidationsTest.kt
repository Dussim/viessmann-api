package xyz.dussim.feature.benchmark.billion

import de.infix.testBalloon.framework.core.TestConfig
import de.infix.testBalloon.framework.core.disable
import de.infix.testBalloon.framework.core.testScope
import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import xyz.dussim.feature.benchmark.billion.definitions.BillionCircuitFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionProgramFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionPumpFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionSensorFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionSystemFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionThermostatFeature
import xyz.dussim.feature.benchmark.billion.definitions.descriptor
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import kotlin.time.TimeSource

private const val FIXTURE_RESOURCE = "one-billion-validations/features.json"
private const val ENABLE_VARIABLE = "ONE_BILLION_VALIDATIONS"
private const val MIN_VALIDATIONS = 1_000_000_000L

// The fixture is decoded this many times, so the run uses distinct instances like a real response would.
private const val FIXTURE_COPIES = 500

// EfficientStringKeyMap picks its representation from the size: dedicated classes up to 4 entries, a jump table
// from 5, and a pass-through for maps with a key of 64+ characters.
private const val N_ELEMENTS_SIZE = 5
private const val PASS_THROUGH_KEY_LENGTH = 64

private const val URI = "https://billion.invalid/"

/** [matchingUris] are the fixture entries the descriptor's structure matcher must accept, in fixture order. */
private class Expectation<F : Feature>(
    val descriptor: FeatureDescriptor<F>,
    val matchingUris: List<String>,
) {
    private val matcher = descriptor.byStructure

    /** The features [descriptor] accepts, each converted to [F]. */
    fun matchAndConvert(features: List<Feature>): List<F> = features.filter(matcher::matches).map(descriptor::getOrThrow)

    /**
     * The work of [matchAndConvert] (a structure validation per feature, a conversion per match) without building a
     * result list, so the run measures validation and conversion rather than collection growth.
     */
    fun countMatchAndConvert(features: List<Feature>): Int {
        var count = 0
        for (feature in features) {
            if (matcher.matches(feature)) {
                descriptor.getOrThrow(feature)
                count++
            }
        }
        return count
    }
}

private val expectations =
    listOf(
        Expectation(
            BillionSystemFeature.descriptor,
            listOf("${URI}billion.system"),
        ),
        Expectation(
            BillionPumpFeature.descriptor,
            listOf(
                "${URI}billion.pumps.0",
                "${URI}billion.pumps.1/long-property-name",
                "${URI}billion.combined",
            ),
        ),
        Expectation(
            BillionThermostatFeature.descriptor,
            listOf("${URI}billion.thermostat", "${URI}billion.combined"),
        ),
        Expectation(
            BillionSensorFeature.descriptor,
            listOf("${URI}billion.sensors.0.temperature"),
        ),
        Expectation(
            BillionProgramFeature.descriptor,
            listOf("${URI}billion.program/with-optional-command", "${URI}billion.program/without-optional-command"),
        ),
        Expectation(
            BillionCircuitFeature.descriptor,
            listOf("${URI}billion.circuits.0"),
        ),
    )

private val json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

private val fixtureText: String by lazy {
    checkNotNull(Expectation::class.java.classLoader.getResource(FIXTURE_RESOURCE)) {
        "Missing test resource '$FIXTURE_RESOURCE'"
    }.readText()
}

private fun decodeFixture(): List<Feature> = json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), fixtureText).data

/** The representation kind a map of these keys gets: its size, capped at [N_ELEMENTS_SIZE], or "pass-through". */
private fun representation(keys: Set<String>): String =
    when {
        keys.size >= N_ELEMENTS_SIZE && keys.any { it.length >= PASS_THROUGH_KEY_LENGTH } -> "pass-through"
        keys.size >= N_ELEMENTS_SIZE -> "N"
        else -> keys.size.toString()
    }

private val enabled = System.getenv(ENABLE_VARIABLE).toBoolean()

val OneBillionValidationsTest by testSuite {
    test("fixture exercises every map representation for properties, commands and parameters") {
        val features = decodeFixture()
        val everySize = listOf("0", "1", "2", "3", "4", "N")

        withClue("properties") {
            features.map { representation(it.properties.keys) }.toSet() shouldContainAll everySize + "pass-through"
        }
        withClue("commands") {
            features.map { representation(it.commands.keys) }.toSet() shouldContainAll everySize
        }
        withClue("parameters") {
            features
                .flatMap { it.commands.values }
                .map { representation(it.params.keys) }
                .toSet() shouldContainAll everySize
        }
    }

    test("each descriptor matches exactly its expected fixture entries") {
        val features = decodeFixture()

        for (expectation in expectations) {
            withClue(expectation.descriptor.wildcardName) {
                expectation.matchAndConvert(features).map(Feature::uri) shouldBe expectation.matchingUris
                expectation.countMatchAndConvert(features) shouldBe expectation.matchingUris.size
            }
        }
    }

    // Opt-in (set ONE_BILLION_VALIDATIONS=true): these are long-running smoke/perf runs, not regular unit tests. Like
    // the original test, the run happens single-threaded and with one concurrent task per descriptor and round.
    // TestScope is disabled because its default timeout is far shorter than a run.
    val billionConfig = (if (enabled) TestConfig else TestConfig.disable()).testScope(isEnabled = false)
    test("one billion validations", testConfig = billionConfig) {
        runOneBillionValidations("single-threaded", parallel = false)
    }
    test("one billion validations, parallel", testConfig = billionConfig) {
        runOneBillionValidations("parallel", parallel = true)
    }
}

private suspend fun runOneBillionValidations(
    name: String,
    parallel: Boolean,
) {
    val features = List(FIXTURE_COPIES) { decodeFixture() }.flatten()
    val validationsPerRound = expectations.size.toLong() * features.size
    // Rounded up: the descriptor and fixture counts do not divide one billion evenly.
    val rounds = (MIN_VALIDATIONS + validationsPerRound - 1) / validationsPerRound
    val expectedCounts = expectations.map { it.matchingUris.size * FIXTURE_COPIES }
    val step = maxOf(1L, rounds / 10)

    fun validate(
        index: Int,
        round: Long,
    ) {
        val actual = expectations[index].countMatchAndConvert(features)
        if (actual != expectedCounts[index]) {
            throw AssertionError(
                "[$name] round $round: ${expectations[index].descriptor.wildcardName} matched $actual features, " +
                    "expected ${expectedCounts[index]}",
            )
        }
    }

    val start = TimeSource.Monotonic.markNow()
    for (round in 0L until rounds) {
        if (parallel) {
            coroutineScope {
                expectations.indices
                    .map { index -> async(Dispatchers.Default) { validate(index, round) } }
                    .awaitAll()
            }
        } else {
            for (index in expectations.indices) validate(index, round)
        }
        if ((round + 1) % step == 0L) {
            println("[$name] ${round + 1}/$rounds rounds after ${start.elapsedNow()}")
        }
    }
    println("[$name] ${rounds * validationsPerRound} validations finished in ${start.elapsedNow()}")
}
