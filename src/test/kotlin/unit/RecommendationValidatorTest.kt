package io.github.krisalord.unit

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
    fun `validateRequest - should pass for CUSTOM prompt when valid custom input is provided`() {
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = "I want a cyberpunk anime.")
        assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
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

    @Test
    fun `validateRequest - should throw when CUSTOM prompt input exceeds 500 characters`() {
        val longInput = "A".repeat(501)
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = longInput)

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Custom input cannot exceed 500 characters.", exception.message)
    }

    @Test
    fun `validateRequest - should gracefully handle lowercase enum strings`() {
        val request = RecommendationRequest(promptType = "action_packed")
        assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
    }
}