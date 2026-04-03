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
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            implementation(projects.api.feature.annotations)

            api(libs.kotlinx.serialization.json)

            api(projects.api.feature.common)
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(tasks.anonymizeJsonVerify)
}
