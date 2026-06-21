plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.client.clientCore)
        api(ktorLibs.client.mock)
    }
}
