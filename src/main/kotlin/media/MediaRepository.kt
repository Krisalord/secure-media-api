package io.github.krisalord.media

import io.github.krisalord.core.exceptions.DatabaseException
import io.ktor.server.auth.OAuth2Exception
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.util.UUID
import javax.xml.crypto.Data

class MediaRepository {
    private fun toModel(row: ResultRow): WatchedMediaModel = WatchedMediaModel(
        id = row[MediaTable.id].value,
        userId = row[MediaTable.userId].value,
        title = row[MediaTable.title],
        mediaType = row[MediaTable.mediaType],
        rating = row[MediaTable.rating],
        watchedAt = row[MediaTable.watchedAt],
        posterUrl = row[MediaTable.posterUrl]
    )

    fun create(model: WatchedMediaModel): WatchedMediaModel {
        try {
            val insertedRow = MediaTable.insert {
                it[id] = model.id ?: UUID.randomUUID()
                it[userId] = model.userId
                it[title] = model.title
                it[mediaType] = model.mediaType
                it[rating] = model.rating
                it[posterUrl] = model.posterUrl
            }

            val row = insertedRow.resultedValues?.firstOrNull()
                ?: throw DatabaseException("Failed to insert media: No auto-generated keys returned.")

            return toModel(row)
        } catch (e: DatabaseException) {
            throw DatabaseException("Unexpected database error")
        }
    }

    fun findAllByUserId(userId: UUID): List<WatchedMediaModel> {
        return MediaTable
            .selectAll()
            .where { MediaTable.userId eq userId }
            .map { toModel(it) }
    }

    fun deleteByIdAndUserId(mediaId: UUID, userId: UUID): Boolean {
        val deletedRows = MediaTable.deleteWhere {
            (MediaTable.id eq mediaId) and (MediaTable.userId eq userId)
        }
        return deletedRows == 1
    }
}