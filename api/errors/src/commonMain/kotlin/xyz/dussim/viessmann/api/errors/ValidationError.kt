package xyz.dussim.viessmann.api.errors

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ValidationError(
    val message: String,
    val path: List<String>,
    val type: String,
    val context: JsonObject? = null,
)
