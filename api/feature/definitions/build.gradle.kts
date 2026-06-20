import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File
import java.time.LocalDate

plugins {
    alias(conventions.plugins.xyz.dussim.build.parameters)
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features.yaml)
}

val openApiFeaturesDirectory =
    layout.dir(
        providers.provider {
            val path = File(buildParameters.openApiPath)
            val openApiDirectory =
                if (path.isAbsolute) {
                    path
                } else {
                    layout.settingsDirectory.asFile.resolve(path.path)
                }
            openApiDirectory.resolve("features")
        },
    )

kotlin {
    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }

    sourceSets.commonMain {
        dependencies {
            api(projects.api.feature.apiFeatureCommon)
            api(projects.api.feature.apiFeatureAnnotations)
        }
    }
}

generateFeatureInterfacesFromYaml {
    featuresYamls = openApiFeaturesDirectory
    generatedSources = layout.buildDirectory.dir("generated/features")
    sourceSets = listOf("jvmMain", "jsMain")
    packageName = "xyz.dussim.viessmann.api.features.generated"

    currentDate = LocalDate.of(2000, 1, 1)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
}

tasks.matching { it.name in setOf("sourcesJar", "jvmSourcesJar", "jsSourcesJar") }.configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
}

dokka {
    dokkaSourceSets.jvmMain {
        sourceRoots.from(tasks.generateFeatureInterfacesFromYaml.map { it.outputs })
    }
    dokkaSourceSets.jsMain {
        sourceRoots.from(tasks.generateFeatureInterfacesFromYaml.map { it.outputs })
    }
}
