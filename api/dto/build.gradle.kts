import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.io.kotest)
    alias(libs.plugins.com.google.devtools.ksp)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
            "-Xcontext-parameters",
            "-Xcontext-sensitive-resolution",
            "-Xannotation-target-all",
        )
    }

    withSourcesJar()
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjdk-release=21")
            jvmTarget = JvmTarget.JVM_21
        }
    }
    js {
        nodejs()
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
    }

    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            implementation(projects.api.feature.annotations)

            api(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)

            api(projects.api.feature.common)
        }
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation(libs.io.kotest.kotest.framework.engine)
        implementation(libs.io.kotest.kotest.assertions.core)
    }

    sourceSets.jvmTest.dependencies {
        implementation(libs.io.kotest.kotest.runner.junit5)
    }
}

detekt {
    buildUponDefaultConfig = true
    source.setFrom("src")
}

kotlinter {
    ktlintVersion = "1.8.0"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(FAILED, PASSED)
        exceptionFormat = FULL
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<FormatTask>().configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    source = source.minus(fileTree("build/generated/ksp")).asFileTree
}

group = "xyz.dussim"
version = "0.0.1"

// -------------------- Anonymization Support Tasks --------------------
@DisableCachingByDefault(because = "Verifies or mutates JSON files in place; not suitable for output caching")
abstract class AnonymizeJsonTask
    @Inject
    constructor(
        objects: ObjectFactory,
        providers: ProviderFactory,
        layout: ProjectLayout,
    ) : DefaultTask() {
        // Inputs with conventions defined inside the task
        @get:Input
        val domain: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.domain").orElse("example.com"))

        @get:Input
        val installationIdNumber: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.installationId").orElse("10000"))

        @get:Input
        val gatewayId: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.gatewayId").orElse("0000000000000000"))

        @get:Input
        val gatewaySerial: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.gatewaySerial").orElse("0000000000000000"))

        @get:Input
        val deviceIdNumeric: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.deviceIdNumeric").orElse("0"))

        @get:Input
        val deviceIdZigbee: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.deviceIdZigbee").orElse("zigbee-0000000000000000"))

        @get:Input
        val deviceIdGeneric: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.deviceIdGeneric").orElse("device-0000000000000000"))

        @get:Input
        val timestampIso: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.timestamp").orElse("2020-01-01T00:00:01Z"))

        // New inputs for extended anonymization
        @get:Input
        val latitude: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.latitude").orElse("0.0"))

        @get:Input
        val longitude: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.longitude").orElse("0.0"))

        @get:Input
        val addressLine1: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.addressLine1").orElse("Street"))

        @get:Input
        val addressLine2: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.addressLine2").orElse("1"))

        @get:Input
        val city: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.city").orElse("City"))

        @get:Input
        val postalCode: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.postalCode").orElse("00000"))

        @get:Input
        val country: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.country").orElse("XX"))

        @get:Input
        val phone: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.phone").orElse("+000000000"))

        @get:Input
        val timeZone: Property<String> =
            objects
                .property(String::class.java)
                .convention(providers.gradleProperty("anonymize.timeZone").orElse("Etc/UTC"))

        @get:Input
        val applyChanges: Property<Boolean> =
            objects
                .property(Boolean::class.java)
                .convention(false)

        @get:InputFiles
        @get:SkipWhenEmpty
        @get:PathSensitive(PathSensitivity.RELATIVE)
        val jsonFiles: ConfigurableFileCollection =
            objects.fileCollection().from(
                layout.projectDirectory.asFileTree.matching {
                    include("**/src/**/resources/**/*.json")
                    exclude("**/build/**")
                    exclude(".gradle/**")
                },
            )

        @TaskAction
        fun run() {
            val files = jsonFiles.files.sortedBy { it.absolutePath }
            var changedCount = 0
            val modified = mutableListOf<File>()
            val doApply = applyChanges.get()

            files.forEach { f ->
                val original = f.readText()
                val transformed = anonymizeJsonContent(original)
                if (original != transformed) {
                    if (doApply) {
                        f.writeText(transformed)
                    }
                    changedCount++
                    modified += f
                }
            }

            if (doApply) {
                logger.lifecycle("[anonymize] Applied to $changedCount file(s).")
                if (changedCount > 0) {
                    modified.forEach { logger.lifecycle(" - ${this.project.rootDir.toPath().relativize(it.toPath())}") }
                }
            } else {
                if (changedCount > 0) {
                    logger.lifecycle("[anonymize] Verification would change $changedCount file(s):")
                    modified.forEach { logger.lifecycle(" - ${this.project.rootDir.toPath().relativize(it.toPath())}") }
                    throw GradleException("Some JSON files contain non-anonymized data. Run anonymizeJsonApply to fix.")
                } else {
                    logger.lifecycle("[anonymize] Verification passed. No changes needed.")
                }
            }
        }

        private fun anonymizeJsonContent(input: String): String {
            var s = input

            // Snapshot task inputs once for this run
            val domainVal = domain.get()
            val installationIdVal = installationIdNumber.get()
            val gatewayIdVal = gatewayId.get()
            val gatewaySerialVal = gatewaySerial.get()
            val deviceIdNumericVal = deviceIdNumeric.get()
            val deviceIdZigbeeVal = deviceIdZigbee.get()
            val deviceIdGenericVal = deviceIdGeneric.get()
            val timestampIsoVal = timestampIso.get()
            val latitudeVal = latitude.get()
            val longitudeVal = longitude.get()
            val addressLine1Val = addressLine1.get()
            val addressLine2Val = addressLine2.get()
            val cityVal = city.get()
            val postalCodeVal = postalCode.get()
            val countryVal = country.get()
            val phoneVal = phone.get()
            val timeZoneVal = timeZone.get()

            // 1) Normalize domains in all URIs to https://example.com
            s = s.replace(AnonymizeRegex.domainHost) { "https://$domainVal" }

            // 2) Replace installationId occurrences (numeric and string)
            s =
                s.replace(AnonymizeRegex.installationIdNumber) {
                    "${it.groupValues[1]}$installationIdVal"
                }
            s =
                s.replace(AnonymizeRegex.installationIdString) {
                    "${it.groupValues[1]}${installationIdVal}${it.groupValues[3]}"
                }

            // 3) Replace gatewayId and gatewaySerial values (string and numeric)
            s =
                s.replace(AnonymizeRegex.gatewayIdString) {
                    "${it.groupValues[1]}${gatewayIdVal}${it.groupValues[3]}"
                }
            s =
                s.replace(AnonymizeRegex.gatewayIdNumber) {
                    "${it.groupValues[1]}$gatewayIdVal"
                }
            s =
                s.replace(AnonymizeRegex.gatewaySerialString) {
                    "${it.groupValues[1]}${gatewaySerialVal}${it.groupValues[3]}"
                }
            s =
                s.replace(AnonymizeRegex.gatewaySerialNumber) {
                    "${it.groupValues[1]}$gatewaySerialVal"
                }

            // 4) Replace deviceId cases (zigbee-*, numeric strings/unquoted, and generic)
            s =
                s.replace(AnonymizeRegex.deviceIdZigbeeString) {
                    "${it.groupValues[1]}${deviceIdZigbeeVal}${it.groupValues[2]}"
                }
            // also handle objects with generic `id` that carry zigbee device ids
            s =
                s.replace(AnonymizeRegex.idZigbeeString) {
                    "${it.groupValues[1]}${deviceIdZigbeeVal}${it.groupValues[2]}"
                }
            // unquoted numeric
            s =
                s.replace(AnonymizeRegex.deviceIdUnquotedNumber) {
                    "${it.groupValues[1]}$deviceIdNumericVal"
                }
            // quoted numeric
            s =
                s.replace(AnonymizeRegex.deviceIdQuotedNumber) {
                    "${it.groupValues[1]}${deviceIdNumericVal}${it.groupValues[2]}"
                }
            // fallback for any other deviceId strings
            s =
                s.replace(AnonymizeRegex.deviceIdStringFallback) {
                    "${it.groupValues[1]}${deviceIdGenericVal}${it.groupValues[3]}"
                }
            // final safety net: any JSON string value that is a zigbee-... token
            s =
                s.replace(AnonymizeRegex.anyZigbeeValue) {
                    "${it.groupValues[1]}${deviceIdZigbeeVal}${it.groupValues[2]}"
                }

            // 5) Normalize time-like fields to a fixed ISO instant
            s = s.replace(AnonymizeRegex.timestampString) { "${it.groupValues[1]}${timestampIsoVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.createdAtString) { "${it.groupValues[1]}${timestampIsoVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.editedAtString) { "${it.groupValues[1]}${timestampIsoVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.lastStatusChangedAtString) { "${it.groupValues[1]}${timestampIsoVal}${it.groupValues[3]}" }

            // 6) Geolocation (numeric)
            s = s.replace(AnonymizeRegex.latitudeSimple) { it.groupValues[1] + latitudeVal }
            s = s.replace(AnonymizeRegex.longitudeSimple) { it.groupValues[1] + longitudeVal }
            s = s.replace(AnonymizeRegex.latitudeValueInObject) { it.groupValues[1] + latitudeVal }
            s = s.replace(AnonymizeRegex.longitudeValueInObject) { it.groupValues[1] + longitudeVal }

            // 7) Address and contact
            s = s.replace(AnonymizeRegex.addressLine1) { "${it.groupValues[1]}${addressLine1Val}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.addressLine2) { "${it.groupValues[1]}${addressLine2Val}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.postalCode) { "${it.groupValues[1]}${postalCodeVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.city) { "${it.groupValues[1]}${cityVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.country) { "${it.groupValues[1]}${countryVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.countryCode) { "${it.groupValues[1]}${countryVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.phoneNumber) { "${it.groupValues[1]}${phoneVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.buildingPhone) { "${it.groupValues[1]}${phoneVal}${it.groupValues[3]}" }

            // 8) Time zone
            s = s.replace(AnonymizeRegex.timeZone) { "${it.groupValues[1]}${timeZoneVal}${it.groupValues[3]}" }

            // 9) Serials (string and numeric)
            s = s.replace(AnonymizeRegex.genericSerialString) { "${it.groupValues[1]}${gatewaySerialVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.genericSerialNumber) { it.groupValues[1] + gatewaySerialVal }
            s = s.replace(AnonymizeRegex.anySerialKeyString) { "${it.groupValues[1]}${gatewaySerialVal}${it.groupValues[3]}" }
            s = s.replace(AnonymizeRegex.anySerialKeyNumber) { it.groupValues[1] + gatewaySerialVal }

            // 10) Replace IDs inside any API path segments
            s = s.replace(AnonymizeRegex.pathInstallations) { it.groupValues[1] + installationIdVal }
            s = s.replace(AnonymizeRegex.pathGateways) { it.groupValues[1] + gatewaySerialVal }
            s = s.replace(AnonymizeRegex.pathDevicesZigbee) { it.groupValues[1] + deviceIdZigbeeVal }
            s = s.replace(AnonymizeRegex.pathDevicesNumber) { it.groupValues[1] + deviceIdNumericVal }
            // any other /devices/<string>
            s = s.replace(AnonymizeRegex.pathDevicesGeneric) { it.groupValues[1] + deviceIdGenericVal }

            return s
        }

        companion object AnonymizeRegex {
            // Domain host in any URI
            val domainHost = Regex("""https?://[^/]+""")

            // installationId values (numeric or string)
            val installationIdNumber = Regex("""("installationId"\s*:\s*)(\d+)""")
            val installationIdString = Regex("""("installationId"\s*:\s*")([^"]+)(")""")

            // gatewayId values
            val gatewayIdString = Regex("""("gatewayId"\s*:\s*")([^"]+)(")""")
            val gatewayIdNumber = Regex("""("gatewayId"\s*:\s*)(\d+)""")

            // gatewaySerial values
            val gatewaySerialString = Regex("""("gatewaySerial"\s*:\s*")([^"]+)(")""")
            val gatewaySerialNumber = Regex("""("gatewaySerial"\s*:\s*)(\d+)""")

            // deviceId variants
            val deviceIdZigbeeString = Regex("""("deviceId"\s*:\s*")zigbee-[0-9a-fA-F]+(")""")
            val deviceIdUnquotedNumber = Regex("""("deviceId"\s*:\s*)(\d+)""")
            val deviceIdQuotedNumber = Regex("""("deviceId"\s*:\s*")\d+(")""")
            val deviceIdStringFallback = Regex("""("deviceId"\s*:\s*")([^"]+)(")""")

            // Additional zigbee ID patterns
            val idZigbeeString = Regex("""("id"\s*:\s*")zigbee-[0-9a-fA-F]+(")""")

            // Any JSON string value that is a zigbee-... token (safe: requires hyphen, so it won't match plain "zigbee")
            val anyZigbeeValue = Regex("""(:\s*")zigbee-[0-9a-fA-F]+(")""")

            // Timestamps (ISO-8601 strings)
            val timestampString = Regex("""("timestamp"\s*:\s*")([^"]+)(")""")
            val createdAtString = Regex("""("createdAt"\s*:\s*")([^"]+)(")""")
            val editedAtString = Regex("""("editedAt"\s*:\s*")([^"]+)(")""")
            val lastStatusChangedAtString = Regex("""("lastStatusChangedAt"\s*:\s*")([^"]+)(")""")

            // Geolocation
            val latitudeSimple = Regex("""("latitude"\s*:\s*)(-?\d+(?:\.\d+)?)""")
            val longitudeSimple = Regex("""("longitude"\s*:\s*)(-?\d+(?:\.\d+)?)""")
            val latitudeValueInObject = Regex("""(?s)("latitude"[^\{\[]*\{[^}]*"value"\s*:\s*)(-?\d+(?:\.\d+)?)""")
            val longitudeValueInObject = Regex("""(?s)("longitude"[^\{\[]*\{[^}]*"value"\s*:\s*)(-?\d+(?:\.\d+)?)""")

            // Address and contact
            val addressLine1 = Regex("""("addressline1"\s*:\s*")([^"]+)(")""")
            val addressLine2 = Regex("""("addressline2"\s*:\s*")([^"]+)(")""")
            val postalCode = Regex("""("postalCode"\s*:\s*")([^"]+)(")""")
            val city = Regex("""("city"\s*:\s*")([^"]+)(")""")
            val country = Regex("""("country"\s*:\s*")([^"]+)(")""")
            val countryCode = Regex("""("countryCode"\s*:\s*")([^"]+)(")""")
            val phoneNumber = Regex("""("phoneNumber"\s*:\s*")([^"]+)(")""")
            val buildingPhone = Regex("""("buildingPhone"\s*:\s*")([^"]+)(")""")

            // Time zone
            val timeZone = Regex("""("timeZone"\s*:\s*")([^"]+)(")""")

            // Serials: generic "serial" and any *Serial key (string or numeric)
            val genericSerialString = Regex("""("serial"\s*:\s*")([^"]+)(")""")
            val genericSerialNumber = Regex("""("serial"\s*:\s*)(\d+)""")
            val anySerialKeyString = Regex("""("[A-Za-z]+Serial"\s*:\s*")([^"]+)(")""")
            val anySerialKeyNumber = Regex("""("[A-Za-z]+Serial"\s*:\s*)(\d+)""")

            // IDs inside API path segments
            val pathInstallations = Regex("""(/installations/)(\d+)""")
            val pathGateways = Regex("""(/gateways/)([^/"}]+)""")
            val pathDevicesZigbee = Regex("""(/devices/)(zigbee-[^/"}]+)""")
            val pathDevicesNumber = Regex("""(/devices/)(\d+)""")
            val pathDevicesGeneric = Regex("""(/devices/)([^/"}]+)""")
        }
    }

val anonymizeJsonVerify by tasks.registering(AnonymizeJsonTask::class) {
    group = "verification"
    description = "Verifies that all JSON files are anonymized; fails if any changes would be made."
}

val anonymizeJsonApply by tasks.registering(AnonymizeJsonTask::class) {
    group = "maintenance"
    description = "Applies anonymization to JSON files in-place."
    // Only override the behavior flag; all other conventions are internal to the task
    applyChanges.convention(true)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    println(name)
    dependsOn(anonymizeJsonVerify)
}
