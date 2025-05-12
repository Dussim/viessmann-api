package xyz.dussim.viessmann.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseData<T>(
    val data: List<T>,
)
