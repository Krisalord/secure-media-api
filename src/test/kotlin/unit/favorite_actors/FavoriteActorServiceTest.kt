//package io.github.krisalord.unit.favorite_actors
//
//import io.github.krisalord.favorite_actors.CreateFavoriteActorRequest
//import io.github.krisalord.favorite_actors.FavoriteActorNotFoundException
//import io.github.krisalord.favorite_actors.FavoriteActorRepository
//import io.github.krisalord.favorite_actors.FavoriteActorService
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.mockk
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import kotlin.test.assertEquals
//
//class FavoriteActorServiceTest {
//
//    private lateinit var favoriteActorRepository: FavoriteActorRepository
//    private lateinit var favoriteActorService: FavoriteActorService
//
//    @BeforeEach
//    fun setup() {
//        favoriteActorRepository = mockk()
//        favoriteActorService = FavoriteActorService(favoriteActorRepository)
//    }
//
//    @Test
//    fun `logFavoriteActor - should sanitize multiple spaces in names before saving`() = runBlocking {
//        val request = CreateFavoriteActorRequest("  Keanu    Reeves  ")
//        val userId = "user-123"
//
//        coEvery { favoriteActorRepository.create(any()) } returns mockk()
//
//        favoriteActorService.logFavoriteActor(userId, request)
//
//        coVerify {
//            favoriteActorRepository.create(withArg {
//                assertEquals("Keanu Reeves", it.name)
//            })
//        }
//    }
//
//    @Test
//    fun `removeFavoriteActor - should throw exception if repository returns false`() = runBlocking {
//        coEvery { favoriteActorRepository.deleteByIdAndUserId(any(), any()) } returns false
//
//        val exception = assertThrows<FavoriteActorNotFoundException> {
//            favoriteActorService.removeFavoriteActor("actor-123", "user-123")
//        }
//
//        assertEquals("Favorite actor not found or access denied.", exception.message)
//    }
//}