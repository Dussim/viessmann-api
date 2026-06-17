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
            api(projects.api.feature.apiFeatureAnnotations)
            api(projects.api.feature.apiFeatureDefinitions)
            api(libs.kotlinx.serialization.json)
            api(projects.api.feature.apiFeatureCommon)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(projects.api.feature.apiFeatureImplementations)
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
