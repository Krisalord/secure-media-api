package io.github.krisalord.model.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaRequest(
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus = WatchStatus.COMPLETED
)

@Serializable
data class MediaResponse(
    val id: String,
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus
)