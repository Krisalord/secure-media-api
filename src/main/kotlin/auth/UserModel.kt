package io.github.krisalord.auth

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class UserModel(
    @BsonId
    val id: ObjectId = ObjectId(),
    val email: String,
    val passwordHash: String,
    val role: String = "USER",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun create(
            sanitizedEmail: String,
            passwordHash: String
        ): UserModel {
            return UserModel(
                email = sanitizedEmail,
                passwordHash = passwordHash,
                role = "USER",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
    }
}