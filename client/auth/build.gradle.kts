plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.client.clientCore)
        api(projects.api.apiAuth)
        api(projects.api.apiErrors)
        api(libs.kotlinx.serialization.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
