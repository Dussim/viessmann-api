plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":api:errors"))
        api(ktorLibs.client.contentNegotiation)
        api(ktorLibs.client.core)
        api(ktorLibs.serialization.kotlinx.json)
        api(libs.kotlinx.serialization.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
