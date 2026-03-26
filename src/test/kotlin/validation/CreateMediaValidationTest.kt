package validation

import io.github.krisalord.errors.MediaValidationException
import io.github.krisalord.models.media.Genre
import io.github.krisalord.models.media.WatchStatus
import io.github.krisalord.models.media.dto.CreateMediaRequest
import io.github.krisalord.validation.MediaValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateMediaValidationTest {

    @Test
    fun `validateCreateMedia accepts valid media request`() {
        val request = CreateMediaRequest(
            title = "Created Movie",
            genres = listOf(Genre.THRILLER, Genre.DRAMA),
            rating = 7,
            status = WatchStatus.PLANNED
        )
        MediaValidation.validateCreateMedia(request)
    }

    @Test
    fun `validateCreateMedia throws if title is blank`() {
        val request = CreateMediaRequest(
            title = "   ",
            genres = listOf(Genre.ACTION),
            rating = 5,
            status = WatchStatus.PLANNED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateCreateMedia(request)
        }
        assertEquals("Title cannot be blank", exception.message)
    }

    @Test
    fun `validateCreateMedia throws if rating is below 0`() {
        val request = CreateMediaRequest(
            title = "Movie",
            genres = listOf(Genre.COMEDY),
            rating = -2,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateCreateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateCreateMedia throws if rating is above 10`() {
        val request = CreateMediaRequest(
            title = "Movie",
            genres = listOf(Genre.HORROR),
            rating = 15,
            status = WatchStatus.DROPPED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateCreateMedia(request)
        }
        assertEquals("Rating must be between 0 and 10", exception.message)
    }

    @Test
    fun `validateCreateMedia throws if genres list is empty`() {
        val request = CreateMediaRequest(
            title = "Movie",
            genres = emptyList(),
            rating = 6,
            status = WatchStatus.COMPLETED
        )

        val exception = assertFailsWith<MediaValidationException> {
            MediaValidation.validateCreateMedia(request)
        }
        assertEquals("At least one genre is required", exception.message)
    }
}
