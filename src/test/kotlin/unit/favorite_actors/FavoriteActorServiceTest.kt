package io.github.krisalord.unit.favorite_actors

import io.github.krisalord.core.database.dbQuery
import io.github.krisalord.favorite_actors.CreateFavoriteActorRequest
import io.github.krisalord.favorite_actors.FavoriteActorNotFoundException
import io.github.krisalord.favorite_actors.FavoriteActorRepository
import io.github.krisalord.favorite_actors.FavoriteActorService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FavoriteActorServiceTest {

    private lateinit var favoriteActorRepository: FavoriteActorRepository
    private lateinit var favoriteActorService: FavoriteActorService

    @BeforeEach
    fun setup() {
        // BYPASS THE DATABASE
        mockkStatic("io.github.krisalord.core.database.DatabaseFactoryKt")
        coEvery { dbQuery<Any?>(any()) } coAnswers {
            val block = firstArg<() -> Any?>() // Non-suspended block
            block.invoke()
        }

        favoriteActorRepository = mockk()
        favoriteActorService = FavoriteActorService(favoriteActorRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll() // Always clean up!
    }

    @Test
    fun `logFavoriteActor - should sanitize multiple spaces in names before saving`() = runBlocking {
        val request = CreateFavoriteActorRequest("  Keanu    Reeves  ")
        val userId = "user-123"

        coEvery { favoriteActorRepository.create(any()) } returns mockk()

        favoriteActorService.logFavoriteActor(userId, request)

        coVerify {
            favoriteActorRepository.create(withArg {
                assertEquals("Keanu Reeves", it.name)
            })
        }
    }

    @Test
    fun `removeFavoriteActor - should throw exception if repository returns false`() = runBlocking {
        coEvery { favoriteActorRepository.deleteByIdAndUserId(any(), any()) } returns false

        val exception = assertThrows<FavoriteActorNotFoundException> {
            favoriteActorService.removeFavoriteActor("actor-123", "user-123")
        }

        assertEquals("Favorite actor not found or access denied.", exception.message)
    }
}