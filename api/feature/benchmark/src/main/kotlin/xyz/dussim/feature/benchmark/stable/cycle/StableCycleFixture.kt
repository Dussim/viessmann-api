package xyz.dussim.feature.benchmark.stable.cycle

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.dussim.feature.benchmark.billion.definitions.BillionCircuitFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionProgramFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionPumpFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionSensorFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionSystemFeature
import xyz.dussim.feature.benchmark.billion.definitions.BillionThermostatFeature
import xyz.dussim.feature.benchmark.billion.definitions.descriptor
import xyz.dussim.feature.benchmark.stable.StableFixtureRepository
import xyz.dussim.viessmann.api.models.ResponseData
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureRegistry
import xyz.dussim.viessmann.feature.api.ViessmannFeature

/**
 * What one cycle resolved: `1` or `0` for each static feature, the number of typed features found by wildcard name
 * for each indexed kind, and how many of the first [StableCycleFixture.BY_INDEX_LOOKUPS] indices resolved by index.
 */
data class StableCycleOutcome(
    val system: Int,
    val thermostat: Int,
    val program: Int,
    val pumps: Int,
    val sensors: Int,
    val circuits: Int,
    val circuitsByIndex: Int,
    val sensorsByIndex: Int,
)

/**
 * Successive responses of one polled device. Every step is the base snapshot with one kind of change applied, and a
 * timestamp five minutes after the previous step's.
 *
 * [expectedFeatureCount] and [expected] are the oracle: the number of features in the response and what a cycle over
 * it must resolve.
 */
enum class StableCycleStep(
    val id: String,
    val expectedFeatureCount: Int,
    val expected: StableCycleOutcome,
) {
    /** 1 system, 1 thermostat, 1 program, 4 pumps, 8 sensors, 4 circuits and 30 features no descriptor matches. */
    BASE("base", 49, StableCycleOutcome(1, 1, 1, 4, 8, 4, 4, 4)),

    /** Same features, different property values. */
    VALUES_CHANGED("values-changed", 49, StableCycleOutcome(1, 1, 1, 4, 8, 4, 4, 4)),

    /** Two pumps, two sensors, one circuit and five unmatched features appear. */
    FEATURES_ADDED("features-added", 59, StableCycleOutcome(1, 1, 1, 6, 10, 5, 4, 4)),

    /** The program, one pump, two sensors, one circuit and five unmatched features disappear. */
    FEATURES_REMOVED("features-removed", 39, StableCycleOutcome(1, 1, 0, 3, 6, 3, 3, 4)),

    /** The thermostat, circuit 1 and sensors 2 and 3 are disabled: present, but without properties and commands. */
    FEATURES_DISABLED("features-disabled", 49, StableCycleOutcome(1, 0, 1, 4, 6, 3, 3, 2)),

    /** The disabled features are back; the content equals the base snapshot. */
    FEATURES_ENABLED("features-enabled", 49, StableCycleOutcome(1, 1, 1, 4, 8, 4, 4, 4)),

    /**
     * Circuit 0 reports a property with another type and sensor 0 a command without its parameter, so both stop
     * matching. The program loses its optional command and pump 1 gains a property; both still match.
     */
    STRUCTURE_CHANGED("structure-changed", 49, StableCycleOutcome(1, 1, 1, 4, 7, 3, 3, 3)),
    ;

    companion object {
        fun fromId(id: String): StableCycleStep = entries.single { it.id == id }
    }
}

/**
 * Versioned input and workload of the stable full-cycle benchmarks: decode a response, build a registry, look up
 * typed features. It uses the benchmark-owned `billion.*` feature definitions and the checked-in feature templates,
 * so it does not depend on the OpenAPI input.
 *
 * The templates, the snapshot composition, the steps and [consume] together define the workload; results are
 * comparable only while all of them are unchanged.
 */
object StableCycleFixture {
    private const val TEMPLATES_RESOURCE = "stable-cycle/v1/templates.json"

    // SHA-256 of the templates with carriage returns removed, so the checkout's line endings do not matter.
    private const val TEMPLATES_SHA256 = "94841b2aa5242ad19a9ff3dd23b7cb2b8e74685765f243cbff5f5fefcaad2c8f"

    private const val PUMP_COUNT = 4
    private const val SENSOR_COUNT = 8
    private const val CIRCUIT_COUNT = 4
    private const val UNRELATED_COUNT = 30

    /** A cycle looks up circuits and sensors `0..<4` by index, whether or not the response contains them. */
    const val BY_INDEX_LOOKUPS = 4

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    private val systemDescriptor = BillionSystemFeature.descriptor
    private val thermostatDescriptor = BillionThermostatFeature.descriptor
    private val programDescriptor = BillionProgramFeature.descriptor
    private val pumpDescriptor = BillionPumpFeature.descriptor
    private val sensorDescriptor = BillionSensorFeature.descriptor
    private val circuitDescriptor = BillionCircuitFeature.descriptor

