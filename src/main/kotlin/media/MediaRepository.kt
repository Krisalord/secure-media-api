package io.github.krisalord.media

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID

class MediaRepository {
    private fun toModel(row: ResultRow): WatchedMediaModel = WatchedMediaModel(
        id = row[MediaTable.id].value.toString(),
        userId = row[MediaTable.userId].toString(),
        title = row[MediaTable.title],
        mediaType = row[MediaTable.mediaType],
        rating = row[MediaTable.rating],
        genre = Genre.fromString(row[MediaTable.genre]) ?: throw IllegalArgumentException("Invalid genre in database"),
        watchedAt = row[MediaTable.watchedAt]
    )

    suspend fun create(userId: String, title: String, mediaType: String, rating: Int, genre: Genre): WatchedMediaModel = newSuspendedTransaction {
        val insertedRow = MediaTable.insert {
            it[MediaTable.userId] = UUID.fromString(userId)
            it[MediaTable.title] = title
            it[MediaTable.mediaType] = mediaType
            it[MediaTable.rating] = rating
            it[MediaTable.genre] = genre.name
        }
        toModel(insertedRow.resultedValues?.first()!!)
    }

    suspend fun findAllByUserId(userId: String): List<WatchedMediaModel> = newSuspendedTransaction {
        MediaTable
            .selectAll()
            .where { MediaTable.userId eq UUID.fromString(userId) }
            .map { toModel(it) }
    }

    suspend fun deleteByIdAndUserId(id: String, userId: String): Boolean = newSuspendedTransaction {
        val mediaUuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return@newSuspendedTransaction false
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return@newSuspendedTransaction false

        val deletedRows = MediaTable.deleteWhere {
            (MediaTable.id eq mediaUuid) and (MediaTable.userId eq userUuid)
        }
        deletedRows == 1
    }
}