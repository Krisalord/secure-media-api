package io.github.krisalord.recommendation

import io.github.krisalord.favorite_actors.FavoriteActorRepository
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.WatchedMediaModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.Locale

class RecommendationService(
    private val mediaRepository: MediaRepository,
    private val favoriteActorRepository: FavoriteActorRepository,
    private val httpClient: HttpClient,
    private val geminiApiKey: String
) {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getRecommendations(userId: String, request: RecommendationRequest): RecommendationResponse {
        RecommendationValidator.validateRequest(request)
        val promptType = PromptType.valueOf(request.promptType.uppercase(Locale.getDefault()))

        val watchHistory = mediaRepository.findAllByUserId(userId)
        val favoriteActors = favoriteActorRepository.findAllByUserId(userId).map { it.name }

        if (promptType == PromptType.FAVORITE_ACTORS && favoriteActors.isEmpty()) {
            throw IllegalArgumentException("You must add at least one favorite actor before using this prompt type.")
        }

        val aiRawOutput = fetchFromGemini(watchHistory, favoriteActors, promptType, request.customInput)

        val validatedItems = runCatching {
            jsonParser.decodeFromString<List<RecommendationItem>>(aiRawOutput)
        }.getOrElse {
            throw AiProviderException("AI provider generated incompatible structural output. Try again.")
        }

        return RecommendationResponse(
            promptType = promptType.name,
            recommendations = validatedItems,
            generatedAt = Instant.now().toString()
        )
    }

    private suspend fun fetchFromGemini(
        history: List<WatchedMediaModel>,
        favoriteActors: List<String>,
        type: PromptType,
        customInput: String?
    ): String {
        val serializedHistory = history.joinToString("\n") {
            "- Title: ${it.title}, Type: ${it.mediaType}, Rating: ${it.rating}/5"
        }

        val systemInstruction = """
            Analyze this watch history:
            $serializedHistory
            
            Generate 3 recommendations. Return your output as a strict JSON array matching this exact model format without markdown blocks:
            [{"title": "Name", "mediaType": "MOVIE", "reason": "Explanation"}]
        """.trimIndent()

        val actionModifier = when (type) {
            PromptType.DEFAULT -> "Suggest 3 diverse movies or shows complementing history parameters."
            PromptType.ACTION_PACKED -> "Suggest 3 entries strictly within Action, Thriller, or Sci-Fi genres."
            PromptType.BINGE_WORTHY -> "Suggest 3 serial television shows with deep serialization."
            PromptType.CUSTOM -> "Satisfy this user constraint: '$customInput'."
            PromptType.FAVORITE_ACTORS -> {
                val actorsList = favoriteActors.joinToString(", ")
                "Suggest 3 new movies or shows featuring these specific actors: $actorsList. CRITICAL REQUIREMENT: You must cross-reference their filmography and DO NOT suggest anything that is already listed in the user's watch history above."
            }
        }

        val fullPrompt = "$systemInstruction\nDirective: $actionModifier"

        val response = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$geminiApiKey") {
            contentType(ContentType.Application.Json)
            setBody(
                GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))),
                    generationConfig = GeminiGenerationConfig()
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw AiProviderException("Gemini engine invocation failed with status code: ${response.status}")
        }

        val geminiBody = response.body<GeminiResponse>()
        return geminiBody.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw AiProviderException("Empty completion returned from Gemini provider.")
    }
}