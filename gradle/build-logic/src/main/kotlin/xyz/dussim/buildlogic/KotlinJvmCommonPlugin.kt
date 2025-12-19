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

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-opt-in=kotlin.time.ExperimentalTime",
                        "-Xjdk-release=21",
                        "-Xcontext-parameters",
                        "-Xcontext-sensitive-resolution",
                        "-Xannotation-target-all",
                        "-Xreturn-value-checker=check",
                        "-Xexplicit-backing-fields"
                    )
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }

            val testImplementation = configurations.named("testImplementation")

            dependencies {
                testImplementation(libs.kotlin.test.common)
                testImplementation(libs.kotlin.test.annotations.common)
                testImplementation(libs.kotest.framework.engine)
                testImplementation(libs.kotest.assertions.core)
                testImplementation(libs.kotest.runner.junit5)
            }

            configureCommon()
        }
    }
}
