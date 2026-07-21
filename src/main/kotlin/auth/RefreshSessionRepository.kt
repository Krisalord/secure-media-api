package io.github.krisalord.auth

import io.github.krisalord.core.exceptions.DatabaseException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

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

    fun create(session: RefreshSessionModel): RefreshSessionModel {
        val insertedRow = UserSessionTable.insert {
            it[id] = UUID.fromString(session.id.ifEmpty { UUID.randomUUID().toString() })
            it[userId] = UUID.fromString(session.userId)
            it[refreshTokenHash] = session.refreshTokenHash
            it[expiresAt] = session.expiresAt
            it[createdAt] = session.createdAt
            it[isRevoked] = session.isRevoked
            it[userAgent] = session.userAgent
            it[ipAddress] = session.ipAddress
        }

        val row = insertedRow.resultedValues?.firstOrNull()
            ?: throw DatabaseException("Failed to insert refresh session.")

        return toModel(row)
    }

    fun findByTokenHash(refreshTokenHash: String): RefreshSessionModel? {
        return UserSessionTable
            .selectAll()
            .where { UserSessionTable.refreshTokenHash eq refreshTokenHash }
            .map { toModel(it) }
            .singleOrNull()
    }

    fun revokeActiveById(sessionId: String): Boolean {
        val uuid = runCatching { UUID.fromString(sessionId) }.getOrNull() ?: return false

        val updatedCount = UserSessionTable.update({
            (UserSessionTable.id eq uuid) and (UserSessionTable.isRevoked eq false)
        }) {
            it[isRevoked] = true
        }

        return updatedCount == 1
    }

    fun revokeAllByUserId(userId: String) {
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return

        UserSessionTable.update({
            (UserSessionTable.userId eq userUuid) and (UserSessionTable.isRevoked eq false)
        }) {
            it[isRevoked] = true
        }
    }

    fun enforceSessionLimit(userId: String, maxSessions: Int) {
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return

        val activeSessionIds = UserSessionTable
            .selectAll()
            .where { (UserSessionTable.userId eq userUuid) and (UserSessionTable.isRevoked eq false) }
            .orderBy(UserSessionTable.createdAt to SortOrder.DESC)
            .map { it[UserSessionTable.id].value }

        if (activeSessionIds.size > maxSessions) {
            val sessionsToRevoke = activeSessionIds.drop(maxSessions)

            if (sessionsToRevoke.isNotEmpty()) {
                UserSessionTable.update({ UserSessionTable.id inList sessionsToRevoke }) {
                    it[isRevoked] = true
                }
            }
        }
    }
}