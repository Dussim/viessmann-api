dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
        create("conventions") {
            from(files("gradle/conventions.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
