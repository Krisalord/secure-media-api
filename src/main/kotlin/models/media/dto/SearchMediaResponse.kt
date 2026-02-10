package io.github.krisalord.models.media.dto

import io.github.krisalord.models.media.Genre
import io.github.krisalord.models.media.WatchStatus
import kotlinx.serialization.Serializable

@Serializable
data class SearchMediaResponse(
    val id: String,
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus
)