plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.client.clientCore)
        implementation(projects.api.apiUsers)
        implementation(projects.api.apiErrors)
        implementation(libs.kotlinx.serialization.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
