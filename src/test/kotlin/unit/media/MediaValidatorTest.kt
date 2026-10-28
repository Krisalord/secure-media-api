package io.github.krisalord.unit.media

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

    @Test
    fun `validateLogRequest - should throw exception when rating is above 5`() {
        val request = CreateMediaRequest("The Room", "MOVIE", 6)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Rating must be an integer between 1 and 5.", exception.message)
    }

    @Test
    fun `validateLogRequest - should throw exception when title exceeds 255 characters`() {
        val longTitle = "a".repeat(256)
        val request = CreateMediaRequest(longTitle, "MOVIE", 5)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Title length cannot exceed 255 characters.", exception.message)
    }

    @Test
    fun `validateLogRequest - should pass when title is exactly 255 characters`() {
        val exactTitle = "a".repeat(255)
        val request = CreateMediaRequest(exactTitle, "MOVIE", 5)
        assertDoesNotThrow { MediaValidator.validateLogRequest(request) }
    }

    @Test
    fun `validateLogRequest - should pass with lowercase media type`() {
        val request = CreateMediaRequest("Inception", "movie", 5)
        assertDoesNotThrow { MediaValidator.validateLogRequest(request) }
    }

    @Test
    fun `validateLogRequest - should pass with TV_SHOW type`() {
        val request = CreateMediaRequest("Breaking Bad", "TV_SHOW", 4)
        assertDoesNotThrow { MediaValidator.validateLogRequest(request) }
    }

    @Test
    fun `validateLogRequest - should pass with rating at lower boundary 1`() {
        val request = CreateMediaRequest("Inception", "MOVIE", 1)
        assertDoesNotThrow { MediaValidator.validateLogRequest(request) }
    }

    @Test
    fun `validateLogRequest - should throw when title is empty string`() {
        val request = CreateMediaRequest("", "MOVIE", 5)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Title cannot be blank.", exception.message)
    }

    @Test
    fun `validateLogRequest - should throw when rating is negative`() {
        val request = CreateMediaRequest("Inception", "MOVIE", -1)
        val exception = assertThrows<InvalidMediaDataException> {
            MediaValidator.validateLogRequest(request)
        }
        assertEquals("Rating must be an integer between 1 and 5.", exception.message)
    }
}