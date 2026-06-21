package xyz.dussim.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatformLibrary() {
    val catalog = libs

    extensions.configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.addAll(
                buildList {
                    add("-opt-in=kotlin.time.ExperimentalTime")
                    add("-Xcontext-sensitive-resolution")
                    add("-Xexpect-actual-classes")
                    add("-Xreturn-value-checker=check")
                },
            )
        }

        withSourcesJar()

        jvm {
            compilerOptions {
                freeCompilerArgs.add("-Xjdk-release=25")
                jvmTarget.set(JvmTarget.JVM_25)
            }
        }

        js {
            nodejs()
            browser()
            useEsModules()
            generateTypeScriptDefinitions()
        }

        val commonMain = sourceSets.getByName("commonMain")
        val webMainSourceSet =
            sourceSets.maybeCreate("webMain").apply {
                dependsOn(commonMain)
            }
        sourceSets.getByName("jsMain").dependsOn(webMainSourceSet)

        sourceSets.getByName("commonTest") {
            dependencies {
                implementation(catalog.kotlin.test.common)
                implementation(catalog.kotlin.test.annotations.common)
                implementation(catalog.kotest.framework.engine)
                implementation(catalog.kotest.assertions.core)
            }
        }

        sourceSets.getByName("jvmTest") {
            dependencies {
                implementation(catalog.kotest.runner.junit5)
            }
        }
    }
}
