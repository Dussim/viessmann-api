plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.client.clientCore)
        api(projects.client.clientAuth)
        api(projects.client.clientEquipment)
        api(projects.client.clientFeatures)
        api(projects.client.clientUsers)
        api(ktorLibs.client.core)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
