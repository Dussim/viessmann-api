package xyz.dussim.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.multiplatform)
                apply(libs.plugins.kotlin.serialization)
            }
            configureCommonPlugins()
            configurePublishing()

            extensions.configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-opt-in=kotlin.time.ExperimentalTime",
                        "-Xcontext-sensitive-resolution",
                        "-Xannotation-target-all",
                        "-Xreturn-value-checker=check",
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

                val commonMain = sourceSets.getByName("commonMain")
                val webMain =
                    sourceSets.maybeCreate("webMain").apply {
                        dependsOn(commonMain)
                    }
                sourceSets.getByName("jsMain").dependsOn(webMain)

                sourceSets.getByName("commonTest") {
                    dependencies {
                        implementation(libs.kotlin.test.common)
                        implementation(libs.kotlin.test.annotations.common)
                        implementation(libs.kotest.framework.engine)
                        implementation(libs.kotest.assertions.core)
                    }
                }

                sourceSets.getByName("jvmTest") {
                    dependencies {
                        implementation(libs.kotest.runner.junit5)
                    }
                }
            }

            configureCommon()
        }
    }
}
