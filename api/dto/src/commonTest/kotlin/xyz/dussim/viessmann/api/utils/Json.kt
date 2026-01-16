package xyz.dussim.viessmann.api.utils

import kotlinx.serialization.json.Json

val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        useAlternativeNames = false

        ignoreUnknownKeys = true
    }
