package xyz.dussim.buildlogic

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
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

fun Project.configureGroupAndVersion() {
    group = "xyz.dussim"
    version = "0.0.3"
}

fun Project.configurePublishing() {
    pluginManager.apply("maven-publish")

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "reposiliteRepositorySnapshots"
                url = uri("https://maven.dussim.xyz/snapshots")

                credentials {
                    username = providers.gradleProperty("repoUsername").get()
                    password = providers.gradleProperty("repoPassword").get()
                }
            }
        }

        publications.withType<MavenPublication>().configureEach {
            pom {
                name.set(project.name)
                description.set(project.description ?: project.name)
                url.set("https://github.com/Dussim/viessmann-api")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("dussim")
                        name.set("Artur Tuzim")
                    }
                }
            }
        }
    }
}

fun Project.configureCommon() {
    configureKotlinter()
    configureTestTasks()
    configureGroupAndVersion()
}
