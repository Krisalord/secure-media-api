package validation

import io.github.krisalord.errors.MediaValidationException
import io.github.krisalord.model.media.Genre
import io.github.krisalord.model.media.WatchStatus
import io.github.krisalord.models.media.dto.UpdateMediaRequest
import io.github.krisalord.validation.input.MediaValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateMediaValidationTest {

    @Test
    fun `validateUpdateMedia accepts valid media request`() {
        val request = UpdateMediaRequest(
            title = "Updated Movie",
            genres = listOf(Genre.THRILLER, Genre.DRAMA),
            rating = 7,
            status = WatchStatus.PLANNED
        )
        MediaValidation.validateUpdateMedia(request)
    }

    @Test
    fun `validateUpdateMedia throws if title is blank`() {
        val request = UpdateMediaRequest(
            title = "   ",
            genres = listOf(Genre.ACTION),
            rating = 5,
            status = WatchStatus.PLANNED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateUpdateMedia(request)
        }
        assertEquals("Title cannot be blank", exception.message)
    }

    @Test
    fun `validateUpdateMedia throws if rating is below 0`() {
        val request = UpdateMediaRequest(
            title = "Movie",
            genres = listOf(Genre.COMEDY),
            rating = -2,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateUpdateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateUpdateMedia throws if rating is above 10`() {
        val request = UpdateMediaRequest(
            title = "Movie",
            genres = listOf(Genre.HORROR),
            rating = 15,
            status = WatchStatus.DROPPED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateUpdateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateUpdateMedia throws if genres list is empty`() {
        val request = UpdateMediaRequest(
            title = "Movie",
            genres = emptyList(),
            rating = 6,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateUpdateMedia(request)
        }
        assertEquals("At least one genre is required", exception.message)
    }
}
