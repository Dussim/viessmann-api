package xyz.dussim.buildlogic

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jmailen.gradle.kotlinter.KotlinterExtension

class KotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("org.jetbrains.dokka")
                apply("org.jmailen.kotlinter")
                apply("io.gitlab.arturbosch.detekt")
                apply("io.kotest")
            }

            extensions.configure<KotlinMultiplatformExtension> {
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
                        jvmTarget.set(JvmTarget.JVM_21)
                    }
                }
                js {
                    nodejs()
                    browser()
                    useEsModules()
                    generateTypeScriptDefinitions()
                }

                sourceSets.getByName("commonTest") {
                    dependencies {
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }
            }

            extensions.configure<KotlinterExtension> {
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

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                source.setFrom("src")
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "21"
            }

            group = "xyz.dussim"
            version = "0.0.1"
        }
    }
}
