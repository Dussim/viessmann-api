enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories.mavenCentral()
}

rootProject.name = "viessmann-api"

include(
    ":api:dto",
    ":api:feature:annotations",
    ":api:feature:processor",
)