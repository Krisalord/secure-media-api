package concurrency

import io.github.krisalord.model.media.Genre
import io.github.krisalord.model.media.MediaModel
import io.github.krisalord.model.media.WatchStatus
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.security.AiRateLimiter
import io.github.krisalord.services.AiClient
import io.github.krisalord.services.AiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiServiceConcurrentRequestTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var aiClient: AiClient
    private lateinit var aiRateLimiter: AiRateLimiter
    private lateinit var aiService: AiService

    @BeforeAll
    fun setup() {
        mediaRepository = mockk()
        aiClient = mockk()
        aiRateLimiter = AiRateLimiter(maxRequests = 10, windowSeconds = 60)

        aiService = AiService(mediaRepository, aiClient, aiRateLimiter)
    }

    @Test
    fun `two concurrent AI requests do not break service`() = runBlocking {
        val userId = ObjectId().toHexString()
        val results = Collections.synchronizedList(mutableListOf<String>())

        coEvery { mediaRepository.getAllMediaByUserId(userId) } returns listOf(
            MediaModel(
                id = ObjectId(),
                userId = userId,
                title = "Movie A",
                genres = listOf(Genre.ACTION),
                rating = 8,
                status = WatchStatus.COMPLETED
            ),
            MediaModel(
                id = ObjectId(),
                userId = userId,
                title = "Movie B",
                genres = listOf(Genre.DRAMA),
                rating = 7,
                status = WatchStatus.WATCHING
            )
        )

        coEvery { aiClient.createSuggestion(any()) } returns "Suggested: Movie C"

        coroutineScope {
            repeat(22) {
                launch {
                    try {
                        val suggestion = aiService.createSuggestion(userId)
                        results.add(suggestion)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        results.add("ERROR")
                    }
                }
            }
        }

        val successCount = results.count { it != "ERROR" }
        println("Successful AI requests: $successCount, Results: $results")

        assertEquals(2, successCount, "Both concurrent AI requests should succeed")
    }
}