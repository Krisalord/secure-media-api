package io.github.krisalord.unit.media

import io.github.krisalord.core.database.dbQuery
import io.github.krisalord.media.CreateMediaRequest
import io.github.krisalord.media.MediaNotFoundException
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.MediaService
import io.github.krisalord.media.WatchedMediaModel
import io.github.krisalord.unit.core.MockHttpClients
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaServiceTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var mediaService: MediaService
    private val fakeUserId = "123e4567-e89b-12d3-a456-426614174000"

    @BeforeEach
    fun setup() {
        // 1. Mock the top-level file where dbQuery lives
        mockkStatic("io.github.krisalord.core.database.DatabaseFactoryKt")

        // 2. Intercept dbQuery and instruct it to instantly execute its lambda block
        coEvery { dbQuery<Any?>(any()) } coAnswers {
            // REMOVED 'suspend' here so it properly matches your synchronous repository calls!
            val block = firstArg<() -> Any?>()
            block.invoke()
        }

        mediaRepository = mockk()
        mediaService = MediaService(
            mediaRepository = mediaRepository,
            httpClient = MockHttpClients.createTmdbMockClient(),
            tmdbApiKey = "fake_test_key"
        )
    }

    @AfterEach
    fun tearDown() {
        // 3. Always unmock static functions after each test to prevent test pollution
        unmockkAll()
    }

    @Test
    fun `logMedia - Valid Movie Request - Fetches TMDB Poster and Saves`() = runBlocking {
        val request = CreateMediaRequest(title = "The Matrix", mediaType = "MOVIE", rating = 5)
        val expectedModel = WatchedMediaModel(
            id = "media-123",
            userId = fakeUserId,
            title = "The Matrix",
            mediaType = "MOVIE",
            rating = 5,
            watchedAt = Instant.now(),
            posterUrl = "https://image.tmdb.org/t/p/w500/matrix_fake_poster.jpg"
        )

        coEvery { mediaRepository.create(any()) } returns expectedModel

        val result = mediaService.logMedia(fakeUserId, request)

        assertEquals("The Matrix", result.title)
        assertEquals("https://image.tmdb.org/t/p/w500/matrix_fake_poster.jpg", result.posterUrl)
        coVerify(exactly = 1) { mediaRepository.create(any()) }
    }

    @Test
    fun `logMedia - TMDB Returns Empty - Saves Without Poster`() = runBlocking {
        val request = CreateMediaRequest(title = "Unknown TV Show", mediaType = "TV_SHOW", rating = 3)
        val expectedModel = WatchedMediaModel(
            id = "media-456",
            userId = fakeUserId,
            title = "Unknown TV Show",
            mediaType = "TV_SHOW",
            rating = 3,
            watchedAt = Instant.now(),
            posterUrl = null
        )

        coEvery { mediaRepository.create(any()) } returns expectedModel

        val result = mediaService.logMedia(fakeUserId, request)

        assertNull(result.posterUrl)
        coVerify(exactly = 1) { mediaRepository.create(any()) }
    }

    @Test
    fun `removeMedia - Invalid ID or Ownership - Throws Exception`() = runBlocking {
        val mediaId = "wrong-media-id"
        coEvery { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) } returns false

        val exception = assertThrows<MediaNotFoundException> {
            mediaService.removeMedia(mediaId, fakeUserId)
        }
        assertEquals("Media log entry not found or access denied.", exception.message)
    }
}