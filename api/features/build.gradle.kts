plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.api.feature.apiFeatureCommon)
        api(projects.api.apiErrors)
        api(libs.kotlinx.serialization.json)
    }
}
