package io.github.krisalord.services

import io.github.krisalord.model.media.MediaModel
import io.github.krisalord.model.media.MediaRequest
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.MediaValidation
import org.bson.types.ObjectId
import java.time.LocalDateTime

class MediaService(
    private val mediaRepository: MediaRepository
) {
    fun createMedia(
        userId: String,
        request: MediaRequest
    ): MediaModel {
        val cleanTitle = Sanitizer.sanitizeText(request.title)

        MediaValidation.validateMedia(request)

        return mediaRepository.addMedia(
            MediaModel(
                id = ObjectId(),
                userId = userId,
                title = cleanTitle,
                genres = request.genres,
                rating = request.rating,
                status = request.status
            )
        )
    }

    fun getMediaByUserId(
        userId: String
    ): List<MediaModel> {
        return mediaRepository.getAllMediaByUserId(userId)
    }

    fun getMediaByMediaId(
        userId: String,
        mediaId: String
    ): MediaModel? {
        return mediaRepository.getMediaByMediaId(ObjectId(mediaId), userId)
    }

    fun updateMedia(
        userId: String,
        mediaId: String,
        request: MediaRequest
    ): Boolean {
        MediaValidation.validateMedia(request)
        val sanitizedTitle = Sanitizer.sanitizeText(request.title)

        return mediaRepository.updateMedia(
            MediaModel(
                id = ObjectId(mediaId),
                userId = userId,
                title = sanitizedTitle,
                genres = request.genres,
                rating = request.rating,
                status = request.status,
                updatedAt = LocalDateTime.now(),
            )
        )
    }

    fun deleteMedia(
        userId: String,
        mediaId: String
    ): Boolean {
        return mediaRepository.deleteMedia(ObjectId(mediaId), userId)
    }
}