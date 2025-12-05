enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories{
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("conventions") {
            from(files("gradle/conventions.versions.toml"))
        }
    }
}

rootProject.name = "viessmann-api"

include(
    ":api:dto",
    ":api:feature:annotations",
    ":api:feature:processor",
    ":api:feature:common",
    ":api:feature:benchmark",
)
