package io.github.krisalord.favorite_actors

object FavoriteActorValidator {
    fun validateLogRequest(request: CreateFavoriteActorRequest) {
        if (request.name.isBlank()) {
            throw InvalidFavoriteActorDataException("Name cannot be blank.")
        }
        if (request.name.length > 255) {
            throw InvalidFavoriteActorDataException("Name length cannot exceed 255 characters.")
        }
    }
}