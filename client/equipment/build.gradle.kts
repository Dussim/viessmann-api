plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.client.clientCore)
        api(projects.api.apiDto)
        api(projects.api.apiEquipment)
        api(projects.api.apiErrors)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
