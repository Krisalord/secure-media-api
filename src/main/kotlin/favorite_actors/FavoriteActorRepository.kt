package io.github.krisalord.favorite_actors

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class FavoriteActorRepository {
    private fun toModel(row: ResultRow): FavoriteActorModel = FavoriteActorModel(
        id = row[FavoriteActorTable.id].value.toString(),
        userId = row[FavoriteActorTable.userId].toString(),
        name = row[FavoriteActorTable.name]
    )

    suspend fun create(model: FavoriteActorModel): FavoriteActorModel = newSuspendedTransaction {
        val insertedRow = FavoriteActorTable.insert {
            it[userId] = UUID.fromString(model.userId)
            it[name] = model.name
        }
        toModel(insertedRow.resultedValues?.first()!!)
    }

    suspend fun findAllByUserId(userId: String): List<FavoriteActorModel> = newSuspendedTransaction {
        FavoriteActorTable
            .selectAll()
            .where { FavoriteActorTable.userId eq UUID.fromString(userId) }
            .map { toModel(it) }
    }

    suspend fun deleteByIdAndUserId(id: String, userId: String): Boolean = newSuspendedTransaction {
        val favoriteActorUuid = runCatching { UUID.fromString(id) }.getOrNull() ?: return@newSuspendedTransaction false
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return@newSuspendedTransaction false

        val deletedRows = FavoriteActorTable.deleteWhere {
            (FavoriteActorTable.id eq favoriteActorUuid) and (FavoriteActorTable.userId eq userUuid)
        }
        deletedRows == 1
    }
}