package io.github.krisalord.services

import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.security.AiRateLimiter
import io.ktor.server.plugins.*

class AiService(
    private val mongoMediaRepository: MediaRepository,
    private val openAiClient: AiClient?,
    private val aiRateLimiter: AiRateLimiter
) {
    private suspend fun fetchAndFormatWatchList(userId: String): String {
        aiRateLimiter.check(userId)

        val watchList = mongoMediaRepository.getAllMediaByUserId(userId)
        if (watchList.isEmpty()) throw NotFoundException("No media found to summarize")

        return watchList.joinToString("\n") { media ->
            "- ${media.title} | Genres: ${media.genres.joinToString(", ")} | Rating: ${media.rating} | Status: ${media.status}"
        }.take(6000)
    }

    suspend fun createSuggestion(userId: String): String {
        val client = openAiClient
            ?: throw NotFoundException("AI summary service not available (OpenAI API key is missing)")

        val combinedWatchList = fetchAndFormatWatchList(userId)

        val prompt = """
        Do NOT include user IDs or any sensitive info.
        Suggest new movies or shows for the user based on their library:
        $combinedWatchList
    """.trimIndent()

        return client.createSuggestion(prompt)
    }
}