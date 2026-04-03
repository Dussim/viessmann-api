import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features.yaml)
}

dependencies {
    add("kspCommonMainMetadata", projects.api.feature.processor)
}

kotlin {
    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            api(projects.api.feature.common)
            api(projects.api.feature.annotations)
        }
    }
}

generateFeatureInterfacesFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedSources = layout.buildDirectory.dir("generated/features")
    packageName = "xyz.dussim.viessmann.api.features.generated"
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    dependsOn("generateFeatureInterfacesFromYaml")
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

dokka {
    dokkaSourceSets.commonMain {
        sourceRoots.from(tasks.generateFeatureInterfacesFromYaml.map { it.outputs })
        sourceRoots.from(tasks.named("kspCommonMainKotlinMetadata").map { it.outputs })
    }
}
