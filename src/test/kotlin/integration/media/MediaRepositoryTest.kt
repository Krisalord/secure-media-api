package io.github.krisalord.integration.media

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.UserModel
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.runDbTest
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.WatchedMediaModel
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
            val maliciousDelete = mediaRepository.deleteByIdAndUserId(mediaA.id!!, userB.id)
            assertEquals(false, maliciousDelete)

            // User A deletes their own movie
            val validDelete = mediaRepository.deleteByIdAndUserId(mediaA.id, userA.id)
            assertTrue(validDelete)
        }
    }

    @Test
    fun `findAllByUserId - should return empty list when user has no media`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("empty_media@example.com", "hash"))

            val results = mediaRepository.findAllByUserId(user.id)

            assertTrue(results.isEmpty())
        }
    }

    @Test
    fun `deleteByIdAndUserId - should return false when media ID does not exist`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("delete_nonexistent@example.com", "hash"))
            val nonexistentId = java.util.UUID.randomUUID()

            val deleted = mediaRepository.deleteByIdAndUserId(nonexistentId, user.id)

            assertEquals(false, deleted)
        }
    }

    @Test
    fun `create - should handle null posterUrl correctly`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("null_poster@example.com", "hash"))
            val media = WatchedMediaModel.create(user.id, "No Poster Movie", "MOVIE", 3, null)

            val created = mediaRepository.create(media)

            assertNotNull(created.id)
            assertEquals("No Poster Movie", created.title)
            assertNull(created.posterUrl)
        }
    }

    @Test
    fun `create - should handle posterUrl correctly`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("with_poster@example.com", "hash"))
            val posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg"
            val media = WatchedMediaModel.create(user.id, "With Poster Movie", "TV_SHOW", 4, posterUrl)

            val created = mediaRepository.create(media)

            assertNotNull(created.id)
            assertEquals(posterUrl, created.posterUrl)
        }
    }

    @Test
    fun `findAllByUserId - should return multiple media entries`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("multi_media@example.com", "hash"))

            mediaRepository.create(WatchedMediaModel.create(user.id, "Movie 1", "MOVIE", 5, null))
            mediaRepository.create(WatchedMediaModel.create(user.id, "Movie 2", "MOVIE", 4, "http://poster2.jpg"))
            mediaRepository.create(WatchedMediaModel.create(user.id, "Show 1", "TV_SHOW", 3, null))

            val results = mediaRepository.findAllByUserId(user.id)

            assertEquals(3, results.size)
        }
    }

    @Test
    fun `findAllByUserId - should not return media from other users`() = runSecureTestApplication {
        runDbTest {
            val userA = authRepository.create(UserModel.create("isolation_a@example.com", "hash"))
            val userB = authRepository.create(UserModel.create("isolation_b@example.com", "hash"))

            mediaRepository.create(WatchedMediaModel.create(userA.id, "User A Movie", "MOVIE", 5, null))
            mediaRepository.create(WatchedMediaModel.create(userB.id, "User B Movie", "MOVIE", 4, null))

            val resultsA = mediaRepository.findAllByUserId(userA.id)
            val resultsB = mediaRepository.findAllByUserId(userB.id)

            assertEquals(1, resultsA.size)
            assertEquals("User A Movie", resultsA.first().title)
            assertEquals(1, resultsB.size)
            assertEquals("User B Movie", resultsB.first().title)
        }
    }
}