import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
    sourceSets.commonMain {
        dependencies {
            api(projects.api.feature.apiFeatureAnnotations)
            api(projects.api.feature.apiFeatureDefinitions)
            api(libs.kotlinx.serialization.json)
            api(projects.api.feature.apiFeatureCommon)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(projects.api.feature.apiFeatureImplementations)
        }
    }
}
