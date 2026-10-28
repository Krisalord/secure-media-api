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

    @Test
    fun `validateRequest - should throw when CUSTOM prompt has null custom input`() {
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = null)

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Custom input cannot be blank when prompt type is CUSTOM.", exception.message)
    }

    @Test
    fun `validateRequest - should throw when CUSTOM input exceeds 500 characters`() {
        val longInput = "a".repeat(501)
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = longInput)

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Custom input cannot exceed 500 characters.", exception.message)
    }

    @Test
    fun `validateRequest - should pass when CUSTOM input is exactly 500 characters`() {
        val exactInput = "a".repeat(500)
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = exactInput)

        assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
    }

    @Test
    fun `validateRequest - should pass for lowercase prompt types`() {
        val lowercasePrompts = listOf("default", "action_packed", "binge_worthy", "custom")

        lowercasePrompts.forEach { prompt ->
            if (prompt == "custom") {
                val request = RecommendationRequest(promptType = prompt, customInput = "Sci-fi movies")
                assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
            } else {
                val request = RecommendationRequest(promptType = prompt)
                assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
            }
        }
    }

    @Test
    fun `validateRequest - should pass for CUSTOM with valid input`() {
        val request = RecommendationRequest(promptType = "CUSTOM", customInput = "I want sci-fi movies")

        assertDoesNotThrow { RecommendationValidator.validateRequest(request) }
    }

    @Test
    fun `validateRequest - should throw when promptType is empty string`() {
        val request = RecommendationRequest(promptType = "")

        val exception = assertThrows<InvalidRecommendationRequestException> {
            RecommendationValidator.validateRequest(request)
        }
        assertEquals("Invalid prompt type: ", exception.message)
    }
}