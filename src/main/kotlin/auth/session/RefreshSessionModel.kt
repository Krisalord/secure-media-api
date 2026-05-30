package io.github.krisalord.auth.session

import io.github.krisalord.auth.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object UserSessionTable : UUIDTable("user_session") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val refreshTokenHash = varchar("refresh_token_hash", 255).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val userAgent = varchar("user_agent", 512).nullable()
    val ipAddress = varchar("ip_address", 45).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

data class RefreshSessionModel(
    val id: String,
    val userId: String,
    val refreshTokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val isRevoked: Boolean,
    val userAgent: String?,
    val ipAddress: String?
)