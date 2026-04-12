plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.api.feature.common)
        api(projects.api.errors)
        api(libs.kotlinx.serialization.json)
    }
}
