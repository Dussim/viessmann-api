plugins {
    id("org.gradlex.build-parameters").version("1.4.5")
}

buildParameters {
    pluginId("xyz.dussim.build-parameters")

    bool("ci") {
        fromEnvironment()
        defaultValue = false
        description = "True if the build is running in CI environment"
    }

    string("openApiPath") {
        fromEnvironment("OPEN_API_PATH")
        defaultValue = ".ignored/featuresOpenApi"
        description = "Path to the OpenAPI sources directory."
    }

    group("cache") {
        string("url") {
            fromEnvironment("BUILD_CACHE_URL")
            description = "Url of remote cache node."
            defaultValue = "https://build-cache.dussim.xyz/cache/"
        }
        string("username") {
            fromEnvironment("BUILD_CACHE_USER")
            description = "Username used for remote cache read/write user. Default for read only user"
            defaultValue = "build-cache-r"
        }
        string("password") {
            // NOTE: this is not any kind of credential/secret leak, this password is for read-only user, and we want every developer of this repository to take adeventage of it
            fromEnvironment("BUILD_CACHE_USER_PASSWORD")
            description = "Password used for remote cache read/write user. Default for read only user"
            defaultValue = ",79'2V`?2CuC"
        }
    }
}
