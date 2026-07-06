package io.github.krisalord.media

class MediaService(private val mediaRepository: MediaRepository) {

    suspend fun logMedia(userId: String, request: CreateMediaRequest): WatchedMediaModel {
        MediaValidator.validateLogRequest(request)
        val sanitizedTitle = request.title.trim().replace(Regex("\\s+"), " ")



        val mediaToCreate = WatchedMediaModel.create(
            userId = userId,
            sanitizedTitle = sanitizedTitle,
            rawMediaType = request.mediaType,
            rating = request.rating
        )

        return mediaRepository.create(mediaToCreate)
    }

    suspend fun getWatchHistory(userId: String): List<WatchedMediaModel> {
        return mediaRepository.findAllByUserId(userId)
    }

    suspend fun removeMedia(id: String, userId: String): Boolean {
        val deleted = mediaRepository.deleteByIdAndUserId(id, userId)
        if (!deleted) throw MediaNotFoundException("Media log entry not found or access denied.")
        return true
    }
}