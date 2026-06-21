package xyz.dussim.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinClientPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.multiplatform)
                apply(libs.plugins.kotlin.serialization)
                apply(libs.plugins.kotlinter)
                apply(libs.plugins.dokka)
            }

            configurePublishing()

            configureKotlinMultiplatformLibrary()
            configureClientTestSupportDependency()

            configureCommon()
        }
    }

    private fun Project.configureClientTestSupportDependency() {
        if (name == "client-core" || name == "client-test-support") return

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonTest") {
                dependencies {
                    implementation(project(":client:client-test-support"))
                }
            }
        }
    }
}
