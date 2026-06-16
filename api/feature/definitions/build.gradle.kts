import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.time.LocalDate

plugins {
    alias(conventions.plugins.xyz.dussim.kotlin.common)
    alias(conventions.plugins.xyz.dussim.generate.features.yaml)
    alias(conventions.plugins.xyz.dussim.generate.features.json)
    alias(conventions.plugins.xyz.dussim.generate.features.json.tests)
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.api.feature.common)
            api(projects.api.feature.annotations)
        }
    }

    val implementationsCommonMain =
        sourceSets.create("implementationsCommonMain") {
            dependsOn(sourceSets.named("commonMain").get())
        }

    jvm {
        compilerOptions.jvmDefault = JvmDefaultMode.NO_COMPATIBILITY

        val implementationsCompilation =
            compilations.create("implementationsJvmMain") {
                defaultSourceSet.dependsOn(implementationsCommonMain)
                defaultSourceSet.kotlin.srcDir(
                    layout.buildDirectory.dir("generated/ksp/jvm/implementationsJvmMain/kotlin"),
                )
            }

        compilations.named("test") {
            associateWith(implementationsCompilation)
        }
    }

    js {
        compilations.create("implementationsJsMain") {
            defaultSourceSet.dependsOn(implementationsCommonMain)
            defaultSourceSet.kotlin.srcDir(
                layout.buildDirectory.dir("generated/ksp/js/implementationsJsMain/kotlin"),
            )
        }
    }

    sourceSets.named("jvmTest") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/feature-json-tests"))
        resources.srcDir(layout.buildDirectory.dir("generated/feature-jsons"))
    }
}

dependencies {
    add("kspJvmImplementationsJvmMain", projects.api.feature.processor)
    add("kspJsImplementationsJsMain", projects.api.feature.processor)
}

generateFeatureInterfacesFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedSources = layout.buildDirectory.dir("generated/features")
    packageName = "xyz.dussim.viessmann.api.features.generated"

    currentDate = LocalDate.of(2000, 1, 1)
}

generateFeatureJsonsFromYaml {
    featuresYamls = layout.settingsDirectory.dir(".ignored/featuresOpenApi/features")
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")

    currentDate = LocalDate.of(2000, 1, 1)
}

generateFeatureJsonTests {
    generatedJsons = layout.buildDirectory.dir("generated/feature-jsons")
    generatedTests = layout.buildDirectory.dir("generated/feature-json-tests")
}

tasks.named("generateFeatureJsonTests") {
    dependsOn("generateFeatureJsonsFromYaml")
}

tasks.matching { it.name == "kspImplementationsJvmMainKotlinJvm" }.configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
}

tasks.matching { it.name == "kspImplementationsJsMainKotlinJs" }.configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
    dependsOn("generateFeatureJsonTests")
}

tasks.matching { it.name in setOf("sourcesJar", "jvmSourcesJar", "jsSourcesJar") }.configureEach {
    dependsOn("generateFeatureInterfacesFromYaml")
}

tasks.matching { it.name == "compileImplementationsJvmMainKotlinJvm" }.configureEach {
    dependsOn("kspImplementationsJvmMainKotlinJvm")
}

tasks.matching { it.name == "compileImplementationsJsMainKotlinJs" }.configureEach {
    dependsOn("kspImplementationsJsMainKotlinJs")
}

dokka {
    dokkaSourceSets.commonMain {
        sourceRoots.from(tasks.generateFeatureInterfacesFromYaml.map { it.outputs })
    }
}

val jvmImplementationsJar =
    tasks.register<Jar>("jvmImplementationsJar") {
        archiveBaseName = "${project.name}-implementations-jvm"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn("jvmJar", "compileImplementationsJvmMainKotlinJvm")
        from(tasks.named<Jar>("jvmJar").map { zipTree(it.archiveFile.get().asFile) })
        from(
            kotlin
                .jvm()
                .compilations
                .getByName("implementationsJvmMain")
                .output.allOutputs,
        )
    }

val jvmImplementationsElements =
    configurations.create("jvmImplementationsElements") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }

artifacts {
    add(jvmImplementationsElements.name, jvmImplementationsJar)
}

val jsImplementationsJar =
    tasks.register<Jar>("jsImplementationsJar") {
        archiveBaseName = "${project.name}-implementations-js"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn("jsJar", "compileImplementationsJsMainKotlinJs")
        from(tasks.named<Jar>("jsJar").map { zipTree(it.archiveFile.get().asFile) })
        from(
            kotlin
                .js()
                .compilations
                .getByName("implementationsJsMain")
                .output.allOutputs,
        )
    }

val implementationsSourcesJar =
    tasks.register<Jar>("implementationsSourcesJar") {
        archiveBaseName = "${project.name}-implementations"
        archiveClassifier = "sources"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn("generateFeatureInterfacesFromYaml", "kspImplementationsJvmMainKotlinJvm", "kspImplementationsJsMainKotlinJs")
        from(kotlin.sourceSets.named("commonMain").map { it.kotlin.sourceDirectories })
        from(kotlin.sourceSets.named("implementationsCommonMain").map { it.kotlin.sourceDirectories })
        from(layout.buildDirectory.dir("generated/ksp/jvm/jvmImplementationsJvmMain/kotlin"))
        from(layout.buildDirectory.dir("generated/ksp/js/jsImplementationsJsMain/kotlin"))
    }

publishing {
    repositories {
        maven {
            name = "ignoredPublicationTest"
            url = uri(layout.settingsDirectory.dir(".ignored/maven-publication-test"))
        }
    }

    publications {
        create<MavenPublication>("implementationsJvm") {
            artifactId = "${project.name}-implementations-jvm"
            artifact(jvmImplementationsJar)
            artifact(implementationsSourcesJar)
            addCompileDependenciesToPom(project.group.toString(), project.version.toString(), "common-jvm", "annotations-jvm")
        }

        create<MavenPublication>("implementationsJs") {
            artifactId = "${project.name}-implementations-js"
            artifact(jsImplementationsJar)
            artifact(implementationsSourcesJar)
            addCompileDependenciesToPom(project.group.toString(), project.version.toString(), "common-js", "annotations-js")
        }
    }
}

fun MavenPublication.addCompileDependenciesToPom(
    dependencyGroup: String,
    dependencyVersion: String,
    vararg artifactIds: String,
) {
    pom.withXml {
        val dependenciesNode = asNode().appendNode("dependencies")
        artifactIds.forEach { dependencyArtifactId ->
            dependenciesNode
                .appendNode("dependency")
                .apply {
                    appendNode("groupId", dependencyGroup)
                    appendNode("artifactId", dependencyArtifactId)
                    appendNode("version", dependencyVersion)
                    appendNode("scope", "compile")
                }
        }
    }
}
