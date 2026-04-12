import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.anonymize.json)
}

kotlin {
    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
    sourceSets.commonMain {
        dependencies {
            api(projects.api.feature.annotations)
            api(projects.api.feature.implementations)
            api(libs.kotlinx.serialization.json)
            api(projects.api.feature.common)
        }
    }
}

tasks.anonymizeJsonVerify {
    // all JSONs are now anonymized or generated, for now I don't expect to add new ones, and this takes a lot of time
    enabled = false
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(tasks.anonymizeJsonVerify)
}
