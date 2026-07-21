package io.github.krisalord.favorite_actors

import io.github.krisalord.plugins.DatabaseException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.UUID

class FavoriteActorRepository {
    private fun toModel(row: ResultRow): FavoriteActorModel = FavoriteActorModel(
        id = row[FavoriteActorTable.id].value.toString(),
        userId = row[FavoriteActorTable.userId].toString(),
        name = row[FavoriteActorTable.name]
    )

    fun create(model: FavoriteActorModel): FavoriteActorModel {
        val insertedRow = FavoriteActorTable.insert {
            it[id] = UUID.fromString(model.id)
            it[userId] = UUID.fromString(model.userId)
            it[name] = model.name
        }

        val row = insertedRow.resultedValues?.firstOrNull()
            ?: throw DatabaseException("Failed to insert favorite actor: No auto-generated keys returned.")

        return toModel(row)
    }

    fun findAllByUserId(userId: String): List<FavoriteActorModel> {
        return FavoriteActorTable
            .selectAll()
            .where { FavoriteActorTable.userId eq UUID.fromString(userId) }
            .map { toModel(it) }
    }

    fun deleteByIdAndUserId(id: String, userId: String): Boolean {
        val favoriteActorUuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return false
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return false

        val deletedRows = FavoriteActorTable.deleteWhere {
            (FavoriteActorTable.id eq favoriteActorUuid) and (FavoriteActorTable.userId eq userUuid)
        }

        return deletedRows == 1
    }
}