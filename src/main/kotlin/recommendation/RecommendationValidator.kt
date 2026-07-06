package io.github.krisalord.recommendation

object RecommendationValidator {
    fun validateRequest(request: RecommendationRequest) {
        val type = runCatching { PromptType.valueOf(request.promptType.uppercase()) }.getOrNull()
            ?: throw InvalidRecommendationRequestException("Invalid prompt type: ${request.promptType}")

        if (type == PromptType.CUSTOM) {
            if (request.customInput.isNullOrBlank()) {
                throw InvalidRecommendationRequestException("Custom input cannot be blank when prompt type is CUSTOM.")
            }
            if (request.customInput.length > 500) {
                throw InvalidRecommendationRequestException("Custom input cannot exceed 500 characters.")
            }
        }
    }
}