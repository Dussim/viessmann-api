enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories.gradlePluginPortal()
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories.mavenCentral()

    versionCatalogs {
        create("conventions") {
            from(files("../conventions.versions.toml"))
        }
    }
}

rootProject.name = "git-revision-plugin"