    /** The response body of every step, in step order. */
    fun responses(): Map<StableCycleStep, String> {
        val templates = loadTemplates()
        return StableCycleStep.entries.associateWith { step ->
            val snapshot = snapshot(templates, step)
            val data = JsonArray(snapshot.values.map { it.with("timestamp", JsonPrimitive(timestamp(step))) })
            json.encodeToString(JsonElement.serializer(), JsonObject(mapOf("data" to data)))
        }
    }

    fun decode(response: String): List<Feature> = json.decodeFromString(ResponseData.serializer(ViessmannFeature.serializer()), response).data

    /**
     * What a consumer does with one decoded response: build a registry, read the static features, read every indexed
     * feature of each kind, and read the first circuits and sensors by index.
     */
    fun consume(features: List<Feature>): StableCycleOutcome {
        val registry = FeatureRegistry(features)
        val system = registry.findOf(systemDescriptor, systemDescriptor.byWildcardNameThenStructure)
        val thermostat = registry.findOf(thermostatDescriptor, thermostatDescriptor.byWildcardNameThenStructure)
        val program = registry.findOf(programDescriptor, programDescriptor.byWildcardNameThenStructure)
        val pumps = registry.allOf(pumpDescriptor, pumpDescriptor.byWildcardNameThenStructure)
        val sensors = registry.allOf(sensorDescriptor, sensorDescriptor.byWildcardNameThenStructure)
        val circuits = registry.allOf(circuitDescriptor, circuitDescriptor.byWildcardNameThenStructure)

        var circuitsByIndex = 0
        var sensorsByIndex = 0
        for (index in 0..<BY_INDEX_LOOKUPS) {
            if (registry.findOf(circuitDescriptor, circuitDescriptor.byNameThenStructure(index)) != null) circuitsByIndex++
            if (registry.findOf(sensorDescriptor, sensorDescriptor.byNameThenStructure(index)) != null) sensorsByIndex++
        }

        return StableCycleOutcome(
            system = if (system != null) 1 else 0,
            thermostat = if (thermostat != null) 1 else 0,
            program = if (program != null) 1 else 0,
            pumps = pumps.size,
            sensors = sensors.size,
            circuits = circuits.size,
            circuitsByIndex = circuitsByIndex,
            sensorsByIndex = sensorsByIndex,
        )
    }

    private fun loadTemplates(): Map<String, JsonObject> {
        val text =
            checkNotNull(StableCycleFixture::class.java.classLoader.getResourceAsStream(TEMPLATES_RESOURCE)) {
                "Missing stable cycle templates '$TEMPLATES_RESOURCE'"
            }.bufferedReader().use { it.readText() }
        val actualHash = StableFixtureRepository.sha256(text.replace("\r", ""))
        check(actualHash == TEMPLATES_SHA256) {
            "Stable cycle templates SHA-256 mismatch: expected=$TEMPLATES_SHA256, actual=$actualHash"
        }
        return json
            .parseToJsonElement(text)
            .jsonObject
            .getValue("data")
            .jsonArray
            .associate { element ->
                val feature = element.jsonObject
                feature.getValue("feature").jsonPrimitive.content to feature
            }
    }

    private fun timestamp(step: StableCycleStep): String = "2000-01-01T00:${(step.ordinal * 5).toString().padStart(2, '0')}:00Z"

    private fun pumpName(index: Int) = "billion.pumps.$index"

    private fun sensorName(index: Int) = "billion.sensors.$index.temperature"

    private fun circuitName(index: Int) = "billion.circuits.$index"

    private fun unrelatedName(index: Int) = "billion.unrelated.$index"

