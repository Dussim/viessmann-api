import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.io.kotest)
    alias(libs.plugins.com.google.devtools.ksp)
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
            freeCompilerArgs.addAll(
                "-Xjdk-release=21",
            )
            jvmTarget = JvmTarget.JVM_21
        }
    }
    js {
        nodejs()
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
    }

    sourceSets.commonMain.dependencies {
        api(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
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

detekt {
    buildUponDefaultConfig = true
    source.setFrom("src")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
}

group = "xyz.dussim"
version = "0.0.1"
