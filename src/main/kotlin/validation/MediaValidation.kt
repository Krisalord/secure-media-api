package io.github.krisalord.validation

import io.github.krisalord.errors.MediaValidationException
import io.github.krisalord.model.media.dto.CreateMediaRequest
import io.github.krisalord.model.media.dto.UpdateMediaRequest

object MediaValidation{
    fun validateCreateMedia(request: CreateMediaRequest) {
        validate(request.title, request.rating, request.genres)
    }
    fun validateUpdateMedia(request: UpdateMediaRequest) {
        validate(request.title, request.rating, request.genres)
    }
    fun validate(title: String, rating: Int, genres: List<*>) {
        if (title.isBlank()) {
            throw MediaValidationException("Title cannot be blank")
        }

        if (rating !in 0..10) {
            throw MediaValidationException("Rating must be between 0 and 10")
        }

        if (genres.isEmpty()) {
            throw MediaValidationException("At least one genre is required")
        }
    }
}