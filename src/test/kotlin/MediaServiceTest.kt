package io.github.krisalord

import io.github.krisalord.model.media.*
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.services.MediaService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.time.LocalDateTime

class MediaServiceTest {

    private val mediaRepository = mockk<MediaRepository>()
    private val mediaService = MediaService(mediaRepository)

    private val userId = "user123"
    private val mediaId = ObjectId().toHexString()

    private val mediaRequest = MediaRequest(
        title = "New title",
        genres = listOf(Genre.ACTION, Genre.ACTION),
        rating = 8,
        status = WatchStatus.COMPLETED
    )

    private val mediaModel = MediaModel(
        id = ObjectId(),
        userId = userId,
        title = "New title",
        genres = listOf(Genre.ACTION, Genre.ACTION),
        rating = 8,
        status = WatchStatus.COMPLETED,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Test
    fun `createMedia calls repository and returns media`() {
        every { mediaRepository.addMedia(any()) } returns mediaModel

        val result = mediaService.createMedia(userId, mediaRequest)

        assertEquals(mediaModel, result)
        verify { mediaRepository.addMedia(any()) }
    }

    @Test
    fun `getMediaByUserId returns list of media`() {
        every { mediaRepository.getAllMediaByUserId(userId) } returns listOf(mediaModel)

        val result = mediaService.getMediaByUserId(userId)

        assertEquals(1, result.size)
        assertEquals(mediaModel, result.first())
    }

    @Test
    fun `getMediaByMediaId returns media if found`() {
        every { mediaRepository.getMediaByMediaId(any(), userId) } returns mediaModel

        val result = mediaService.getMediaByMediaId(userId, mediaId)

        assertEquals(mediaModel, result)
        verify { mediaRepository.getMediaByMediaId(any(), userId) }
    }

    @Test
    fun `getMediaByMediaId returns null if not found`() {
        every { mediaRepository.getMediaByMediaId(any(), userId) } returns null

        val result = mediaService.getMediaByMediaId(userId, mediaId)

        assertNull(result)
    }

    @Test
    fun `updateMedia returns true when repository update succeeds`() {
        every { mediaRepository.updateMedia(any()) } returns true

        val result = mediaService.updateMedia(userId, mediaId, mediaRequest)

        assertTrue(result)
        verify { mediaRepository.updateMedia(any()) }
    }

    @Test
    fun `updateMedia returns false when repository update fails`() {
        every { mediaRepository.updateMedia(any()) } returns false

        val result = mediaService.updateMedia(userId, mediaId, mediaRequest)

        assertFalse(result)
    }

    @Test
    fun `deleteMedia returns true when repository delete succeeds`() {
        every { mediaRepository.deleteMedia(any(), userId) } returns true

        val result = mediaService.deleteMedia(userId, mediaId)

        assertTrue(result)
        verify { mediaRepository.deleteMedia(any(), userId) }
    }

    @Test
    fun `deleteMedia returns false when repository delete fails`() {
        every { mediaRepository.deleteMedia(any(), userId) } returns false

        val result = mediaService.deleteMedia(userId, mediaId)

        assertFalse(result)
    }
}