package xyz.dussim.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jmailen.gradle.kotlinter.KotlinterExtension

fun Project.configureCommonPlugins() {
    with(pluginManager) {
        apply(libs.plugins.dokka)
        apply(libs.plugins.kotlinter)
        apply(libs.plugins.detekt)
        apply(libs.plugins.ksp)
        apply(libs.plugins.kotest)
    }
}

fun Project.configureKotlinter() {
    extensions.configure<KotlinterExtension> {
        ktlintVersion = "1.8.0"
    }
}

fun Project.configureTestTasks() {
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
}

fun Project.configureDetekt() {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        source.setFrom("src")
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
    }
}

fun Project.configureGroupAndVersion() {
    group = "xyz.dussim"
    version = "0.0.1"
}

fun Project.configureCommon() {
    configureKotlinter()
    configureTestTasks()
    configureDetekt()
    configureGroupAndVersion()
}
