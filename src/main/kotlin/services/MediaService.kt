package io.github.krisalord.services

import io.github.krisalord.errors.NotFoundException
import io.github.krisalord.models.media.MediaModel
import io.github.krisalord.models.media.mappers.toCreateMediaResponse
import io.github.krisalord.models.media.mappers.toSearchMediaResponse
import io.github.krisalord.models.media.dto.CreateMediaRequest
import io.github.krisalord.models.media.dto.CreateMediaResponse
import io.github.krisalord.models.media.dto.SearchMediaResponse
import io.github.krisalord.models.media.dto.UpdateMediaRequest
import io.github.krisalord.repositories.MediaRepository
import org.bson.types.ObjectId

class MediaService(private val mediaRepository: MediaRepository) {
    suspend fun createMedia(userId: String, request: CreateMediaRequest): CreateMediaResponse {
        val media = MediaModel.createNewMedia(userId, request)
        val savedMedia = mediaRepository.addMedia(media)
        return savedMedia.toCreateMediaResponse()
    }

    suspend fun getMediaByUserId(userId: String): List<SearchMediaResponse> {
        return mediaRepository
            .getAllMediaByUserId(userId)
            .map { it.toSearchMediaResponse() }
    }

    suspend fun getMediaByMediaId(userId: String, mediaId: String): SearchMediaResponse {
        val media = mediaRepository.getMediaByMediaId(ObjectId(mediaId), userId)
            ?: throw NotFoundException("Media not found")

        return media.toSearchMediaResponse()
    }

    suspend fun updateMedia(userId: String, mediaId: String, request: UpdateMediaRequest) {
        val media = MediaModel.updateExistingMedia(mediaId, userId, request)
        val updated = mediaRepository.updateMedia(media)
        if (!updated) {
            throw NotFoundException("Media not found")
        }
    }

    suspend fun deleteMedia(userId: String, mediaId: String) {
        val deleted = mediaRepository.deleteMedia(ObjectId(mediaId), userId)
        if (!deleted) {
            throw NotFoundException("Media not found")
        }
    }


}