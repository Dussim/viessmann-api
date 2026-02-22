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
    @Suppress("UnstableApiUsage")
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
