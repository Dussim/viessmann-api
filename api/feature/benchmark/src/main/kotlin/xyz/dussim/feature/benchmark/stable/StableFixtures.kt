package xyz.dussim.feature.benchmark.stable

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.dussim.feature.benchmark.stable.large.definitions.StableLargeFeature
import xyz.dussim.feature.benchmark.stable.medium.definitions.StableMediumFeature
import xyz.dussim.feature.benchmark.stable.small.definitions.StableSmallFeature
import xyz.dussim.viessmann.feature.api.ArrayNumberConstraints
import xyz.dussim.viessmann.feature.api.ArrayStringConstraints
import xyz.dussim.viessmann.feature.api.BooleanConstraints
import xyz.dussim.viessmann.feature.api.BooleanValue
import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import xyz.dussim.viessmann.feature.api.EnergyMatrix
import xyz.dussim.viessmann.feature.api.EnergyMatrixConstraints
import xyz.dussim.viessmann.feature.api.Feature
import xyz.dussim.viessmann.feature.api.FeatureDescriptor
import xyz.dussim.viessmann.feature.api.NullableBooleanValue
import xyz.dussim.viessmann.feature.api.NumberConstraints
import xyz.dussim.viessmann.feature.api.ObjectConstraints
import xyz.dussim.viessmann.feature.api.OtherRoomConfiguration
import xyz.dussim.viessmann.feature.api.Parameter
import xyz.dussim.viessmann.feature.api.Property
import xyz.dussim.viessmann.feature.api.Schedule
import xyz.dussim.viessmann.feature.api.ScheduleConstraints
import xyz.dussim.viessmann.feature.api.StringConstraints
import xyz.dussim.viessmann.feature.api.ViessmannFeature
import xyz.dussim.viessmann.feature.api.of
import xyz.dussim.viessmann.feature.api.ofEmptyList
import xyz.dussim.viessmann.feature.api.ofNullable
import xyz.dussim.viessmann.feature.api.validation.ValidationError
import java.security.MessageDigest
import kotlin.time.Instant
import xyz.dussim.feature.benchmark.stable.large.definitions.descriptor as largeDescriptor
import xyz.dussim.feature.benchmark.stable.medium.definitions.descriptor as mediumDescriptor
import xyz.dussim.feature.benchmark.stable.small.definitions.descriptor as smallDescriptor

private const val MANIFEST_RESOURCE = "stable-validation/v1/fixture-manifest.json"

enum class StableSize(
    val id: String,
    val propertyCount: Int,
    val commandCount: Int,
    val parameterCount: Int,
) {
    SMALL("small", 4, 2, 4),
    MEDIUM("medium", 16, 8, 32),
    LARGE("large", 64, 32, 128),
    ;

    val outerRuleCount: Int get() = propertyCount + commandCount
    val validationRuleCount: Int get() = propertyCount + commandCount + parameterCount

    companion object {
        fun fromId(id: String): StableSize = entries.single { it.id == id }
    }
}

enum class StableCase(
    val id: String,
) {
    VALID("valid"),
    INVALID_FIRST("invalid-first"),
    INVALID_MIDDLE("invalid-middle"),
    INVALID_LAST("invalid-last"),
    INVALID_ALL("invalid-all"),
    MISSING_PROPERTY("missing-property"),
    MISSING_COMMAND("missing-command"),
    MISSING_REQUIRED_PARAM("missing-required-param"),
    WRONG_PARAM_CONSTRAINT_TYPE("wrong-param-constraint-type"),
    OPTIONAL_COMMAND_ABSENT("optional-command-absent"),
    OPTIONAL_COMMAND_MALFORMED("optional-command-malformed"),
    ;

    companion object {
        fun fromId(id: String): StableCase = entries.single { it.id == id }
    }
}

enum class StableErrorCategory(
    val id: String,
) {
    MISSING_COMPONENT("missing-component"),
    COMPONENT_TYPE_MISMATCH("component-type-mismatch"),
    NUMBER_OF_PARAMETERS_MISMATCH("number-of-parameters-mismatch"),
    ;

    companion object {
        fun from(error: ValidationError): StableErrorCategory =
            when (error) {
                is ValidationError.MissingComponent -> MISSING_COMPONENT
                is ValidationError.ComponentTypeMismatch -> COMPONENT_TYPE_MISMATCH
                is ValidationError.NumberOfParametersMismatch -> NUMBER_OF_PARAMETERS_MISMATCH
            }
    }
}

