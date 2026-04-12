plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.api.dto)
        api(projects.api.errors)
        api(libs.kotlinx.serialization.json)
    }
}
