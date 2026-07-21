package io.github.krisalord.unit.media

import io.github.krisalord.media.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class MediaServiceTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var mediaService: MediaService
    private val fakeUserId = "123e4567-e89b-12d3-a456-426614174000"

    private val mockEngine = MockEngine { request ->
        when (request.url.encodedPath) {
            "/3/search/movie" -> respond(
                content = """{"results": [{"poster_path": "/matrix_fake_poster.jpg"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
            "/3/search/tv" -> respond(
                content = """{"results": []}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
            else -> respondError(HttpStatusCode.NotFound)
        }
    }

    @BeforeEach
    fun setup() {
        mediaRepository = mockk()
        mediaService = MediaService(
            mediaRepository = mediaRepository,
            httpClient = HttpClient(mockEngine),
            tmdbApiKey = "fake_test_key"
        )
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
    fun `removeMedia - Valid ID - Returns True`() = runBlocking {
        val mediaId = "media-123"
        coEvery { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) } returns true

        val result = mediaService.removeMedia(mediaId, fakeUserId)

        assertTrue(result)
        coVerify { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) }
    }

    @Test
    fun `removeMedia - Invalid ID or Ownership - Throws Exception`() = runBlocking {
        val mediaId = "wrong-media-id"
        coEvery { mediaRepository.deleteByIdAndUserId(mediaId, fakeUserId) } returns false
        val exception = assertThrows(MediaNotFoundException::class.java) {
            runBlocking { mediaService.removeMedia(mediaId, fakeUserId) }
        }
        assertEquals("Media log entry not found or access denied.", exception.message)
    }

    @Test
    fun `getWatchHistory - Returns List of Media`() = runBlocking {
        val mockList = listOf(
            WatchedMediaModel("1", fakeUserId, "Movie A", "MOVIE", 4, Instant.now(), null),
            WatchedMediaModel("2", fakeUserId, "Movie B", "MOVIE", 5, Instant.now(), null)
        )
        coEvery { mediaRepository.findAllByUserId(fakeUserId) } returns mockList

        val result = mediaService.getWatchHistory(fakeUserId)

        assertEquals(2, result.size)
        assertEquals("Movie A", result[0].title)
    }
}