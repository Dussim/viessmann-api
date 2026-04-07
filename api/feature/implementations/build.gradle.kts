import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features.yaml)
    alias(conventions.plugins.xyz.dussim.generate.features.json)
    alias(conventions.plugins.xyz.dussim.generate.features.json.tests)
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
    sourceSets.getByName("jvmTest") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/feature-json-tests"))
        resources.srcDir(layout.buildDirectory.dir("generated/feature-jsons"))
    }
}

generateFeatureInterfacesFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedSources = layout.buildDirectory.dir("generated/features")
    packageName = "xyz.dussim.viessmann.api.features.generated"
}

generateFeatureJsonsFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")
}

generateFeatureJsonTests {
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")
    generatedTests = layout.buildDirectory.dir("generated/feature-json-tests")
}

tasks.named("generateFeatureJsonTests") {
    dependsOn("generateFeatureJsonsFromYaml")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    dependsOn("generateFeatureInterfacesFromYaml")
    dependsOn("generateFeatureJsonTests")
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
