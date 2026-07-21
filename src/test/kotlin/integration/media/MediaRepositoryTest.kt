package io.github.krisalord.integration.media

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.UserModel
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.runDbTest
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.WatchedMediaModel
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaRepositoryTest : BaseIntegrationTest() {

    private val authRepository = AuthRepository()
    private val mediaRepository = MediaRepository()

    @Test
    fun `create and findAllByUserId - should insert and retrieve media successfully`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("media_repo@example.com", "hash"))
            val media = WatchedMediaModel.create(user.id, "The Matrix", "MOVIE", 5, "http://poster.url")

            mediaRepository.create(media)
            val results = mediaRepository.findAllByUserId(user.id)

            assertEquals(1, results.size)
            assertEquals("The Matrix", results.first().title)
            assertEquals(5, results.first().rating)
        }
    }

    @Test
    fun `deleteByIdAndUserId - should strictly delete only if both ID and UserID match`() = runSecureTestApplication {
        runDbTest {
            val userA = authRepository.create(UserModel.create("userA@example.com", "hash"))
            val userB = authRepository.create(UserModel.create("userB@example.com", "hash"))

            val mediaA = mediaRepository.create(WatchedMediaModel.create(userA.id, "Movie A", "MOVIE", 5, null))

            // User B attempts to delete User A's movie
            val maliciousDelete = mediaRepository.deleteByIdAndUserId(mediaA.id, userB.id)
            assertEquals(false, maliciousDelete)

            // User A deletes their own movie
            val validDelete = mediaRepository.deleteByIdAndUserId(mediaA.id, userA.id)
            assertTrue(validDelete)
        }
    }
}