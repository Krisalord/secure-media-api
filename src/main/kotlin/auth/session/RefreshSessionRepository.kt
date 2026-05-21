package io.github.krisalord.auth.session

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant
import java.util.concurrent.TimeUnit

class RefreshSessionRepository(
    private val refreshSessions: CoroutineCollection<RefreshSessionModel>
) {
    suspend fun ensureIndexes() {
        refreshSessions.createIndex(
            Indexes.ascending("refreshTokenHash"),
            IndexOptions()
                .unique(true)
                .name("refresh_token_hash_unique")
        )

        refreshSessions.createIndex(
            Indexes.ascending("userId", "revokedAt", "expiresAt"),
            IndexOptions()
                .name("refresh_sessions_user_active_lookup_comp_idx")
        )

        refreshSessions.createIndex(
            Indexes.ascending("expiresAt"),
            IndexOptions()
                .expireAfter(0, TimeUnit.DAYS)
                .name("refresh_sessions_expire_at_ttl")
        )
    }


    suspend fun create(session: RefreshSessionModel): RefreshSessionModel {
        refreshSessions.insertOne(session)
        return session
    }

    suspend fun findByTokenHash(refreshTokenHash: String): RefreshSessionModel? =
        refreshSessions.findOne(
            RefreshSessionModel::refreshTokenHash eq refreshTokenHash
        )

    suspend fun revokeActiveById(sessionId: String): Boolean {
        if (!ObjectId.isValid(sessionId))
            return false

        val result = refreshSessions.updateOne(
            and(
                RefreshSessionModel::id eq ObjectId(sessionId),
                RefreshSessionModel::revokedAt eq null,
            ),
            setValue(RefreshSessionModel::revokedAt, Instant.now())
        )

        return result.modifiedCount == 1L
    }

    suspend fun revokeAllByUserId(userId: String) {
        refreshSessions.updateMany(
            and(
                RefreshSessionModel::userId eq userId,
                RefreshSessionModel::revokedAt eq null,
            ),
            setValue(RefreshSessionModel::revokedAt, Instant.now())
        )
    }
}