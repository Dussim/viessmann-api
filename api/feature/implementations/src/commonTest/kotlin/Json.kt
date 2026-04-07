package xyz.dussim.viessmann.feature.api

import kotlinx.serialization.json.Json

val json =
    Json {
        explicitNulls = false
        encodeDefaults = false
        useAlternativeNames = false

        ignoreUnknownKeys = true
    }
