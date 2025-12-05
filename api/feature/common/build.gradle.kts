plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.serialization.json)
    }
}
