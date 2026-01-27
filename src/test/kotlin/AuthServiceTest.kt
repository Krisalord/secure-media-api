package io.github.krisalord

import io.github.krisalord.errors.InvalidPasswordException
import io.github.krisalord.errors.UserAlreadyExistsException
import io.github.krisalord.errors.UserNotFoundException
import io.github.krisalord.model.user.UserModel
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.services.AuthService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordHashing: PasswordHashing
    private lateinit var authService: AuthService

    @BeforeTest
    fun setup() {
        userRepository = mockk()
        passwordHashing = mockk()
        authService = AuthService(userRepository, passwordHashing)
    }

    @Test
    fun `register throws UserAlreadyExistsException if email exists`() {
        every { userRepository.findByEmail("test@example.com") } returns UserModel(ObjectId(), "test@example.com", "hash")

        assertFailsWith<UserAlreadyExistsException> {
            authService.register("test@example.com", "password123")
        }

        verify { userRepository.findByEmail("test@example.com") }
    }

    @Test
    fun `register returns UserModel if email is new`() {
        every { userRepository.findByEmail("new@example.com") } returns null
        every { passwordHashing.hash("password123") } returns "hashedPassword"
        every { userRepository.registerUser(any()) } answers { firstArg() }

        val user = authService.register("new@example.com", "password123")

        assertEquals("new@example.com", user.email)
        assertEquals("hashedPassword", user.passwordHash)
    }

    @Test
    fun `login throws UserNotFoundException if user does not exist`() {
        every { userRepository.findByEmail("unknown@example.com") } returns null

        assertFailsWith<UserNotFoundException> {
            authService.login("unknown@example.com", "password123")
        }
    }

    @Test
    fun `login throws InvalidPasswordException if password is wrong`() {
        val user = UserModel(ObjectId(), "test@example.com", "hash")
        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordHashing.verify("wrongPassword", "hash") } returns false

        assertFailsWith<InvalidPasswordException> {
            authService.login("test@example.com", "wrongPassword")
        }
    }

    @Test
    fun `login returns token if credentials are correct`() {
        val user = UserModel(ObjectId(), "test@example.com", "hash")
        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordHashing.verify("password123", "hash") } returns true

        val token = authService.login("test@example.com", "password123")
        assert(token.isNotEmpty())
    }
}
