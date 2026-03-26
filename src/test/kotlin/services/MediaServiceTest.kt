package services

import io.github.krisalord.errors.NotFoundException
import io.github.krisalord.models.media.Genre
import io.github.krisalord.models.media.MediaModel
import io.github.krisalord.models.media.WatchStatus
import io.github.krisalord.models.media.dto.CreateMediaRequest
import io.github.krisalord.models.media.dto.SearchMediaResponse
import io.github.krisalord.models.media.dto.UpdateMediaRequest
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.services.MediaCache
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import kotlin.test.*

class MediaServiceTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var mediaService: io.github.krisalord.services.MediaService

    @BeforeTest
    fun setup() {
        mediaRepository = mockk()
        mediaService = io.github.krisalord.services.MediaService(mediaRepository)

        mockkObject(MediaCache)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createMedia returns CreateMediaResponse and invalidates cache`() = runTest {
        val userId = "user123"
        val request = CreateMediaRequest(
            title = "My Media",
            genres = listOf(Genre.ACTION),
            rating = 8,
            status = WatchStatus.WATCHING
        )
        val savedMedia = MediaModel(
            id = ObjectId(),
            userId = userId,
            title = request.title,
            genres = request.genres,
            rating = request.rating,
            status = request.status
        )

        coEvery { mediaRepository.addMedia(any()) } returns savedMedia
        every { MediaCache.invalidateAllMediaOfUser(userId) } just Runs

        val response = mediaService.createMedia(userId, request)

        assertEquals(savedMedia.title, response.title)
        assertEquals(savedMedia.genres, response.genres)
        assertEquals(savedMedia.rating, response.rating)
        assertEquals(savedMedia.status, response.status)
        coVerify { mediaRepository.addMedia(any()) }
        verify { MediaCache.invalidateAllMediaOfUser(userId) }
    }

    @Test
    fun `getMediaByUserId returns cached list if present`() = runTest {
        val userId = "user123"
        val cachedList = listOf(
            SearchMediaResponse(
                id = "1",
                title = "Title",
                genres = listOf(Genre.ACTION),
                rating = 8,
                status = WatchStatus.WATCHING
            )
        )
        every { MediaCache.getAllMediaOfUser(userId) } returns cachedList

        val result = mediaService.getMediaByUserId(userId)

        assertEquals(cachedList, result)
        verify { MediaCache.getAllMediaOfUser(userId) }
        coVerify(exactly = 0) { mediaRepository.getAllMediaByUserId(any()) }
    }

    @Test
    fun `getMediaByUserId fetches from repository and caches if not present`() = runTest {
        val userId = "user123"
        every { MediaCache.getAllMediaOfUser(userId) } returns null

        val mediaModel = MediaModel(
            id = ObjectId(),
            userId = userId,
            title = "Title",
            genres = listOf(Genre.ACTION),
            rating = 8,
            status = WatchStatus.WATCHING
        )
        coEvery { mediaRepository.getAllMediaByUserId(userId) } returns listOf(mediaModel)
        every { MediaCache.setAllMediaOfUser(userId, any()) } just Runs

        val result = mediaService.getMediaByUserId(userId)

        assertEquals(1, result.size)
        assertEquals(mediaModel.title, result[0].title)
        coVerify { mediaRepository.getAllMediaByUserId(userId) }
        verify { MediaCache.setAllMediaOfUser(userId, any()) }
    }

    @Test
    fun `getMediaByMediaId returns media if found`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()
        val media = MediaModel(
            id = ObjectId(mediaId),
            userId = userId,
            title = "Title",
            genres = listOf(Genre.ACTION),
            rating = 8,
            status = WatchStatus.WATCHING
        )

        coEvery { mediaRepository.getMediaByMediaId(ObjectId(mediaId), userId) } returns media

        val response = mediaService.getMediaByMediaId(userId, mediaId)

        assertEquals(media.title, response.title)
        coVerify { mediaRepository.getMediaByMediaId(ObjectId(mediaId), userId) }
    }

    @Test
    fun `getMediaByMediaId throws NotFoundException if not found`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()

        coEvery { mediaRepository.getMediaByMediaId(ObjectId(mediaId), userId) } returns null

        assertFailsWith<NotFoundException> {
            mediaService.getMediaByMediaId(userId, mediaId)
        }
    }

    @Test
    fun `updateMedia calls repository and invalidates cache`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()
        val request = UpdateMediaRequest(
            title = "New Title",
            genres = listOf(Genre.DRAMA),
            rating = 9,
            status = WatchStatus.COMPLETED
        )

        every { MediaCache.invalidateAllMediaOfUser(userId) } just Runs
        coEvery { mediaRepository.updateMedia(match { it.id.toHexString() == mediaId && it.userId == userId }) } returns true

        mediaService.updateMedia(userId, mediaId, request)

        verify { MediaCache.invalidateAllMediaOfUser(userId) }
        coVerify { mediaRepository.updateMedia(match { it.id.toHexString() == mediaId && it.userId == userId }) }
    }

    @Test
    fun `updateMedia throws NotFoundException if media does not exist`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()
        val request = UpdateMediaRequest(
            title = "Title",
            genres = listOf(Genre.ACTION),
            rating = 8,
            status = WatchStatus.WATCHING
        )

        coEvery { mediaRepository.updateMedia(match { it.id.toHexString() == mediaId && it.userId == userId }) } returns false

        assertFailsWith<NotFoundException> {
            mediaService.updateMedia(userId, mediaId, request)
        }
    }

    @Test
    fun `deleteMedia calls repository and invalidates cache`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()

        coEvery { mediaRepository.deleteMedia(ObjectId(mediaId), userId) } returns true
        every { MediaCache.invalidateAllMediaOfUser(userId) } just Runs

        mediaService.deleteMedia(userId, mediaId)

        coVerify { mediaRepository.deleteMedia(ObjectId(mediaId), userId) }
        verify { MediaCache.invalidateAllMediaOfUser(userId) }
    }

    @Test
    fun `deleteMedia throws NotFoundException if media does not exist`() = runTest {
        val userId = "user123"
        val mediaId = ObjectId().toHexString()

        coEvery { mediaRepository.deleteMedia(ObjectId(mediaId), userId) } returns false

        assertFailsWith<NotFoundException> {
            mediaService.deleteMedia(userId, mediaId)
        }
    }
}