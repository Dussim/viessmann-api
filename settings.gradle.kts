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
            from("io.ktor:ktor-version-catalog:3.5.1")
        }
    }
}

plugins {
    id("com.gradle.develocity").version("4.5.0")
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
    ":client:test-support",
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
project(":client:test-support").name = "client-test-support"
project(":api:feature:annotations").name = "api-feature-annotations"
project(":api:feature:processor").name = "api-feature-processor"
project(":api:feature:common").name = "api-feature-common"
project(":api:feature:benchmark").name = "api-feature-benchmark"
project(":api:feature:definitions").name = "api-feature-definitions"
project(":api:feature:implementations").name = "api-feature-implementations"

fun warnIfBlankBuildParameter(
    name: String,
    value: String,
) {
    if (value.isBlank()) {
        logger.warn("Build parameter '$name' is empty. Check the matching Gradle property or environment variable.")
    }
}

fun warnIfBlankEnvironmentVariable(name: String) {
    if (providers.environmentVariable(name).orNull.isNullOrBlank()) {
        logger.warn("Environment variable '$name' is empty or not set.")
    }
}

fun resolveSettingsRelativePath(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file else settingsDir.resolve(file.path)
}

fun warnIfDirectoryMissingOrEmpty(
    name: String,
    directory: File,
) {
    if (!directory.isDirectory) {
        logger.warn("$name does not exist or is not a directory: $directory")
    } else if (directory.listFiles()?.isEmpty() != false) {
        logger.warn("$name is empty: $directory")
    }
}

warnIfBlankBuildParameter("openApiPath", buildParameters.openApiPath)
warnIfBlankBuildParameter("cache.username", buildParameters.cache.username)
warnIfBlankBuildParameter("cache.password", buildParameters.cache.password)

if (buildParameters.ci) {
    listOf(
        "OPEN_API_PATH",
        "BUILD_CACHE_USER",
        "BUILD_CACHE_USER_PASSWORD",
        "REPO_USERNAME",
        "REPO_PASSWORD",
    ).forEach(::warnIfBlankEnvironmentVariable)
}

if (buildParameters.openApiPath.isNotBlank()) {
    val openApiDirectory = resolveSettingsRelativePath(buildParameters.openApiPath)
    val openApiFeaturesDirectory = openApiDirectory.resolve("features")

    warnIfDirectoryMissingOrEmpty("OpenAPI directory configured by 'openApiPath'", openApiDirectory)
    warnIfDirectoryMissingOrEmpty("OpenAPI features directory", openApiFeaturesDirectory)

    if (
        openApiFeaturesDirectory.isDirectory &&
        openApiFeaturesDirectory
            .listFiles { file -> file.isFile && file.extension in setOf("yaml", "yml") }
            ?.isEmpty() != false
    ) {
        logger.warn("OpenAPI features directory contains no YAML files: $openApiFeaturesDirectory")
    }
}

develocity {
    val ci = buildParameters.ci
    val gitHash = providers.environmentVariable("CI_COMMIT_SHA")
    val gitBranch = providers.environmentVariable("CI_COMMIT_BRANCH")
    buildScan {
        uploadInBackground = false
        publishing.onlyIf { false }

        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"

        capture {
            buildLogging = true
            testLogging = true
        }

        tag(
            when (ci) {
                true -> "CI"
                false -> "LOCAL"
            },
        )

        if (ci) {
            value("Git Commit ID", gitHash.get())
            value("Git Branch", gitBranch.get())
        }
    }
}

buildCache {
    // Please never log the password in the build script, so that it won't be exposed in the CI logs
    logger.warn(
        """
        Build cache configuration:
        Local:
        - isEnabled: ${!buildParameters.ci}
        - isPush:    true
        Remote:
        - isEnabled: true
        - isPush:    ${buildParameters.ci}
        - url:       ${buildParameters.cache.url}
        - user:      ${buildParameters.cache.username}
        """.trimIndent(),
    )

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
