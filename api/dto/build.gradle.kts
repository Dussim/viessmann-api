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
    alias(libs.plugins.io.kotest.multiplatform)
    alias(libs.plugins.com.google.devtools.ksp)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}

kotlin {
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

            api(libs.org.jetbrains.kotlinx.kotlinx.serialization.core)
            api(libs.org.jetbrains.kotlinx.kotlinx.datetime)
            api(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
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
    source = source.minus(fileTree("build/generated/ksp")).asFileTree
}

group = "xyz.dussim"
version = "0.0.1"
