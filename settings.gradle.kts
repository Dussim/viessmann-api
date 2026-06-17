import xyz.dussim.settings.GitRevisionValueSource

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")
    includeBuild("gradle/build-parameters")
    includeBuild("gradle/git-revision-plugin")
    repositories {
        gradlePluginPortal()
        mavenCentral()
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
            from("io.ktor:ktor-version-catalog:3.5.0")
        }
    }
}

plugins {
    id("com.gradle.develocity").version("4.4.3")
    id("xyz.dussim.build-parameters")
    id("xyz.dussim.git-revision")
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

project(":api:dto").name = "api-dto"
project(":api:errors").name = "api-errors"
project(":api:equipment").name = "api-equipment"
project(":api:features").name = "api-features"
project(":api:auth").name = "api-auth"
project(":api:users").name = "api-users"
project(":client:core").name = "client-core"
project(":client:auth").name = "client-auth"
project(":client:equipment").name = "client-equipment"
project(":client:features").name = "client-features"
project(":client:users").name = "client-users"
project(":client:facade").name = "client-facade"
project(":api:feature:annotations").name = "api-feature-annotations"
project(":api:feature:processor").name = "api-feature-processor"
project(":api:feature:common").name = "api-feature-common"
project(":api:feature:benchmark").name = "api-feature-benchmark"
project(":api:feature:definitions").name = "api-feature-definitions"
project(":api:feature:implementations").name = "api-feature-implementations"

// if (System.getenv("VIESSMANN_API_DEV") == "true") {
//    include(
//        ":client:auth-secret",
//        ":client:integration-test",
//    )
//    project(":client:auth-secret").name = "client-auth-secret"
//    project(":client:integration-test").name = "client-integration-test"
// }

develocity {
    val gitHash = providers.of(GitRevisionValueSource::class) {}
    buildScan {
        publishing.onlyIf { false }

        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"

        capture {
            buildLogging = true
            testLogging = true
        }

        tag(
            when (buildParameters.ci) {
                true -> "CI"
                false -> "LOCAL"
            },
        )

        background {
            if (!buildParameters.ci) {
                value("Git Commit ID", gitHash.get())
            }
        }
    }
}

buildCache {
    local {
        isEnabled = !buildParameters.ci
        isPush = true
    }

    remote<HttpBuildCache> {
        isPush = buildParameters.ci

        url = uri(buildParameters.cache.url)

        credentials {
            username = buildParameters.cache.username
            password = buildParameters.cache.password
        }
    }
}
