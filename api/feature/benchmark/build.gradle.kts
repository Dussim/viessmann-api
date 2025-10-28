import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.io.kotest)
    alias(libs.plugins.com.google.devtools.ksp)
    id("me.champeau.jmh") version "0.7.3"
}
dependencies {
    implementation(projects.api.feature.annotations)

    implementation(projects.api.feature.common)

    ksp(projects.api.feature.processor)

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(libs.io.kotest.kotest.framework.engine)
    testImplementation(libs.io.kotest.kotest.assertions.core)
    testImplementation(libs.io.kotest.kotest.runner.junit5)
}

kotlin.compilerOptions {
    freeCompilerArgs.addAll(
        "-opt-in=kotlin.time.ExperimentalTime",
        "-Xjdk-release=21",
        "-Xcontext-parameters",
        "-Xcontext-sensitive-resolution",
        "-Xannotation-target-all",
    )
    jvmTarget = JvmTarget.JVM_21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

jmh {
    jmhVersion.set("1.37")
    // JVM arguments
    jvmArgs.addAll("-Xms2g", "-Xmx2g", "-XX:+UseCompactObjectHeaders", "-XX:+UnlockExperimentalVMOptions", "-XX:+TrustFinalNonStaticFields")

    // GC profiling
    profilers.addAll("gc")

    // Result file format: json, csv, text
    resultFormat.set("json")

    // Output file for results
    resultsFile.set(layout.buildDirectory.file("reports/jmh/results.json"))
//    humanOutputFile.set(layout.buildDirectory.file("reports/jmh/human.txt"))

    // Fail build on error
    failOnError.set(true)

    // Verbose output
    verbosity.set("NORMAL")

    // exclude map benchmark as I already established performance
//    excludes.addAll(".*MapBenchmark.*")
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

detekt {
    buildUponDefaultConfig = true
    source.setFrom("src")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
}

group = "xyz.dussim.feature.benchmark"
version = "0.0.1"
