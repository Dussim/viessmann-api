pluginManagement {
    repositories {
        gradlePluginPortal {
            content { excludeGroup("xyz.dussim") }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal {
            content { excludeGroup("xyz.dussim") }
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.dussim.xyz/snapshots")
            }
            filter {
                includeGroupAndSubgroups("xyz.dussim")
            }
        }
    }
    versionCatalogs {
        register("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
