import io.morethan.jmhreport.gradle.task.JmhReportTask
import me.champeau.jmh.JMHTask
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.jvm.common)
    alias(libs.plugins.ksp)
    id("me.champeau.jmh") version "0.7.3"
    id("xyz.dussim.jmhreport") version "0.10.2"
}

dependencies {
    implementation(projects.api.apiDto)
    implementation(projects.api.feature.apiFeatureAnnotations)
    implementation(projects.api.feature.apiFeatureCommon)
    implementation(projects.api.feature.apiFeatureDefinitions)
    implementation(projects.api.feature.apiFeatureImplementations)
    ksp(projects.api.feature.apiFeatureProcessor)

    testImplementation(libs.kotest.assertions.core)
}

val stableJvmArgs =
    listOf(
        "-Xms2g",
        "-Xmx2g",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseCompactObjectHeaders",
        "-XX:+TrustFinalNonStaticFields",
        "--sun-misc-unsafe-memory-access=allow",
    )

ksp {
    arg("descriptorsChunkSize", "64")
    arg("formatGeneratedSources", "false")
    arg("validationRulesChunkSize", "64")
}

jmh {
    jmhVersion.set("1.37")
    // JVM arguments
    jvmArgs.addAll(stableJvmArgs)

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
    includes.addAll("xyz.dussim.feature.benchmark.featurevalidation.average.MassiveValidationBenchmark")

    // exclude map benchmark as I already established performance
//    excludes.addAll(".*MapBenchmark.*")
}

val stableEnvironmentFile = layout.buildDirectory.file("reports/jmh/environment.json")
val stableManifestFile = layout.projectDirectory.file("src/main/resources/stable-validation/v1/fixture-manifest.json")

val stableValidationEnvironment =
    tasks.register<JavaExec>("stableValidationEnvironment") {
        group = "benchmark"
        description = "Writes the stable benchmark environment fingerprint."
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("xyz.dussim.feature.benchmark.stable.StableEnvironmentFingerprint")
        args(stableManifestFile.asFile.absolutePath, stableEnvironmentFile.get().asFile.absolutePath)
        inputs.file(stableManifestFile)
        outputs.file(stableEnvironmentFile)
    }

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}

fun JMHTask.configureStableValidationJmh(include: String) {
    group = "benchmark"
    dependsOn(tasks.test, stableValidationEnvironment, tasks.named("jmhJar"))

    jmhClasspath.from(configurations.named("jmh"))
    testRuntimeClasspath.from(configurations.named("jmhRuntimeClasspath"))
    jarArchive = tasks.named<Jar>("jmhJar").flatMap { it.archiveFile }

    jmhVersion = "1.37"
    includeTests = false
    includes = listOf(include)
    excludes = emptyList()
    jvmArgs = stableJvmArgs
    profilers = listOf("gc")
    resultFormat = "json"
    resultExtension = "json"
    failOnError = true
    verbosity = "NORMAL"
    environment = emptyMap()
}

val stableValidationJmh =
    tasks.register<JMHTask>("stableValidationJmh") {
        description = "Runs all versioned stable validation acceptance benchmark suites."
        configureStableValidationJmh(
            include = "xyz\\.dussim\\.feature\\.benchmark\\.stable\\.(small|medium|large)\\..*Benchmark",
        )
        val timestamp =
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
        resultsFile = layout.buildDirectory.file("reports/validation/all/$timestamp/results.json")
    }

val stableSmallValidationJmh =
    tasks.register<JMHTask>("stableSmallValidationJmh") {
        description = "Runs the small stable validation acceptance benchmark suite."
        configureStableValidationJmh(
            include = "xyz\\.dussim\\.feature\\.benchmark\\.stable\\.small\\..*Benchmark",
        )
        val timestamp =
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
        resultsFile = layout.buildDirectory.file("reports/validation/small/$timestamp/results.json")
    }

val stableMediumValidationJmh =
    tasks.register<JMHTask>("stableMediumValidationJmh") {
        description = "Runs the medium stable validation acceptance benchmark suite."
        configureStableValidationJmh(
            include = "xyz\\.dussim\\.feature\\.benchmark\\.stable\\.medium\\..*Benchmark",
        )
        val timestamp =
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
        resultsFile = layout.buildDirectory.file("reports/validation/medium/$timestamp/results.json")
    }

val stableLargeValidationJmh =
    tasks.register<JMHTask>("stableLargeValidationJmh") {
        description = "Runs the large stable validation acceptance benchmark suite."
        configureStableValidationJmh(
            include = "xyz\\.dussim\\.feature\\.benchmark\\.stable\\.large\\..*Benchmark",
        )
        val timestamp =
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now())
        resultsFile = layout.buildDirectory.file("reports/validation/large/$timestamp/results.json")
    }

val stableValidationJmhReport =
    tasks.register<JmhReportTask>("stableValidationJmhReport") {
        description = "Creates an HTML report for all stable validation benchmark suites."
        jmhResultPath = stableValidationJmh.flatMap { it.resultsFile }
        jmhReportOutput = layout.dir(stableValidationJmh.flatMap { it.resultsFile }.map { it.asFile.parentFile })
    }

val stableSmallValidationJmhReport =
    tasks.register<JmhReportTask>("stableSmallValidationJmhReport") {
        description = "Creates an HTML report for the small stable validation benchmark suite."
        jmhResultPath = stableSmallValidationJmh.flatMap { it.resultsFile }
        jmhReportOutput = layout.dir(stableSmallValidationJmh.flatMap { it.resultsFile }.map { it.asFile.parentFile })
    }

val stableMediumValidationJmhReport =
    tasks.register<JmhReportTask>("stableMediumValidationJmhReport") {
        description = "Creates an HTML report for the medium stable validation benchmark suite."
        jmhResultPath = stableMediumValidationJmh.flatMap { it.resultsFile }
        jmhReportOutput = layout.dir(stableMediumValidationJmh.flatMap { it.resultsFile }.map { it.asFile.parentFile })
    }

val stableLargeValidationJmhReport =
    tasks.register<JmhReportTask>("stableLargeValidationJmhReport") {
        description = "Creates an HTML report for the large stable validation benchmark suite."
        jmhResultPath = stableLargeValidationJmh.flatMap { it.resultsFile }
        jmhReportOutput = layout.dir(stableLargeValidationJmh.flatMap { it.resultsFile }.map { it.asFile.parentFile })
    }

stableValidationJmh {
    finalizedBy(stableValidationJmhReport)
}

stableSmallValidationJmh {
    finalizedBy(stableSmallValidationJmhReport)
}

stableMediumValidationJmh {
    finalizedBy(stableMediumValidationJmhReport)
}

stableLargeValidationJmh {
    finalizedBy(stableLargeValidationJmhReport)
}

group = "xyz.dussim.feature.benchmark"
