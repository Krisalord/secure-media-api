package io.github.krisalord.model.media

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

data class MediaModel(
    @BsonId val id: ObjectId,
    val userId: String,
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

fun MediaModel.toResponse(): MediaResponse =
    MediaResponse(
        id = this.id.toHexString(),
        title = this.title,
        genres = this.genres,
        rating = this.rating,
        status = this.status
    )