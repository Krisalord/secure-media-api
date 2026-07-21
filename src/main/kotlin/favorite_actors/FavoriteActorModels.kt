package io.github.krisalord.favorite_actors

import io.github.krisalord.auth.UsersTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import java.util.UUID

object FavoriteActorTable : UUIDTable("favorite_actors") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
}

data class FavoriteActorModel(
    val id: String,
    val userId: String,
    val name: String
) {
    companion object {
        fun create(
            userId: String,
            sanitizedName: String
        ): FavoriteActorModel {
            return FavoriteActorModel(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = sanitizedName
            )
        }
    }
}

fun FavoriteActorModel.toResponse(): FavoriteActorResponse = FavoriteActorResponse(
    id = this.id,
    userId = this.userId,
    name = this.name
)

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