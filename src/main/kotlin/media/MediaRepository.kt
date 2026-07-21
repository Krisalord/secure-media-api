package io.github.krisalord.media

import io.github.krisalord.plugins.DatabaseException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID

class MediaRepository {
    private fun toModel(row: ResultRow): WatchedMediaModel = WatchedMediaModel(
        id = row[MediaTable.id].value.toString(),
        userId = row[MediaTable.userId].toString(),
        title = row[MediaTable.title],
        mediaType = row[MediaTable.mediaType],
        rating = row[MediaTable.rating],
        watchedAt = row[MediaTable.watchedAt],
        posterUrl = row[MediaTable.posterUrl]
    )

    fun create(model: WatchedMediaModel): WatchedMediaModel {
        val insertedRow = MediaTable.insert {
            it[id] = UUID.fromString(model.id)
            it[userId] = UUID.fromString(model.userId)
            it[title] = model.title
            it[mediaType] = model.mediaType
            it[rating] = model.rating
            it[posterUrl] = model.posterUrl
        }

        val row = insertedRow.resultedValues?.firstOrNull()
            ?: throw DatabaseException("Failed to insert media: No auto-generated keys returned.")

        return toModel(row)
    }

    fun findAllByUserId(userId: String): List<WatchedMediaModel> {
        return MediaTable
            .selectAll()
            .where { MediaTable.userId eq UUID.fromString(userId) }
            .map { toModel(it) }
    }

    fun deleteByIdAndUserId(id: String, userId: String): Boolean {
        val mediaUuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return false
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return false

        val deletedRows = MediaTable.deleteWhere {
            (MediaTable.id eq mediaUuid) and (MediaTable.userId eq userUuid)
        }

        return deletedRows == 1
    }
}