package io.github.krisalord.auth

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant
import java.util.UUID

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex("user_email_unique")
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50).default("USER")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object UserSessionTable : UUIDTable("user_session") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val refreshTokenHash = varchar("refresh_token_hash", 255).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val userAgent = varchar("user_agent", 512).nullable()
    val ipAddress = varchar("ip_address", 45).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}


data class UserModel(
    val id: String,
    val email: String,
    val passwordHash: String,
    val role: String = "USER",
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            sanitizedEmail: String,
            passwordHash: String
        ): UserModel {
            return UserModel(
                id = UUID.randomUUID().toString(),
                email = sanitizedEmail,
                passwordHash = passwordHash,
                role = "USER",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
    }
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

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthTokenResponse(
    val accessToken: String
)
