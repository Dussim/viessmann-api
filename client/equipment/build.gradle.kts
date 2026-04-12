plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.client)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":client:client-core"))
        api(project(":api:dto"))
        api(project(":api:equipment"))
        api(project(":api:errors"))
    }
    sourceSets.commonTest.dependencies {
        implementation(ktorLibs.client.mock)
    }
}
