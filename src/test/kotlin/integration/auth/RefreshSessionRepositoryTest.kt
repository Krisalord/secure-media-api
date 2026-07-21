package io.github.krisalord.integration.auth

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.RefreshSessionModel
import io.github.krisalord.auth.RefreshSessionRepository
import io.github.krisalord.auth.UserModel
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.runDbTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RefreshSessionRepositoryTest : BaseIntegrationTest() {

    private val authRepository = AuthRepository()
    private val refreshSessionRepository = RefreshSessionRepository()

    private fun createDummyUser(): UserModel {
        val user = UserModel.create("session_test_${UUID.randomUUID()}@example.com", "hash")
        return authRepository.create(user)
    }

    @Test
    fun `enforceSessionLimit - should revoke the oldest session when limit is exceeded`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()

            for (i in 1..3) {
                val session = RefreshSessionModel(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    refreshTokenHash = "hash_$i",
                    expiresAt = Instant.now().plusSeconds(3600),
                    createdAt = Instant.now().minusSeconds((100 - i).toLong()),
                    isRevoked = false,
                    userAgent = "Device $i",
                    ipAddress = "127.0.0.1"
                )
                refreshSessionRepository.create(session)
            }

            refreshSessionRepository.enforceSessionLimit(user.id, maxSessions = 2)

            val session1 = refreshSessionRepository.findByTokenHash("hash_1") // The oldest
            val session2 = refreshSessionRepository.findByTokenHash("hash_2")
            val session3 = refreshSessionRepository.findByTokenHash("hash_3") // The newest

            assertTrue(session1!!.isRevoked)
            assertEquals(false, session2!!.isRevoked)
            assertEquals(false, session3!!.isRevoked)
        }
    }

    @Test
    fun `revokeAllByUserId - should revoke all active sessions for a user`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()
            val session = RefreshSessionModel(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                refreshTokenHash = "hash_revoke_all",
                expiresAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now(),
                isRevoked = false,
                userAgent = "Device",
                ipAddress = "127.0.0.1"
            )
            refreshSessionRepository.create(session)

            refreshSessionRepository.revokeAllByUserId(user.id)

            val updatedSession = refreshSessionRepository.findByTokenHash("hash_revoke_all")
            assertNotNull(updatedSession)
            assertTrue(updatedSession.isRevoked)
        }
    }
}