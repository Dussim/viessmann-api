package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseData<T>(
    val cursor: Cursor? = null,
    val data: List<T>,
)

@Serializable
data class Cursor(
    val next: String,
)
