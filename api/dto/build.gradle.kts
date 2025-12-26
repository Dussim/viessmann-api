import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.anonymize.json)
    alias(conventions.plugins.xyz.dussim.generate.features)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}

kotlin {
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            implementation(projects.api.feature.annotations)

            api(libs.kotlinx.serialization.json)

            api(projects.api.feature.common)
        }
    }
}

generateFeatureInterfaces {
    featuresJsons = layout.projectDirectory.dir("src/commonTest/resources/features/device")
    generatedSources = layout.buildDirectory.dir("generated/features")
    packageName.set("xyz.dussim.viessmann.api.features")

    ignoredFeatures.addAll(
        "device.etn",
        "device.messages.errors.raw",
        "device.serial",
        "device.timeseries.monitoringIonization",
        "device.zigbee.active",
        "heating.boiler.pumps.internal",
        "heating.boiler.pumps.internal.target",
        "heating.boiler.sensors.temperature.commonSupply",
        "heating.boiler.serial",
        "heating.boiler.temperature",
        "heating.bufferCylinder.sensors.temperature.main",
        "device.configuration",
        "device.zigbee.coordinator",
        "rooms",
        "rooms.{}",
        "rooms.others.{}",
        "device.timezone",
        "tcu.mode",
        "heating.circuits.{}.heating.schedule",
    )
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(tasks.anonymizeJsonVerify)
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<FormatTask>().configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    source = source.minus(fileTree("build/generated/ksp")).asFileTree
}

tasks.sourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.jvmSourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.jsSourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}
