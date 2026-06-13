package io.github.krisalord.media

import kotlinx.serialization.Serializable

@Serializable
data class CreateMediaRequest(
    val title: String,
    val mediaType: String,
    val rating: Int,
    val genre: String
)