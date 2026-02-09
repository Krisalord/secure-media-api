package io.github.krisalord.model.media.dto

import io.github.krisalord.model.media.Genre
import io.github.krisalord.model.media.WatchStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMediaRequest(
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus
)