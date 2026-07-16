import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register(conventions.plugins.xyz.dussim.git.revision) {
            implementationClass = "xyz.dussim.settings.GitRevisionValuePlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
    freeCompilerArgs.add("-Xjdk-release=17")
}

group = "xyz.dussim"
version = "1.0.0"

fun <T : PluginDeclaration> NamedDomainObjectContainer<T>.register(
    plugin: Provider<PluginDependency>,
    configurationAction: Action<T>,
) {
    register(plugin.get().pluginId) {
        id = name
        configurationAction.execute(this)
    }
}
