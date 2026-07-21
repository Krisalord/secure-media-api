package io.github.krisalord.integration.favorite_actors

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.UserModel
import io.github.krisalord.favorite_actors.FavoriteActorModel
import io.github.krisalord.favorite_actors.FavoriteActorRepository
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.runDbTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteActorRepositoryTest : BaseIntegrationTest() {

    private val authRepository = AuthRepository()
    private val actorRepository = FavoriteActorRepository()

    @Test
    fun `create and findAllByUserId - should insert and retrieve favorite actors`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("actor_repo@example.com", "hash"))
            val actor = FavoriteActorModel.create(user.id, "Keanu Reeves")

            actorRepository.create(actor)
            val results = actorRepository.findAllByUserId(user.id)

            assertEquals(1, results.size)
            assertEquals("Keanu Reeves", results.first().name)
        }
    }

    @Test
    fun `deleteByIdAndUserId - should return true on successful deletion`() = runSecureTestApplication {
        runDbTest {
            val user = authRepository.create(UserModel.create("delete_actor@example.com", "hash"))
            val actor = actorRepository.create(FavoriteActorModel.create(user.id, "Tom Hanks"))

            val deleted = actorRepository.deleteByIdAndUserId(actor.id, user.id)
            assertTrue(deleted)

            val remaining = actorRepository.findAllByUserId(user.id)
            assertTrue(remaining.isEmpty())
        }
    }
}