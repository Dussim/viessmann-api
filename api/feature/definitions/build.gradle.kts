import com.google.devtools.ksp.gradle.KspAATask
import xyz.dussim.buildlogic.GenerateFeatureJsonsFromYamlTask
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File
import java.time.LocalDate

plugins {
    alias(conventions.plugins.xyz.dussim.build.parameters)
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features)
    alias(conventions.plugins.xyz.dussim.generate.features.json)
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

val generateFeatureJsonsFromYamlTask = tasks.named<GenerateFeatureJsonsFromYamlTask>("generateFeatureJsonsFromYaml")

generateFeatureJsonsFromYaml {
    featuresYamls = openApiFeaturesDirectory
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")

    currentDate = LocalDate.of(2000, 1, 1)
}

generateFeatureInterfaces {
    featuresJsons = generateFeatureJsonsFromYamlTask.flatMap { it.generatedJsons }
    generatedSources = layout.buildDirectory.dir("generated/features")
    sourceSets = listOf("jvmMain", "jsMain")
    packageName = "xyz.dussim.viessmann.api.features.generated"
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn("generateFeatureInterfaces")
}

tasks.withType<KspAATask>().configureEach {
    dependsOn("generateFeatureInterfaces")
}

tasks.matching { it.name in setOf("sourcesJar", "jvmSourcesJar", "jsSourcesJar") }.configureEach {
    dependsOn("generateFeatureInterfaces")
}

dokka {
    dokkaSourceSets.jvmMain {
        sourceRoots.from(tasks.generateFeatureInterfaces.map { it.outputs })
    }
    dokkaSourceSets.jsMain {
        sourceRoots.from(tasks.generateFeatureInterfaces.map { it.outputs })
    }
}