data class StableFixtureManifestEntry(
    val size: StableSize,
    val case: StableCase,
    val sha256: String,
    val expectedValid: Boolean,
    val expectedErrorCount: Int,
    val expectedFirstError: StableErrorCategory?,
    val expectedCategories: Map<StableErrorCategory, Int>,
    val propertyCount: Int,
    val commandCount: Int,
    val commandParameterCount: Int,
    val outerRuleCount: Int,
    val validationRuleCount: Int,
) {
    val id: String get() = "${size.id}/${case.id}"
}

data class StableFixture(
    val manifest: StableFixtureManifestEntry,
    val canonicalJson: String,
    val feature: Feature,
    val descriptor: FeatureDescriptor<out Feature>,
) {
    val id: String get() = manifest.id
}

class StableFixtureSet internal constructor(
    val schemaVersion: Int,
    val fixtureVersion: String,
    val operationsPerInvocation: Int,
    private val fixtureMap: Map<String, StableFixture>,
) {
    val fixtures: List<StableFixture> get() = fixtureMap.values.toList()

    operator fun get(
        size: StableSize,
        case: StableCase,
    ): StableFixture = fixtureMap.getValue("${size.id}/${case.id}")
}

object StableFixtureRepository {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

    fun load(verifyManifest: Boolean = true): StableFixtureSet {
        val manifestText =
            checkNotNull(StableFixtureRepository::class.java.classLoader.getResourceAsStream(MANIFEST_RESOURCE)) {
                "Missing stable validation manifest '$MANIFEST_RESOURCE'"
            }.bufferedReader().use { it.readText() }
        val root = json.parseToJsonElement(manifestText).jsonObject
        val schemaVersion = root.requiredInt("schemaVersion")
        check(schemaVersion == 1) { "Unsupported stable fixture schema version $schemaVersion" }
        val fixtureVersion = root.requiredString("fixtureVersion")
        val operationsPerInvocation = root.requiredInt("operationsPerInvocation")
        check(operationsPerInvocation == 1) { "Single-case stable benchmarks require exactly one operation per invocation" }

        val fixtures =
            root.getValue("entries").jsonArray.map { element ->
                val entry = parseEntry(element.jsonObject)
                val feature = StableFixtureBuilder.build(entry.size, entry.case)
                val canonicalJson = json.encodeToString(ViessmannFeature.serializer(), feature)
                val actualHash = sha256(canonicalJson)
                if (verifyManifest) {
                    check(entry.sha256 == actualHash) {
                        "Fixture ${entry.id} SHA-256 mismatch: manifest=${entry.sha256}, actual=$actualHash"
                    }
                    check(entry.propertyCount == entry.size.propertyCount)
                    check(entry.commandCount == entry.size.commandCount)
                    check(entry.commandParameterCount == entry.size.parameterCount)
                    check(entry.outerRuleCount == entry.size.outerRuleCount)
                    check(entry.validationRuleCount == entry.size.validationRuleCount)
                }
                StableFixture(entry, canonicalJson, feature, descriptor(entry.size))
            }

        check(fixtures.map { it.id }.toSet().size == fixtures.size) { "Duplicate stable fixture IDs" }
        val expectedIds =
            StableSize.entries
                .flatMap { size ->
                    StableCase.entries.map { case -> "${size.id}/${case.id}" }
                }.toSet()
        check(fixtures.map { it.id }.toSet() == expectedIds) {
            "Manifest must contain the complete stable fixture matrix"
        }

        return StableFixtureSet(
            schemaVersion = schemaVersion,
            fixtureVersion = fixtureVersion,
            operationsPerInvocation = operationsPerInvocation,
            fixtureMap = fixtures.associateBy(StableFixture::id),
        )
    }

    fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun parseEntry(value: JsonObject): StableFixtureManifestEntry {
        val expectedCategories =
            value.getValue("expectedCategories").jsonObject.entries.associate { (key, count) ->
                StableErrorCategory.entries.single { it.id == key } to count.jsonPrimitive.int
            }
        return StableFixtureManifestEntry(
            size = StableSize.fromId(value.requiredString("size")),
            case = StableCase.fromId(value.requiredString("case")),
            sha256 = value.requiredString("sha256"),
            expectedValid =
                value
                    .getValue("expectedValid")
                    .jsonPrimitive.content
                    .toBooleanStrict(),
            expectedErrorCount = value.requiredInt("expectedErrorCount"),
            expectedFirstError =
                value["expectedFirstError"]
                    ?.takeUnless { it is JsonNull }
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.let { id -> StableErrorCategory.entries.single { it.id == id } },
            expectedCategories = expectedCategories,
            propertyCount = value.requiredInt("propertyCount"),
            commandCount = value.requiredInt("commandCount"),
            commandParameterCount = value.requiredInt("commandParameterCount"),
            outerRuleCount = value.requiredInt("outerRuleCount"),
            validationRuleCount = value.requiredInt("validationRuleCount"),
        )
    }

