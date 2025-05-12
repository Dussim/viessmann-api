import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.io.kotest.multiplatform)
    alias(libs.plugins.com.google.devtools.ksp)
}

dependencies {
    api(projects.api.feature.annotations)

    implementation(libs.com.google.devtools.ksp.symbol.processing.api)
    implementation(libs.com.google.auto.service.auto.service.annotations)
    implementation(libs.com.squareup.kotlinpoet.ksp)

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(libs.io.kotest.kotest.framework.engine)
    testImplementation(libs.io.kotest.kotest.assertions.core)
    testImplementation(libs.io.kotest.kotest.runner.junit5)

    ksp(libs.dev.zacsweers.autoservice.auto.service.ksp)
}

kotlin.compilerOptions {
    freeCompilerArgs.add("-Xjdk-release=21")
    jvmTarget = JvmTarget.JVM_21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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

group = "xyz.dussim"
version = "0.0.1"
