package io.github.krisalord.unit.recommendation

import io.github.krisalord.recommendation.InvalidRecommendationRequestException
import io.github.krisalord.recommendation.RecommendationRequest
import io.github.krisalord.recommendation.RecommendationValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class RecommendationValidatorTest {

    @Test
    fun `validateRequest - should pass for all valid standard enum prompt types`() {
        val validPrompts = listOf("DEFAULT", "ACTION_PACKED", "BINGE_WORTHY")

        validPrompts.forEach { prompt ->
            val request = RecommendationRequest(promptType = prompt)
            assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
        }
    }

    @Test
    fun `validateRequest - should throw when promptType cannot be mapped to PromptType enum`() {
        val request = RecommendationRequest(promptType = "ROMANTIC_COMEDY")

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Invalid prompt type: ROMANTIC_COMEDY", exception.message)
    }

    @Test
    fun `validateRequest - should throw when CUSTOM prompt is missing custom input`() {
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = "   ")

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Custom input cannot be blank when prompt type is CUSTOM.", exception.message)
    }
}