package io.github.krisalord.validation

import io.github.krisalord.errors.MediaValidationException
import io.github.krisalord.model.media.MediaRequest
import io.github.krisalord.model.media.WatchStatus

object MediaValidation{
    fun validateMedia(request: MediaRequest) {
        if (request.title.isBlank()) {
            throw MediaValidationException("Title cannot be blank")
        }

        if (request.rating !in 0..10) {
            throw MediaValidationException("Rating must be between 0 and 10")
        }

        if (request.genres.isEmpty()) {
            throw MediaValidationException("At least one genre is required")
        }
    }
}