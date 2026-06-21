package xyz.dussim.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.multiplatform)
                apply(libs.plugins.kotlin.serialization)
            }
            configureCommonPlugins()
            configurePublishing()

            configureKotlinMultiplatformLibrary()

            configureCommon()
        }
    }
}
