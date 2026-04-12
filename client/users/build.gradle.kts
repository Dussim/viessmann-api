plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":client:client-core"))
        implementation(project(":api:users"))
        implementation(project(":api:errors"))
        implementation(libs.kotlinx.serialization.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
