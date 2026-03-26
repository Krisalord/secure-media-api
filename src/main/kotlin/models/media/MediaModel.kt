package io.github.krisalord.models.media

import io.github.krisalord.models.media.dto.CreateMediaRequest
import io.github.krisalord.models.media.dto.UpdateMediaRequest
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.MediaValidation
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class MediaModel(
    @BsonId val id: ObjectId,
    val userId: String,
    val title: String,
    val genres: List<Genre>,
    val rating: Int,
    val status: WatchStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun createNewMedia(userId: String, createMediaRequest: CreateMediaRequest): MediaModel {
            MediaValidation.validateCreateMedia(createMediaRequest)
            val sanitizedTitle = Sanitizer.sanitizeText(createMediaRequest.title)

            return MediaModel(
                id = ObjectId(),
                userId = userId,
                title = sanitizedTitle,
                genres = createMediaRequest.genres,
                rating = createMediaRequest.rating,
                status = createMediaRequest.status
            )
        }

        fun updateExistingMedia(mediaId: String, userId: String, updateMediaRequest: UpdateMediaRequest): MediaModel {
            MediaValidation.validateUpdateMedia(updateMediaRequest)
            val sanitizedTitle = Sanitizer.sanitizeText(updateMediaRequest.title)

            return MediaModel(
                id = ObjectId(mediaId),
                userId = userId,
                title = sanitizedTitle,
                genres = updateMediaRequest.genres,
                rating = updateMediaRequest.rating,
                status = updateMediaRequest.status,
                updatedAt = LocalDateTime.now()
            )
        }
    }
}