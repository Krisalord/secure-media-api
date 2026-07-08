package io.github.krisalord.unit

import io.github.krisalord.media.CreateMediaRequest
import io.github.krisalord.media.InvalidMediaDataException
import io.github.krisalord.media.MediaValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class MediaValidatorTest {

    @Test
    fun `validateLogRequest - should pass on valid request`() {
        val request = CreateMediaRequest("Inception", "MOVIE", 5)
        assertDoesNotThrow { MediaValidator.validateLogRequest(request) }
    }

    @Test
    fun `validateLogRequest - should throw exception when title is blank`() {
        val request = CreateMediaRequest("   ", "MOVIE", 5)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Title cannot be blank.", exception.message)
    }

    @Test
    fun `validateLogRequest - should throw exception on invalid media type`() {
        val request = CreateMediaRequest("Naruto", "ANIME", 5)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Media type must be either MOVIE or TV_SHOW.", exception.message)
    }

    @Test
    fun `validateLogRequest - should throw exception when rating is out of bounds`() {
        val request = CreateMediaRequest("The Room", "MOVIE", 0)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Rating must be an integer between 1 and 5.", exception.message)
    }
}