import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.ben.manes.versions)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.plugins.kotest)
    implementation(libs.plugins.kotlinter)
    implementation(libs.plugins.dokka)
    implementation(libs.plugins.kotlin.jvm)
    implementation(libs.plugins.kotlin.multiplatform)
    implementation(libs.plugins.kotlin.serialization)
    implementation(libs.plugins.ksp)

    implementation(libs.kotlinpoet.ksp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.swagger.parser)

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.runner.junit5)
}

gradlePlugin {
    plugins {
        register("kotlinCommon") {
            id = "xyz.dussim.kotlin.common"
            implementationClass = "xyz.dussim.buildlogic.KotlinCommonPlugin"
        }
        register("kotlinJvmCommon") {
            id = "xyz.dussim.kotlin.jvm.common"
            implementationClass = "xyz.dussim.buildlogic.KotlinJvmCommonPlugin"
        }
        register("kotlinClient") {
            id = "xyz.dussim.kotlin.client"
            implementationClass = "xyz.dussim.buildlogic.KotlinClientPlugin"
        }
        register("generateFeatureInterfaces") {
            id = "xyz.dussim.generate.features"
            implementationClass = "xyz.dussim.buildlogic.GenerateFeatureInterfacesFromParsedFeaturePlugin"
        }
        register("generateFeatureJsonsFromYaml") {
            id = "xyz.dussim.generate.features.json"
            implementationClass = "xyz.dussim.buildlogic.GenerateFeatureJsonsFromYamlPlugin"
        }
        register("generateFeatureJsonTests") {
            id = "xyz.dussim.generate.features.json.tests"
            implementationClass = "xyz.dussim.buildlogic.GenerateFeatureJsonTestsPlugin"
        }
        register("generateValidationResultOf") {
            id = "xyz.dussim.generate.validation.result.of"
            implementationClass = "xyz.dussim.buildlogic.GenerateValidationResultOfPlugin"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlinter {
    ktlintVersion = "1.8.0"
}

fun DependencyHandler.implementation(plugin: Provider<PluginDependency>): Dependency? = implementation(plugin.map { idToMavenCoordinate(it.pluginId, it.version.requiredVersion) })

fun idToMavenCoordinate(
    id: String,
    version: String,
): String = "$id:$id.gradle.plugin:$version"
