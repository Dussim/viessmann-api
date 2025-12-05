package xyz.dussim.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

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
            // Snapshot task inputs once for this run
            val config =
                AnonymizationConfig(
                    domain = domain.get(),
                    installationId = installationIdNumber.get(),
                    gatewayId = gatewayId.get(),
                    gatewaySerial = gatewaySerial.get(),
                    deviceIdNumeric = deviceIdNumeric.get(),
                    deviceIdZigbee = deviceIdZigbee.get(),
                    deviceIdGeneric = deviceIdGeneric.get(),
                    timestamp = timestampIso.get(),
                    latitude = latitude.get(),
                    longitude = longitude.get(),
                    addressLine1 = addressLine1.get(),
                    addressLine2 = addressLine2.get(),
                    city = city.get(),
                    postalCode = postalCode.get(),
                    country = country.get(),
                    phone = phone.get(),
                    timeZone = timeZone.get(),
                )

            return input
                .let { anonymizeDomains(it, config) }
                .let { anonymizeInstallationIds(it, config) }
                .let { anonymizeGatewayIds(it, config) }
                .let { anonymizeDeviceIds(it, config) }
                .let { anonymizeTimestamps(it, config) }
                .let { anonymizeGeolocation(it, config) }
                .let { anonymizeAddressAndContact(it, config) }
                .let { anonymizeTimeZone(it, config) }
                .let { anonymizeSerials(it, config) }
                .let { anonymizeApiPaths(it, config) }
        }

        private fun anonymizeDomains(
            s: String,
            config: AnonymizationConfig,
        ): String = s.replace(Patterns.Domain.host) { "https://${config.domain}" }

        private fun anonymizeInstallationIds(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Installation.idNumber) { "${it.groupValues[1]}${config.installationId}" }
                .replace(Patterns.Installation.idString) { "${it.groupValues[1]}${config.installationId}${it.groupValues[3]}" }

        private fun anonymizeGatewayIds(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Gateway.idString) { "${it.groupValues[1]}${config.gatewayId}${it.groupValues[3]}" }
                .replace(Patterns.Gateway.idNumber) { "${it.groupValues[1]}${config.gatewayId}" }
                .replace(Patterns.Gateway.serialString) { "${it.groupValues[1]}${config.gatewaySerial}${it.groupValues[3]}" }
                .replace(Patterns.Gateway.serialNumber) { "${it.groupValues[1]}${config.gatewaySerial}" }

        private fun anonymizeDeviceIds(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Device.idZigbeeString) { "${it.groupValues[1]}${config.deviceIdZigbee}${it.groupValues[2]}" }
                .replace(Patterns.Device.idZigbeeGeneric) { "${it.groupValues[1]}${config.deviceIdZigbee}${it.groupValues[2]}" }
                .replace(Patterns.Device.idUnquotedNumber) { "${it.groupValues[1]}${config.deviceIdNumeric}" }
                .replace(Patterns.Device.idQuotedNumber) { "${it.groupValues[1]}${config.deviceIdNumeric}${it.groupValues[2]}" }
                .replace(Patterns.Device.idStringFallback) { "${it.groupValues[1]}${config.deviceIdGeneric}${it.groupValues[3]}" }
                .replace(Patterns.Device.anyZigbeeValue) { "${it.groupValues[1]}${config.deviceIdZigbee}${it.groupValues[2]}" }

        private fun anonymizeTimestamps(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Timestamp.timestamp) { "${it.groupValues[1]}${config.timestamp}${it.groupValues[3]}" }
                .replace(Patterns.Timestamp.createdAt) { "${it.groupValues[1]}${config.timestamp}${it.groupValues[3]}" }
                .replace(Patterns.Timestamp.editedAt) { "${it.groupValues[1]}${config.timestamp}${it.groupValues[3]}" }
                .replace(Patterns.Timestamp.lastStatusChangedAt) { "${it.groupValues[1]}${config.timestamp}${it.groupValues[3]}" }

        private fun anonymizeGeolocation(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Geolocation.latitudeSimple) { it.groupValues[1] + config.latitude }
                .replace(Patterns.Geolocation.longitudeSimple) { it.groupValues[1] + config.longitude }
                .replace(Patterns.Geolocation.latitudeInObject) { it.groupValues[1] + config.latitude }
                .replace(Patterns.Geolocation.longitudeInObject) { it.groupValues[1] + config.longitude }

        private fun anonymizeAddressAndContact(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Address.line1) { "${it.groupValues[1]}${config.addressLine1}${it.groupValues[3]}" }
                .replace(Patterns.Address.line2) { "${it.groupValues[1]}${config.addressLine2}${it.groupValues[3]}" }
                .replace(Patterns.Address.postalCode) { "${it.groupValues[1]}${config.postalCode}${it.groupValues[3]}" }
                .replace(Patterns.Address.city) { "${it.groupValues[1]}${config.city}${it.groupValues[3]}" }
                .replace(Patterns.Address.country) { "${it.groupValues[1]}${config.country}${it.groupValues[3]}" }
                .replace(Patterns.Address.countryCode) { "${it.groupValues[1]}${config.country}${it.groupValues[3]}" }
                .replace(Patterns.Address.phoneNumber) { "${it.groupValues[1]}${config.phone}${it.groupValues[3]}" }
                .replace(Patterns.Address.buildingPhone) { "${it.groupValues[1]}${config.phone}${it.groupValues[3]}" }

        private fun anonymizeTimeZone(
            s: String,
            config: AnonymizationConfig,
        ): String = s.replace(Patterns.TimeZone.timeZone) { "${it.groupValues[1]}${config.timeZone}${it.groupValues[3]}" }

        private fun anonymizeSerials(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.Serial.genericString) { "${it.groupValues[1]}${config.gatewaySerial}${it.groupValues[3]}" }
                .replace(Patterns.Serial.genericNumber) { it.groupValues[1] + config.gatewaySerial }
                .replace(Patterns.Serial.anyKeyString) { "${it.groupValues[1]}${config.gatewaySerial}${it.groupValues[3]}" }
                .replace(Patterns.Serial.anyKeyNumber) { it.groupValues[1] + config.gatewaySerial }

        private fun anonymizeApiPaths(
            s: String,
            config: AnonymizationConfig,
        ): String =
            s
                .replace(Patterns.ApiPath.installations) { it.groupValues[1] + config.installationId }
                .replace(Patterns.ApiPath.gateways) { it.groupValues[1] + config.gatewaySerial }
                .replace(Patterns.ApiPath.devicesZigbee) { it.groupValues[1] + config.deviceIdZigbee }
                .replace(Patterns.ApiPath.devicesNumber) { it.groupValues[1] + config.deviceIdNumeric }
                .replace(Patterns.ApiPath.devicesGeneric) { it.groupValues[1] + config.deviceIdGeneric }

        private data class AnonymizationConfig(
            val domain: String,
            val installationId: String,
            val gatewayId: String,
            val gatewaySerial: String,
            val deviceIdNumeric: String,
            val deviceIdZigbee: String,
            val deviceIdGeneric: String,
            val timestamp: String,
            val latitude: String,
            val longitude: String,
            val addressLine1: String,
            val addressLine2: String,
            val city: String,
            val postalCode: String,
            val country: String,
            val phone: String,
            val timeZone: String,
        )

        private object Patterns {
            object Domain {
                val host = Regex("""https?://[^/]+""")
            }

            object Installation {
                val idNumber = Regex("""("installationId"\s*:\s*)(\d+)""")
                val idString = Regex("""("installationId"\s*:\s*")([^"]+)(")""")
            }

            object Gateway {
                val idString = Regex("""("gatewayId"\s*:\s*")([^"]+)(")""")
                val idNumber = Regex("""("gatewayId"\s*:\s*)(\d+)""")
                val serialString = Regex("""("gatewaySerial"\s*:\s*")([^"]+)(")""")
                val serialNumber = Regex("""("gatewaySerial"\s*:\s*)(\d+)""")
            }

            object Device {
                val idZigbeeString = Regex("""("deviceId"\s*:\s*")zigbee-[0-9a-fA-F]+(")""")
                val idZigbeeGeneric = Regex("""("id"\s*:\s*")zigbee-[0-9a-fA-F]+(")""")
                val idUnquotedNumber = Regex("""("deviceId"\s*:\s*)(\d+)""")
                val idQuotedNumber = Regex("""("deviceId"\s*:\s*")\d+(")""")
                val idStringFallback = Regex("""("deviceId"\s*:\s*")([^"]+)(")""")
                val anyZigbeeValue = Regex("""(:\s*")zigbee-[0-9a-fA-F]+(")""")
            }

            object Timestamp {
                val timestamp = Regex("""("timestamp"\s*:\s*")([^"]+)(")""")
                val createdAt = Regex("""("createdAt"\s*:\s*")([^"]+)(")""")
                val editedAt = Regex("""("editedAt"\s*:\s*")([^"]+)(")""")
                val lastStatusChangedAt = Regex("""("lastStatusChangedAt"\s*:\s*")([^"]+)(")""")
            }

            object Geolocation {
                val latitudeSimple = Regex("""("latitude"\s*:\s*)(-?\d+(?:\.\d+)?)""")
                val longitudeSimple = Regex("""("longitude"\s*:\s*)(-?\d+(?:\.\d+)?)""")
                val latitudeInObject = Regex("""(?s)("latitude"[^\{\[]*\{[^}]*"value"\s*:\s*)(-?\d+(?:\.\d+)?)""")
                val longitudeInObject = Regex("""(?s)("longitude"[^\{\[]*\{[^}]*"value"\s*:\s*)(-?\d+(?:\.\d+)?)""")
            }

            object Address {
                val line1 = Regex("""("addressline1"\s*:\s*")([^"]+)(")""")
                val line2 = Regex("""("addressline2"\s*:\s*")([^"]+)(")""")
                val postalCode = Regex("""("postalCode"\s*:\s*")([^"]+)(")""")
                val city = Regex("""("city"\s*:\s*")([^"]+)(")""")
                val country = Regex("""("country"\s*:\s*")([^"]+)(")""")
                val countryCode = Regex("""("countryCode"\s*:\s*")([^"]+)(")""")
                val phoneNumber = Regex("""("phoneNumber"\s*:\s*")([^"]+)(")""")
                val buildingPhone = Regex("""("buildingPhone"\s*:\s*")([^"]+)(")""")
            }

            object TimeZone {
                val timeZone = Regex("""("timeZone"\s*:\s*")([^"]+)(")""")
            }

            object Serial {
                val genericString = Regex("""("serial"\s*:\s*")([^"]+)(")""")
                val genericNumber = Regex("""("serial"\s*:\s*)(\d+)""")
                val anyKeyString = Regex("""("[A-Za-z]+Serial"\s*:\s*")([^"]+)(")""")
                val anyKeyNumber = Regex("""("[A-Za-z]+Serial"\s*:\s*)(\d+)""")
            }

            object ApiPath {
                val installations = Regex("""(/installations/)(\d+)""")
                val gateways = Regex("""(/gateways/)([^/"}]+)""")
                val devicesZigbee = Regex("""(/devices/)(zigbee-[^/"}]+)""")
                val devicesNumber = Regex("""(/devices/)(\d+)""")
                val devicesGeneric = Regex("""(/devices/)([^/"}]+)""")
            }
        }
    }
