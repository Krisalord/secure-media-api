package io.github.krisalord.media

import io.github.krisalord.auth.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object MediaTable : UUIDTable("media") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val mediaType = varchar("media_type", 50)
    val rating = integer("rating")
    val genre = varchar("genre", 50)
    val watchedAt = timestamp("watched_at").defaultExpression(CurrentTimestamp)
}

data class WatchedMediaModel(
    val id: String,
    val userId: String,
    val title: String,
    val mediaType: String,
    val rating: Int,
    val genre: Genre,
    val watchedAt: Instant
)