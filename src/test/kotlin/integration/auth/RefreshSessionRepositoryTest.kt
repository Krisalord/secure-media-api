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
import kotlin.test.assertNull
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
                    id = UUID.randomUUID(),
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

            val session1 = refreshSessionRepository.findByTokenHash("hash_1")
            val session2 = refreshSessionRepository.findByTokenHash("hash_2")
            val session3 = refreshSessionRepository.findByTokenHash("hash_3")

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
                id = UUID.randomUUID(),
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

    @Test
    fun `create - should insert a new session and map it back correctly`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()
            val sessionToCreate = RefreshSessionModel(
                id = UUID.randomUUID(),
                userId = user.id,
                refreshTokenHash = "hash_create_test",
                expiresAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now(),
                isRevoked = false,
                userAgent = "TestAgent",
                ipAddress = "192.168.1.1"
            )

            val createdSession = refreshSessionRepository.create(sessionToCreate)

            assertNotNull(createdSession.id)
            assertEquals(user.id, createdSession.userId)
            assertEquals("hash_create_test", createdSession.refreshTokenHash)
            assertEquals("TestAgent", createdSession.userAgent)
            assertEquals("192.168.1.1", createdSession.ipAddress)
            assertEquals(false, createdSession.isRevoked)
        }
    }

    @Test
    fun `findByTokenHash - should return the correct session if hash exists`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()
            val session = RefreshSessionModel(
                id = UUID.randomUUID(),
                userId = user.id,
                refreshTokenHash = "hash_find_test",
                expiresAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now(),
                isRevoked = false,
                userAgent = null,
                ipAddress = null
            )
            refreshSessionRepository.create(session)

            val foundSession = refreshSessionRepository.findByTokenHash("hash_find_test")

            assertNotNull(foundSession)
            assertEquals(user.id, foundSession.userId)
        }
    }

    @Test
    fun `findByTokenHash - should return null if hash does not exist`() = runSecureTestApplication {
        runDbTest {
            val foundSession = refreshSessionRepository.findByTokenHash("nonexistent_hash")

            assertNull(foundSession)
        }
    }

    @Test
    fun `revokeActiveById - should return true and revoke an active session`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()
            val sessionId = UUID.randomUUID()
            val session = RefreshSessionModel(
                id = sessionId,
                userId = user.id,
                refreshTokenHash = "hash_revoke_test",
                expiresAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now(),
                isRevoked = false,
                userAgent = null,
                ipAddress = null
            )
            refreshSessionRepository.create(session)

            val revoked = refreshSessionRepository.revokeActiveById(sessionId)

            assertTrue(revoked)

            val updatedSession = refreshSessionRepository.findByTokenHash("hash_revoke_test")
            assertNotNull(updatedSession)
            assertTrue(updatedSession.isRevoked)
        }
    }

    @Test
    fun `revokeActiveById - should return false when session is already revoked`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()
            val sessionId = UUID.randomUUID()
            val session = RefreshSessionModel(
                id = sessionId,
                userId = user.id,
                refreshTokenHash = "hash_already_revoked",
                expiresAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now(),
                isRevoked = true,
                userAgent = null,
                ipAddress = null
            )
            refreshSessionRepository.create(session)

            val revoked = refreshSessionRepository.revokeActiveById(sessionId)

            assertEquals(false, revoked)
        }
    }

    @Test
    fun `revokeActiveById - should return false when session id does not exist`() = runSecureTestApplication {
        runDbTest {
            val nonexistentId = UUID.randomUUID()

            val revoked = refreshSessionRepository.revokeActiveById(nonexistentId)

            assertEquals(false, revoked)
        }
    }

    @Test
    fun `enforceSessionLimit - should not revoke sessions when under limit`() = runSecureTestApplication {
        runDbTest {
            val user = createDummyUser()

            for (i in 1..2) {
                val session = RefreshSessionModel(
                    id = UUID.randomUUID(),
                    userId = user.id,
                    refreshTokenHash = "hash_under_limit_$i",
                    expiresAt = Instant.now().plusSeconds(3600),
                    createdAt = Instant.now().minusSeconds((100 - i).toLong()),
                    isRevoked = false,
                    userAgent = "Device $i",
                    ipAddress = "127.0.0.1"
                )
                refreshSessionRepository.create(session)
            }

            refreshSessionRepository.enforceSessionLimit(user.id, maxSessions = 5)

            val session1 = refreshSessionRepository.findByTokenHash("hash_under_limit_1")
            val session2 = refreshSessionRepository.findByTokenHash("hash_under_limit_2")

            assertEquals(false, session1!!.isRevoked)
            assertEquals(false, session2!!.isRevoked)
        }
    }
}