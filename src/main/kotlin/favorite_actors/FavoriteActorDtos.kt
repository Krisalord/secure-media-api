package io.github.krisalord.favorite_actors

import kotlinx.serialization.Serializable

@Serializable
data class CreateFavoriteActorRequest(
    val name: String
)

@Serializable
data class FavoriteActorResponse(
    val id: String,
    val userId: String,
    val name: String
)

fun FavoriteActorModel.toResponse(): FavoriteActorResponse = FavoriteActorResponse(
    id = this.id,
    userId = this.userId,
    name = this.name
)