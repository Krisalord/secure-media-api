package io.github.krisalord.unit

import io.github.krisalord.media.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MediaServiceTest {

    private val mediaRepository = mockk<MediaRepository>()
    private val mediaService = MediaService(mediaRepository)

    @Test
    fun `logMedia - should successfully sanitize title and map request to model`() = runBlocking {
        val request = CreateMediaRequest("  The   Matrix  ", "MOVIE", 5)
        val userId = "user-123"

        coEvery { mediaRepository.create(any()) } answers { firstArg() }

        val result = mediaService.logMedia(userId, request)

        assertEquals("The Matrix", result.title)
        assertEquals("MOVIE", result.mediaType)
        assertEquals(5, result.rating)
        coVerify(exactly = 1) { mediaRepository.create(any()) }
    }

    @Test
    fun `removeMedia - should throw MediaNotFoundException if media does not exist`() = runBlocking {
        coEvery { mediaRepository.deleteByIdAndUserId(any(), any()) } returns false

        val exception = assertThrows<MediaNotFoundException> {
            mediaService.removeMedia("media-123", "user-123")
        }

        assertEquals("Media log entry not found or access denied.", exception.message)
        coVerify(exactly = 1) { mediaRepository.deleteByIdAndUserId("media-123", "user-123") }
    }
}