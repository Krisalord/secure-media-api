package io.github.krisalord.recommendation

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationRequest(
    val promptType: String,
    val customInput: String? = null
)

@Serializable
data class RecommendationItem(
    val title: String,
    val mediaType: String,
    val reason: String
)

@Serializable
data class RecommendationResponse(
    val promptType: String,
    val recommendations: List<RecommendationItem>,
    val generatedAt: String
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

enum class PromptType {
    DEFAULT,
    ACTION_PACKED,
    BINGE_WORTHY,
    CUSTOM
}