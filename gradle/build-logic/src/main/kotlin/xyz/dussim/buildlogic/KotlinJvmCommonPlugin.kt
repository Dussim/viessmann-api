package xyz.dussim.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinJvmCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.jvm)
            }
            configureCommonPlugins()
            configurePublishing()

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-Xjdk-release=25",
                        "-Xcontext-sensitive-resolution",
                        "-Xreturn-value-checker=check",
                    )
                    jvmTarget.set(JvmTarget.JVM_25)
                }
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_25
                targetCompatibility = JavaVersion.VERSION_25
            }

            val testImplementation = configurations.named("testImplementation")

            dependencies {
                testImplementation(libs.kotest.assertions.core)
                testImplementation(libs.testBalloon.framework.core)
                testImplementation(libs.testBalloon.integration.kotest.assertions)
            }

            configureCommon()
        }
    }
}
