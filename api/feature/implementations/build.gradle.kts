import com.google.devtools.ksp.gradle.KspAATask
import xyz.dussim.buildlogic.GenerateFeatureJsonsFromYamlTask
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import java.time.LocalDate

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features.json)
    alias(conventions.plugins.xyz.dussim.generate.features.json.tests)
}

val definitionsProject = project(":api:feature:api-feature-definitions")
val generatedFeatureDefinitionSources = definitionsProject.layout.buildDirectory.dir("generated/features")
val generateFeatureDefinitions = definitionsProject.tasks.named("generateFeatureInterfacesFromYaml")

val jvmKspSources = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")
val jsKspSources = layout.buildDirectory.dir("generated/ksp/js/jsMain/kotlin")

val featureProcessorProject = project(":api:feature:api-feature-processor")
val featureProcessorInputs =
    files(
        featureProcessorProject.layout.projectDirectory.dir("src/main"),
        featureProcessorProject.layout.projectDirectory.file("build.gradle.kts"),
    )
val generateFeatureJsonsFromYamlTask = tasks.named<GenerateFeatureJsonsFromYamlTask>("generateFeatureJsonsFromYaml")

kotlin {
    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }

    sourceSets.commonMain {
        dependencies {
            api(projects.api.feature.apiFeatureCommon)
            api(projects.api.feature.apiFeatureAnnotations)
            api(projects.api.feature.apiFeatureDefinitions)
        }
    }

    sourceSets.named("jvmTest") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/feature-json-tests"))
        resources.srcDir(layout.buildDirectory.dir("generated/feature-jsons"))
    }
}

dependencies {
    add("kspJvm", projects.api.feature.apiFeatureProcessor)
    add("kspJs", projects.api.feature.apiFeatureProcessor)
}

ksp {
    arg("formatGeneratedSources", "false")
}

generateFeatureJsonsFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")

    currentDate = LocalDate.of(2000, 1, 1)
}

generateFeatureJsonTests {
    generatedJsons = generateFeatureJsonsFromYamlTask.flatMap { it.generatedJsons }
    generatedTests = layout.buildDirectory.dir("generated/feature-json-tests")
}

tasks.withType<KspAATask>().configureEach {
    if (name in setOf("kspKotlinJvm", "kspKotlinJs")) {
        dependsOn(generateFeatureDefinitions)
        kspConfig.sourceRoots.from(generatedFeatureDefinitionSources)
        kspConfig.javaSourceRoots.from(generatedFeatureDefinitionSources)
        inputs
            .files(featureProcessorInputs)
            .withPropertyName("featureImplementationProcessorInputs")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }
}

tasks.named("compileTestKotlinJvm") {
    dependsOn("generateFeatureJsonTests")
}

tasks.named("jvmTestProcessResources") {
    dependsOn("generateFeatureJsonsFromYaml")
}

tasks.matching { it.name in setOf("sourcesJar", "jvmSourcesJar", "jsSourcesJar") }.configureEach {
    dependsOn("kspKotlinJvm", "kspKotlinJs")
}

tasks.named<Jar>("jvmSourcesJar") {
    from(jvmKspSources)
}

tasks.named<Jar>("jsSourcesJar") {
    from(jsKspSources)
}

dokka {
    dokkaSourceSets.jvmMain {
        sourceRoots.from(tasks.named("kspKotlinJvm").map { it.outputs })
    }
    dokkaSourceSets.jsMain {
        sourceRoots.from(tasks.named("kspKotlinJs").map { it.outputs })
    }
}
