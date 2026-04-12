plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.serialization.json)
    }
}
