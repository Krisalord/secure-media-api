package io.github.krisalord.auth.session

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class RefreshSessionModel(
    @BsonId
    val id: ObjectId = ObjectId(),
    val userId: String,
    val refreshTokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
    val revokedAt: Instant? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null,
)