    private fun descriptor(size: StableSize): FeatureDescriptor<out Feature> =
        when (size) {
            StableSize.SMALL -> StableSmallFeature.smallDescriptor
            StableSize.MEDIUM -> StableMediumFeature.mediumDescriptor
            StableSize.LARGE -> StableLargeFeature.largeDescriptor
        }

    private fun JsonObject.requiredString(name: String): String = getValue(name).jsonPrimitive.content

    private fun JsonObject.requiredInt(name: String): Int = getValue(name).jsonPrimitive.int
}

private object StableFixtureBuilder {
    fun build(
        size: StableSize,
        case: StableCase,
    ): ViessmannFeature {
        val base = base(size)
        return when (case) {
            StableCase.VALID -> {
                base
            }

            StableCase.INVALID_FIRST -> {
                base.withWrongProperty(0)
            }

            StableCase.INVALID_MIDDLE -> {
                base.withWrongProperty((size.outerRuleCount - 1) / 2)
            }

            StableCase.INVALID_LAST -> {
                base.withWrongParameter(size.commandCount - 1, commandArity(size, size.commandCount - 1) - 1)
            }

            StableCase.INVALID_ALL -> {
                base.withEveryRuleInvalid(size)
            }

            StableCase.MISSING_PROPERTY -> {
                base.updateProperties { it - propertyName(0) }
            }

            StableCase.MISSING_COMMAND -> {
                base.updateCommands { it - commandName(0) }
            }

            StableCase.MISSING_REQUIRED_PARAM -> {
                val commandIndex = if (size == StableSize.SMALL) 0 else 1
                base.updateCommand(commandIndex) { command ->
                    command.copy(params = EfficientStringKeyMap(command.params - "a0"))
                }
            }

            StableCase.WRONG_PARAM_CONSTRAINT_TYPE -> {
                base.withWrongParameter(if (size == StableSize.SMALL) 0 else 1, 0)
            }

            StableCase.OPTIONAL_COMMAND_ABSENT -> {
                base.updateCommands { it - commandName(size.commandCount - 1) }
            }

            StableCase.OPTIONAL_COMMAND_MALFORMED -> {
                base.withWrongParameter(size.commandCount - 1, 0)
            }
        }
    }

    private fun base(size: StableSize): ViessmannFeature =
        ViessmannFeature(
            feature = "benchmark.stable.${size.id}",
            gatewayId = "stable-gateway",
            deviceId = "stable-device",
            timestamp = Instant.parse("2000-01-01T00:00:00Z"),
            isEnabled = true,
            isReady = true,
            apiVersion = 1,
            uri = "https://benchmark.invalid/stable/${size.id}",
            properties =
                EfficientStringKeyMap(
                    (0 until size.propertyCount).associate { index -> propertyName(index) to property(index) },
                ),
            commands =
                EfficientStringKeyMap(
                    (0 until size.commandCount).associate { index -> commandName(index) to command(size, index) },
                ),
        )

    private fun property(index: Int): Property =
        when (index % 16) {
            0, 12 -> {
                Property.of(true)
            }

            1, 13 -> {
                Property.of(42.0)
            }

            2 -> {
                Property.of("active")
            }

            3 -> {
                Property.ofNullable(string = null)
            }

            4 -> {
                Property.of(1.0, 2.0)
            }

            5 -> {
                Property.of("alpha", "beta")
            }

            6 -> {
                Property.of(
                    OtherRoomConfiguration(
                        hydraulicBalance = true,
                        heatupSpeed = "normal",
                        trvAlgoActive = true,
                        openPointDetection = false,
                        virtualClimateSensor = false,
                        etrvSync = true,
                        useTrvOpenWindow = false,
                        heatOnTime = true,
                    ),
                )
            }

            7 -> {
                Property.of(
                    "monday" to
                        listOf(
                            Schedule(
                                start = "08:00",
                                end = "10:00",
                                mode = "on",
                                position = 0,
                                active = true,
                            ),
                        ),
                )
            }

            8 -> {
                Property.ofNullable(boolean = null)
            }

            9 -> {
                Property.ofNullable(double = null)
            }

            10, 15 -> {
                Property.of("stable")
            }

            11 -> {
                Property.of(EnergyMatrix(emptyList(), emptyList(), emptyList(), emptyList()))
            }

            14 -> {
                Property.ofEmptyList()
            }

            else -> {
                error("Unreachable property pattern")
            }
        }

