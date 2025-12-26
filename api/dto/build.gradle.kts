import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.anonymize.json)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}

kotlin {
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
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<FormatTask>().configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
    source = source.minus(fileTree("build/generated/ksp")).asFileTree
}

tasks.sourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.jvmSourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.jsSourcesJar {
    dependsOn("kspCommonMainKotlinMetadata")
}
