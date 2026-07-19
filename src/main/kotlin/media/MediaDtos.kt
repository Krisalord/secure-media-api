package io.github.krisalord.media

import kotlinx.serialization.Serializable

@Serializable
data class CreateMediaRequest(
    val title: String,
    val mediaType: String,
    val rating: Int
)
@Serializable
data class MediaResponse(
    val id: String,
    val userId: String,
    val title: String,
    val mediaType: String,
    val rating: Int,
    val watchedAt: String,
    val posterUrl: String? = null
)
fun WatchedMediaModel.toResponse(): MediaResponse = MediaResponse(
    id = this.id,
    userId = this.userId,
    title = this.title,
    mediaType = this.mediaType,
    rating = this.rating,
    watchedAt = this.watchedAt.toString(),
    posterUrl = this.posterUrl
)