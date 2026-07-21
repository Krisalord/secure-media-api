package io.github.krisalord.integration.auth

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.UserAlreadyExistsException
import io.github.krisalord.auth.UserModel
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.runDbTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthRepositoryTest : BaseIntegrationTest() {

    private val authRepository = AuthRepository()

    @Test
    fun `create - should insert a new user and map it back correctly`() = runSecureTestApplication {
        val userToCreate = UserModel.create(
            sanitizedEmail = "repo_insert@example.com",
            passwordHash = "secure_hash_string"
        )

        runDbTest {
            val createdUser = authRepository.create(userToCreate)

            assertNotNull(createdUser.id)
            assertEquals("repo_insert@example.com", createdUser.email)
            assertEquals("secure_hash_string", createdUser.passwordHash)
            assertEquals("USER", createdUser.role)
            assertNotNull(createdUser.createdAt)
        }
    }

    @Test
    fun `create - should throw UserAlreadyExistsException when email violates unique constraint`() = runSecureTestApplication {
        val userOne = UserModel.create("duplicate@example.com", "hash1")
        val userTwo = UserModel.create("duplicate@example.com", "hash2")

        runDbTest {
            authRepository.create(userOne)

            assertThrows<UserAlreadyExistsException> {
                authRepository.create(userTwo)
            }
        }
    }

    @Test
    fun `findByEmail - should return the correct user model if email exists`() = runSecureTestApplication {
        val user = UserModel.create("find_me@example.com", "hash123")

        runDbTest {
            authRepository.create(user)
            val foundUser = authRepository.findByEmail("find_me@example.com")

            assertNotNull(foundUser)
            assertEquals(user.id, foundUser.id)
            assertEquals("find_me@example.com", foundUser.email)
        }
    }

    @Test
    fun `findByEmail - should return null if email does not exist`() = runSecureTestApplication {
        runDbTest {
            val foundUser = authRepository.findByEmail("nonexistent@example.com")
            assertNull(foundUser)
        }
    }

    @Test
    fun `findById - should return the correct user model if UUID exists`() = runSecureTestApplication {
        val user = UserModel.create("find_by_id@example.com", "hash123")

        runDbTest {
            val createdUser = authRepository.create(user)
            val foundUser = authRepository.findById(createdUser.id)

            assertNotNull(foundUser)
            assertEquals(createdUser.id, foundUser.id)
            assertEquals("find_by_id@example.com", foundUser.email)
        }
    }

    @Test
    fun `findById - should return null if UUID does not exist in the database`() = runSecureTestApplication {
        runDbTest {
            val randomUuid = UUID.randomUUID().toString()
            val foundUser = authRepository.findById(randomUuid)
            assertNull(foundUser)
        }
    }

    @Test
    fun `findById - should gracefully return null without crashing if provided string is not a valid UUID format`() = runSecureTestApplication {
        runDbTest {
            val foundUser = authRepository.findById("not-a-valid-uuid-string")
            assertNull(foundUser)
        }
    }
}