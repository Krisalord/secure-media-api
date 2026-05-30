package io.github.krisalord.auth.session

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

class RefreshSessionRepository {
    private fun toModel(row: ResultRow): RefreshSessionModel = RefreshSessionModel(
        id = row[UserSessionTable.id].value.toString(),
        userId = row[UserSessionTable.userId].toString(),
        refreshTokenHash = row[UserSessionTable.refreshTokenHash],
        expiresAt = row[UserSessionTable.expiresAt],
        createdAt = row[UserSessionTable.createdAt],
        isRevoked = row[UserSessionTable.isRevoked],
        userAgent = row[UserSessionTable.userAgent],
        ipAddress = row[UserSessionTable.ipAddress]
    )

    suspend fun create(session: RefreshSessionModel): RefreshSessionModel = newSuspendedTransaction {
        val insertedRow = UserSessionTable.insert {
            if (session.id.isNotEmpty()) it[id] = UUID.fromString(session.id)
            it[userId] = UUID.fromString(session.userId)
            it[refreshTokenHash] = session.refreshTokenHash
            it[expiresAt] = session.expiresAt
            it[isRevoked] = session.isRevoked
            it[userAgent] = session.userAgent
            it[ipAddress] = session.ipAddress
        }

        toModel(insertedRow.resultedValues?.first()!!)
    }

    suspend fun findByTokenHash(refreshTokenHash: String): RefreshSessionModel? = newSuspendedTransaction {
        UserSessionTable
            .selectAll()
            .where { UserSessionTable.refreshTokenHash eq refreshTokenHash }
            .map { toModel(it) }
            .singleOrNull()
    }

    suspend fun revokeActiveById(sessionId: String): Boolean = newSuspendedTransaction {
        val uuid = runCatching { UUID.fromString(sessionId) }.getOrNull() ?: return@newSuspendedTransaction false

        val updatedCount = UserSessionTable.update({
            (UserSessionTable.id eq uuid) and (UserSessionTable.isRevoked eq false)
        }) {
            it[isRevoked] = true
        }

        updatedCount == 1
    }

    suspend fun revokeAllByUserId(userId: String): Unit = newSuspendedTransaction {
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return@newSuspendedTransaction

        UserSessionTable.update({
            (UserSessionTable.userId eq userUuid) and (UserSessionTable.isRevoked eq false)
        }) {
            it[isRevoked] = true
        }
    }
}