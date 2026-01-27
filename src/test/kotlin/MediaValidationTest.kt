package io.github.krisalord

import io.github.krisalord.errors.MediaValidationException
import io.github.krisalord.model.media.Genre
import io.github.krisalord.model.media.MediaRequest
import io.github.krisalord.model.media.WatchStatus
import io.github.krisalord.validation.MediaValidation
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class MediaValidationTest {

    @Test
    fun `validateMedia accepts valid media request`() {
        val request = MediaRequest(
            title = "My Movie",
            genres = listOf(Genre.THRILLER),
            rating = 8,
            status = WatchStatus.COMPLETED
        )
        MediaValidation.validateMedia(request)
    }

    @Test
    fun `validateMedia throws if title is blank`() {
        val request = MediaRequest(
            title = "   ",
            genres = listOf(Genre.THRILLER),
            rating = 5,
            status = WatchStatus.PLANNED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateMedia(request)
        }
        assertEquals("Title cannot be blank", exception.message)
    }

    @Test
    fun `validateMedia throws if rating is below 0`() {
        val request = MediaRequest(
            title = "Movie",
            genres = listOf(Genre.THRILLER),
            rating = -1,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateMedia throws if rating is above 10`() {
        val request = MediaRequest(
            title = "Movie",
            genres = listOf(Genre.THRILLER),
            rating = 11,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateMedia throws if genres list is empty`() {
        val request = MediaRequest(
            title = "Movie",
            genres = emptyList(),
            rating = 5,
            status = WatchStatus.DROPPED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateMedia(request)
        }
        assertEquals("At least one genre is required", exception.message)
    }
}
