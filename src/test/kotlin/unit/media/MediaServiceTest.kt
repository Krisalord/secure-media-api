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
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaServiceTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var mediaService: MediaService
    private val fakeUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

    @BeforeEach
    fun setup() {
        mockkStatic("io.github.krisalord.core.database.DatabaseFactoryKt")

        coEvery { dbQuery<Any?>(any()) } coAnswers {
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
        unmockkAll()
    }

    @Test
    fun `logMedia - Valid Movie Request - Fetches TMDB Poster and Saves`() = runBlocking {
        val request = CreateMediaRequest(title = "The Matrix", mediaType = "MOVIE", rating = 5)
        val expectedModel = WatchedMediaModel(
            id = UUID.randomUUID(),
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
            id = UUID.randomUUID(),
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
        val mediaId = UUID.randomUUID()
        coEvery { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) } returns false

        val exception = assertThrows<MediaNotFoundException> {
            mediaService.removeMedia(mediaId, fakeUserId)
        }
        assertEquals("Media log entry not found or access denied.", exception.message)
    }

    @Test
    fun `removeMedia - Valid ID and Ownership - Returns true`() = runBlocking {
        val mediaId = UUID.randomUUID()
        coEvery { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) } returns true

        val result = mediaService.removeMedia(mediaId, fakeUserId)

        assertTrue(result)
        coVerify(exactly = 1) { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) }
    }

    @Test
    fun `getWatchHistory - should return list of media for user`() = runBlocking {
        val history = listOf(
            WatchedMediaModel(
                id = UUID.randomUUID(),
                userId = fakeUserId,
                title = "The Matrix",
                mediaType = "MOVIE",
                rating = 5,
                watchedAt = Instant.now(),
                posterUrl = "https://image.tmdb.org/t/p/w500/matrix.jpg"
            ),
            WatchedMediaModel(
                id = UUID.randomUUID(),
                userId = fakeUserId,
                title = "Breaking Bad",
                mediaType = "TV_SHOW",
                rating = 5,
                watchedAt = Instant.now(),
                posterUrl = null
            )
        )

        coEvery { mediaRepository.findAllByUserId(fakeUserId) } returns history

        val result = mediaService.getWatchHistory(fakeUserId)

        assertEquals(2, result.size)
        assertEquals("The Matrix", result[0].title)
        assertEquals("Breaking Bad", result[1].title)
        coVerify(exactly = 1) { mediaRepository.findAllByUserId(fakeUserId) }
    }

    @Test
    fun `getWatchHistory - should return empty list when no media exists`() = runBlocking {
        coEvery { mediaRepository.findAllByUserId(fakeUserId) } returns emptyList()

        val result = mediaService.getWatchHistory(fakeUserId)

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { mediaRepository.findAllByUserId(fakeUserId) }
    }

    @Test
    fun `logMedia - should return null posterUrl when TMDB API key is blank`() = runBlocking {
        val mediaServiceNoKey = MediaService(
            mediaRepository = mediaRepository,
            httpClient = MockHttpClients.createTmdbMockClient(),
            tmdbApiKey = ""
        )

        val request = CreateMediaRequest(title = "The Matrix", mediaType = "MOVIE", rating = 5)
        val expectedModel = WatchedMediaModel(
            id = UUID.randomUUID(),
            userId = fakeUserId,
            title = "The Matrix",
            mediaType = "MOVIE",
            rating = 5,
            watchedAt = Instant.now(),
            posterUrl = null
        )

        coEvery { mediaRepository.create(any()) } returns expectedModel

        val result = mediaServiceNoKey.logMedia(fakeUserId, request)

        assertNull(result.posterUrl)
        coVerify(exactly = 1) { mediaRepository.create(any()) }
    }

    @Test
    fun `logMedia - should throw InvalidMediaDataException for invalid request`() = runBlocking {
        val request = CreateMediaRequest(title = "", mediaType = "MOVIE", rating = 5)

        val exception = assertThrows<io.github.krisalord.media.InvalidMediaDataException> {
            mediaService.logMedia(fakeUserId, request)
        }

        assertEquals("Title cannot be blank.", exception.message)
    }

    @Test
    fun `logMedia - should trim and normalize whitespace in title`() = runBlocking {
        val request = CreateMediaRequest(title = "  The   Matrix  ", mediaType = "MOVIE", rating = 5)
        val expectedModel = WatchedMediaModel(
            id = UUID.randomUUID(),
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
    }
}