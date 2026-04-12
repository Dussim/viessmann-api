plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":client:client-core"))
        api(project(":client:client-auth"))
        api(project(":client:client-equipment"))
        api(project(":client:client-features"))
        api(project(":client:client-users"))
        api(ktorLibs.client.core)
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
