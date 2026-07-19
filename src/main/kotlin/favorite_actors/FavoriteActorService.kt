package io.github.krisalord.favorite_actors

class FavoriteActorService(private val favoriteActorRepository: FavoriteActorRepository) {
    suspend fun logFavoriteActor(userId: String, request: CreateFavoriteActorRequest): FavoriteActorModel {
        FavoriteActorValidator.validateLogRequest(request)
        val sanitizedName = request.name.trim().replace(Regex("\\s+"), " ")

        val favoriteActorToCreate = FavoriteActorModel.create(
            userId = userId,
            sanitizedName = sanitizedName
        )

        return favoriteActorRepository.create(favoriteActorToCreate)
    }

    suspend fun getFavoriteActorList(userId: String): List<FavoriteActorModel> {
        return favoriteActorRepository.findAllByUserId(userId)
    }

    suspend fun removeFavoriteActor(id: String, userId: String): Boolean {
        val deleted = favoriteActorRepository.deleteByIdAndUserId(id, userId)
        if (!deleted) throw FavoriteActorNotFoundException("Favorite actor not found or access denied.")
        return true
    }
}