    /** The features of [step]'s response by name, in response order. */
    private fun snapshot(
        templates: Map<String, JsonObject>,
        step: StableCycleStep,
    ): LinkedHashMap<String, JsonObject> {
        val pump = templates.getValue("billion.pumps.0")
        val sensor = templates.getValue("billion.sensors.0.temperature")
        val circuit = templates.getValue("billion.circuits.0")
        val unrelated = templates.getValue("billion.unrelated")

        val features = LinkedHashMap<String, JsonObject>()

        fun add(
            template: JsonObject,
            name: String,
        ) {
            features[name] = template.with("feature", JsonPrimitive(name)).with("uri", JsonPrimitive("https://billion.invalid/$name"))
        }

        fun change(
            name: String,
            transform: (JsonObject) -> JsonObject,
        ) {
            features[name] = transform(features.getValue(name))
        }

        fun remove(name: String) {
            checkNotNull(features.remove(name)) { "Stable cycle snapshot has no feature '$name' to remove" }
        }

        add(templates.getValue("billion.system"), "billion.system")
        add(templates.getValue("billion.thermostat"), "billion.thermostat")
        add(templates.getValue("billion.program"), "billion.program")
        for (index in 0..<PUMP_COUNT) add(pump, pumpName(index))
        for (index in 0..<SENSOR_COUNT) add(sensor, sensorName(index))
        for (index in 0..<CIRCUIT_COUNT) add(circuit, circuitName(index))
        for (index in 0..<UNRELATED_COUNT) add(unrelated, unrelatedName(index))

        when (step) {
            StableCycleStep.BASE, StableCycleStep.FEATURES_ENABLED -> {
                Unit
            }

            StableCycleStep.VALUES_CHANGED -> {
                change("billion.thermostat") { it.withPropertyValue("temperature", JsonPrimitive(22.5)) }
                for (index in 0..<PUMP_COUNT step 2) change(pumpName(index)) { it.withPropertyValue("status", JsonPrimitive("off")) }
                for (index in 0..<SENSOR_COUNT) change(sensorName(index)) { it.withPropertyValue("value", JsonPrimitive(13.5 + index)) }
                for (index in 0..<CIRCUIT_COUNT) {
                    change(circuitName(index)) {
                        it
                            .withPropertyValue("supplyTemperature", JsonPrimitive(47.5))
                            .withPropertyValue("returnTemperature", JsonPrimitive(36.5))
                    }
                }
            }

            StableCycleStep.FEATURES_ADDED -> {
                for (index in PUMP_COUNT..<PUMP_COUNT + 2) add(pump, pumpName(index))
                for (index in SENSOR_COUNT..<SENSOR_COUNT + 2) add(sensor, sensorName(index))
                add(circuit, circuitName(CIRCUIT_COUNT))
                for (index in UNRELATED_COUNT..<UNRELATED_COUNT + 5) add(unrelated, unrelatedName(index))
            }

            StableCycleStep.FEATURES_REMOVED -> {
                remove("billion.program")
                remove(pumpName(PUMP_COUNT - 1))
                remove(sensorName(SENSOR_COUNT - 1))
                remove(sensorName(SENSOR_COUNT - 2))
                remove(circuitName(CIRCUIT_COUNT - 1))
                for (index in UNRELATED_COUNT - 5..<UNRELATED_COUNT) remove(unrelatedName(index))
            }

            StableCycleStep.FEATURES_DISABLED -> {
                for (name in listOf("billion.thermostat", circuitName(1), sensorName(2), sensorName(3))) {
                    change(name) { it.disabled() }
                }
            }

            StableCycleStep.STRUCTURE_CHANGED -> {
                change(circuitName(0)) {
                    it.withProperty("pressure", JsonObject(mapOf("type" to JsonPrimitive("string"), "value" to JsonPrimitive("high"))))
                }
                change(sensorName(0)) { it.withCommandParams("calibrate", JsonObject(emptyMap())) }
                change("billion.program") { it.withoutCommand("rename") }
                change(pumpName(1)) {
                    it.withProperty(
                        "power",
                        JsonObject(mapOf("type" to JsonPrimitive("number"), "value" to JsonPrimitive(35.0), "unit" to JsonPrimitive("watt"))),
                    )
                }
            }
        }
        return features
    }

    private fun JsonObject.with(
        key: String,
        value: JsonElement,
    ): JsonObject = JsonObject(this + (key to value))

    private fun JsonObject.withProperty(
        name: String,
        property: JsonObject,
    ): JsonObject = with("properties", getValue("properties").jsonObject.with(name, property))

    private fun JsonObject.withPropertyValue(
        name: String,
        value: JsonPrimitive,
    ): JsonObject {
        val property = getValue("properties").jsonObject.getValue(name).jsonObject
        return withProperty(name, property.with("value", value))
    }

    private fun JsonObject.withCommandParams(
        name: String,
        params: JsonObject,
    ): JsonObject {
        val commands = getValue("commands").jsonObject
        return with("commands", commands.with(name, commands.getValue(name).jsonObject.with("params", params)))
    }

    private fun JsonObject.withoutCommand(name: String): JsonObject {
        val commands = getValue("commands").jsonObject
        check(name in commands) { "Stable cycle feature has no command '$name' to remove" }
        return with("commands", JsonObject(commands - name))
    }

    private fun JsonObject.disabled(): JsonObject =
        with("isEnabled", JsonPrimitive(false))
            .with("properties", JsonObject(emptyMap()))
            .with("commands", JsonObject(emptyMap()))
}
