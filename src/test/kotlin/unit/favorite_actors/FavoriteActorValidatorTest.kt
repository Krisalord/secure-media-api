package io.github.krisalord.unit.favorite_actors

import io.github.krisalord.favorite_actors.CreateFavoriteActorRequest
import io.github.krisalord.favorite_actors.FavoriteActorValidator
import io.github.krisalord.favorite_actors.InvalidFavoriteActorDataException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class FavoriteActorValidatorTest {

    @Test
    fun `validateLogRequest - should pass on valid name`() {
        val request = CreateFavoriteActorRequest(name = "Keanu Reeves")
        assertDoesNotThrow {
            FavoriteActorValidator.validateLogRequest(request)
        }
    }

    @Test
    fun `validateLogRequest - should throw when name is blank`() {
        val request = CreateFavoriteActorRequest(name = "   ")
        assertThrows<InvalidFavoriteActorDataException> {
            FavoriteActorValidator.validateLogRequest(request)
        }
    }

    @Test
    fun `validateLogRequest - should throw when name exceeds 255 chars`() {
        val longName = "a".repeat(256)
        val request = CreateFavoriteActorRequest(name = longName)
        assertThrows<InvalidFavoriteActorDataException> {
            FavoriteActorValidator.validateLogRequest(request)
        }
    }
}