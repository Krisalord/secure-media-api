package io.github.krisalord.auth


import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import java.time.Instant


object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex("user_email_unique")
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50).default("USER")


    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
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
                id = "",
                email = sanitizedEmail,
                passwordHash = passwordHash,
                role = "USER",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
    }
}