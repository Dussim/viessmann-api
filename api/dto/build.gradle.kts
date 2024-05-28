import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    js {
        nodejs()
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
    }

    sourceSets.commonMain.dependencies {
        api(libs.org.jetbrains.kotlinx.kotlinx.serialization.core)
        api(libs.org.jetbrains.kotlinx.kotlinx.datetime)
    }
}

detekt {
    buildUponDefaultConfig = true

    source.setFrom("src")
}
