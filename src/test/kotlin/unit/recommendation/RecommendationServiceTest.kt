package io.github.krisalord.unit.recommendation

import io.github.krisalord.favorite_actors.FavoriteActorRepository
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.recommendation.RecommendationRequest
import io.github.krisalord.recommendation.RecommendationService
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecommendationServiceTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var favoriteActorRepository: FavoriteActorRepository
    private val fakeUserId = "user-123"
    private val fakeApiKey = "test-gemini-key"

    @BeforeEach
    fun setup() {
        mediaRepository = mockk()
        favoriteActorRepository = mockk()
    }

    private fun createServiceWithMockEngine(engine: MockEngine): RecommendationService {
        val mockClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        return RecommendationService(
            mediaRepository = mediaRepository,
            favoriteActorRepository = favoriteActorRepository,
            httpClient = mockClient,
            geminiApiKey = fakeApiKey
        )
    }

    @Test
    fun `getRecommendations - should successfully parse Gemini JSON response`() = runBlocking {
        val rawGeminiResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "[{\"title\": \"Inception\", \"mediaType\": \"MOVIE\", \"reason\": \"Because you like Sci-Fi.\"}]"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = rawGeminiResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val service = createServiceWithMockEngine(mockEngine)
        val request = RecommendationRequest(promptType = "DEFAULT")

        coEvery { mediaRepository.findAllByUserId(fakeUserId) } returns emptyList()
        coEvery { favoriteActorRepository.findAllByUserId(fakeUserId) } returns emptyList()

        val response = service.getRecommendations(fakeUserId, request)

        assertEquals(1, response.recommendations.size)
        assertEquals("Inception", response.recommendations.first().title)
        assertEquals("Because you like Sci-Fi.", response.recommendations.first().reason)
        assertEquals("DEFAULT", response.promptType)
    }

    @Test
    fun `getRecommendations - should throw exception when Gemini returns 500 error`() = runBlocking {
        val mockEngine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }

        val service = createServiceWithMockEngine(mockEngine)
        val request = RecommendationRequest(promptType = "ACTION_PACKED")

        coEvery { mediaRepository.findAllByUserId(fakeUserId) } returns emptyList()
        coEvery { favoriteActorRepository.findAllByUserId(fakeUserId) } returns emptyList()

        val exception = assertThrows<Exception> {
            service.getRecommendations(fakeUserId, request)
        }

        assertTrue(exception.message!!.contains("Gemini engine invocation failed with status code: 500"))
    }
}