    private fun command(
        size: StableSize,
        index: Int,
    ): Command =
        Command(
            uri = "https://benchmark.invalid/stable/${size.id}/commands/${commandName(index)}",
            name = commandName(index),
            isExecutable = true,
            params =
                EfficientStringKeyMap(
                    (0 until commandArity(size, index)).associate { parameterIndex ->
                        val adapterIndex =
                            if (index == 1 && size != StableSize.SMALL) {
                                parameterIndex
                            } else {
                                (index * commandArity(size, index) + parameterIndex) % 8
                            }
                        "a$parameterIndex" to parameter(adapterIndex, parameterIndex % 2 == 0)
                    },
                ),
        )

    private fun parameter(
        adapterIndex: Int,
        required: Boolean,
    ): Parameter =
        when (adapterIndex % 8) {
            0 -> {
                Parameter.of(BooleanConstraints, required)
            }

            1 -> {
                Parameter.of(
                    NumberConstraints(
                        min = 0.0,
                        max = 100.0,
                        stepping = 0.5,
                        enum = listOf(42.0),
                    ),
                    required,
                )
            }

            2 -> {
                Parameter.of(
                    StringConstraints(
                        minLength = 1,
                        maxLength = 16,
                        regEx = "^[a-z]+$",
                        enum = listOf("stable"),
                    ),
                    required,
                )
            }

            3 -> {
                Parameter.of(ArrayNumberConstraints(1, 4, listOf(1.0, 2.0)), required)
            }

            4 -> {
                Parameter.of(ObjectConstraints(1, 4, listOf("value")), required)
            }

            5 -> {
                Parameter.of(
                    ScheduleConstraints(
                        modes = listOf("on", "off"),
                        maxEntries = 4,
                        resolution = 10,
                        defaultMode = "off",
                        overlapAllowed = false,
                    ),
                    required,
                )
            }

            6 -> {
                Parameter.of(EnergyMatrixConstraints, required)
            }

            7 -> {
                Parameter.of(ArrayStringConstraints(1, 4, listOf("alpha", "beta")), required)
            }

            else -> {
                error("Unreachable constraint pattern")
            }
        }

    private fun commandArity(
        size: StableSize,
        index: Int,
    ): Int =
        when {
            size == StableSize.SMALL -> 2
            index == 0 -> 0
            index == 1 -> 8
            else -> 4
        }

    private fun ViessmannFeature.withWrongProperty(index: Int): ViessmannFeature =
        updateProperties { properties ->
            val name = propertyName(index)
            properties + (name to wrongProperty(properties.getValue(name)))
        }

    private fun wrongProperty(original: Property): Property =
        if (original.value is BooleanValue || original.value is NullableBooleanValue) {
            Property.of("wrong")
        } else {
            Property.of(false)
        }

    private fun ViessmannFeature.withWrongParameter(
        commandIndex: Int,
        parameterIndex: Int,
    ): ViessmannFeature =
        updateCommand(commandIndex) { command ->
            val name = "a$parameterIndex"
            command.copy(
                params = EfficientStringKeyMap(command.params + (name to wrongParameter(command.params.getValue(name)))),
            )
        }

    private fun wrongParameter(original: Parameter): Parameter =
        if (original.constraints is BooleanConstraints) {
            parameter(2, original.required)
        } else {
            parameter(0, original.required)
        }

    private fun ViessmannFeature.withEveryRuleInvalid(size: StableSize): ViessmannFeature {
        val invalidProperties = properties.mapValues { (_, property) -> wrongProperty(property) }
        val invalidCommands =
            commands.mapValues { (name, command) ->
                val index = name.removePrefix("c").toInt()
                val invalidParams =
                    if (commandArity(size, index) == 0) {
                        mapOf("extra" to parameter(0, true))
                    } else {
                        command.params.mapValues { (_, parameter) -> wrongParameter(parameter) }
                    }
                command.copy(params = EfficientStringKeyMap(invalidParams))
            }
        return copy(
            properties = EfficientStringKeyMap(invalidProperties),
            commands = EfficientStringKeyMap(invalidCommands),
        )
    }

    private fun ViessmannFeature.updateCommand(
        index: Int,
        transform: (Command) -> Command,
    ): ViessmannFeature =
        updateCommands { commands ->
            val name = commandName(index)
            commands + (name to transform(commands.getValue(name)))
        }

    private fun ViessmannFeature.updateProperties(transform: (Map<String, Property>) -> Map<String, Property>): ViessmannFeature =
        copy(properties = EfficientStringKeyMap(transform(properties)))

    private fun ViessmannFeature.updateCommands(transform: (Map<String, Command>) -> Map<String, Command>): ViessmannFeature =
        copy(commands = EfficientStringKeyMap(transform(commands)))

    private fun propertyName(index: Int): String = "p${index.toString().padStart(2, '0')}"

    private fun commandName(index: Int): String = "c${index.toString().padStart(2, '0')}"
}
