package io.github.krisalord.favorite_actors

import io.github.krisalord.auth.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

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
                id = "",
                userId = userId,
                name = sanitizedName
            )
        }
    }
}