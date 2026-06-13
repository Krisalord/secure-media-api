package io.github.krisalord.media

class MediaService(private val mediaRepository: MediaRepository) {

    suspend fun logMedia(userId: String, request: CreateMediaRequest): WatchedMediaModel {
        MediaValidator.validateLogRequest(request)

        return mediaRepository.create(
            userId = userId,
            title = request.title.trim(),
            mediaType = request.mediaType.uppercase(),
            rating = request.rating,
            genre = Genre.fromString(request.genre)!!
        )
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