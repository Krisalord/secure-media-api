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
    val watchedAt = timestamp("watched_at").defaultExpression(CurrentTimestamp)
    val posterUrl = varchar("poster_url", 500).nullable()
}

data class WatchedMediaModel(
    val id: String,
    val userId: String,
    val title: String,
    val mediaType: String,
    val rating: Int,
    val watchedAt: Instant,
    val posterUrl: String?
) {
    companion object {
        fun create(
            userId: String,
            sanitizedTitle: String,
            rawMediaType: String,
            rating: Int,
            posterUrl: String?
        ): WatchedMediaModel {
            return WatchedMediaModel(
                id = "",
                userId = userId,
                title = sanitizedTitle,
                mediaType = rawMediaType,
                rating = rating,
                watchedAt = Instant.now(),
                posterUrl = posterUrl
            )
        }
    }
}