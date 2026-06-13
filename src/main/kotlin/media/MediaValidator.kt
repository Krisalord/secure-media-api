package io.github.krisalord.media

object MediaValidator {
    private val allowedMediaTypes = setOf("MOVIE", "TV_SHOW")

    fun validateLogRequest(request: CreateMediaRequest) {
        if (request.title.isBlank()) {
            throw InvalidMediaDataException("Title cannot be blank.")
        }
        if (request.title.length > 255) {
            throw InvalidMediaDataException("Title length cannot exceed 255 characters.")
        }
        if (request.mediaType.uppercase() !in allowedMediaTypes) {
            throw InvalidMediaDataException("Media type must be either MOVIE or TV_SHOW.")
        }
        if (request.rating !in 1..5) {
            throw InvalidMediaDataException("Rating must be an integer between 1 and 5.")
        }
        if (Genre.fromString(request.genre) == null) {
            throw InvalidMediaDataException("Unsupported or invalid genre: ${request.genre}.")
        }
    }
}