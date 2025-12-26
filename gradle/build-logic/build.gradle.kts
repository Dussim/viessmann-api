import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.plugins.kotest)
    implementation(libs.plugins.kotlinter)
    implementation(libs.plugins.detekt)
    implementation(libs.plugins.dokka)
    implementation(libs.plugins.kotlin.jvm)
    implementation(libs.plugins.kotlin.multiplatform)
    implementation(libs.plugins.kotlin.serialization)
    implementation(libs.plugins.ksp)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinpoet.ksp)

    implementation("xyz.dussim:dto:0.0.2")
    implementation("xyz.dussim:annotations:0.0.2")
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
        register("anonymizeJson") {
            id = "xyz.dussim.anonymize.json"
            implementationClass = "xyz.dussim.buildlogic.AnonymizeJsonPlugin"
        }
        register("generateFeatureInterfaces") {
            id = "xyz.dussim.generate.features"
            implementationClass = "xyz.dussim.buildlogic.GenerateFeatureInterfacesFromParsedFeaturePlugin"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlinter {
    ktlintVersion = "1.8.0"
}

fun DependencyHandler.implementation(plugin: Provider<PluginDependency>): Dependency? = implementation(plugin.map { idToMavenCoordinate(it.pluginId, it.version.requiredVersion) })

fun idToMavenCoordinate(
    id: String,
    version: String,
): String = "$id:$id.gradle.plugin:$version"
