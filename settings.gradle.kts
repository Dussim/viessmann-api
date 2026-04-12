enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven("https://maven.dussim.xyz/snapshots")
            }
            filter {
                includeGroupAndSubgroups("xyz.dussim")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    versionCatalogs {
        register("conventions") {
            from(files("gradle/conventions.versions.toml"))
        }
        create("ktorLibs") {
            from("io.ktor:ktor-version-catalog:3.3.1")
        }
    }
}

rootProject.name = "viessmann-api"

include(
    ":api:dto",
    ":api:errors",
    ":api:equipment",
    ":api:features",
    ":api:auth",
    ":api:users",
    ":client:core",
    ":client:auth",
    ":client:equipment",
    ":client:features",
    ":client:users",
    ":client:facade",
    ":api:feature:annotations",
    ":api:feature:processor",
    ":api:feature:common",
    ":api:feature:benchmark",
    ":api:feature:definitions",
    ":api:feature:implementations",
)

project(":client:core").name = "client-core"
project(":client:auth").name = "client-auth"
project(":client:equipment").name = "client-equipment"
project(":client:features").name = "client-features"
project(":client:users").name = "client-users"
project(":client:facade").name = "client-facade"
