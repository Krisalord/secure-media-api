package io.github.krisalord.favorite_actors

import io.github.krisalord.core.database.dbQuery

class FavoriteActorService(private val favoriteActorRepository: FavoriteActorRepository) {

    suspend fun logFavoriteActor(userId: String, request: CreateFavoriteActorRequest): FavoriteActorModel = dbQuery {
        FavoriteActorValidator.validateLogRequest(request)
        val sanitizedName = request.name.trim().replace(Regex("\\s+"), " ")

        val favoriteActorToCreate = FavoriteActorModel.create(
            userId = userId,
            sanitizedName = sanitizedName
        )

        favoriteActorRepository.create(favoriteActorToCreate)
    }

    suspend fun getFavoriteActorList(userId: String): List<FavoriteActorModel> = dbQuery {
        favoriteActorRepository.findAllByUserId(userId)
    }

    suspend fun removeFavoriteActor(id: String, userId: String): Boolean = dbQuery {
        val deleted = favoriteActorRepository.deleteByIdAndUserId(id, userId)
        if (!deleted) throw FavoriteActorNotFoundException("Favorite actor not found or access denied.")
        true
    }
}