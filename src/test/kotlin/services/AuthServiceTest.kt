package services

import com.mongodb.MongoWriteException
import com.mongodb.WriteError
import io.github.krisalord.config.JwtConfig
import io.github.krisalord.errors.InvalidPasswordException
import io.github.krisalord.errors.UserAlreadyExistsException
import io.github.krisalord.errors.UserNotFoundException
import io.github.krisalord.model.user.UserModel
import io.github.krisalord.model.user.dto.LoginRequest
import io.github.krisalord.model.user.dto.RegisterRequest
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.services.AuthService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import kotlin.test.*

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordHashing: PasswordHashing
    private lateinit var jwtConfig: JwtConfig
    private lateinit var authService: AuthService

    @BeforeTest
    fun setup() {
        userRepository = mockk()
        passwordHashing = mockk()
        jwtConfig = mockk()

        authService = AuthService(
            userRepository,
            passwordHashing,
            jwtConfig
        )
    }

    @Test
    fun `register throws UserAlreadyExistsException if email exists`() = runTest {
        val writeError = mockk<WriteError>()
        every { writeError.code } returns 11000

        val mongoException = mockk<MongoWriteException>()
        every { mongoException.error } returns writeError

        every { passwordHashing.hash("password123") } returns "hash"

        coEvery { userRepository.registerUser(any()) } throws mongoException

        assertFailsWith<UserAlreadyExistsException> {
            authService.register(RegisterRequest("test@example.com", "password123"))
        }

        coVerify { userRepository.registerUser(any()) }
    }

    @Test
    fun `register returns RegisterResponse if email is new`() = runTest {
        every { passwordHashing.hash("password123") } returns "hashedPassword"
        coEvery { userRepository.registerUser(any()) } answers { firstArg() }

        val response = authService.register(RegisterRequest("new@example.com", "password123"))

        assertEquals("new@example.com", response.email)
        assertTrue(response.id.isNotEmpty())
    }

    @Test
    fun `login throws UserNotFoundException if user does not exist`() = runTest {
        coEvery { userRepository.findByEmail("unknown@example.com") } returns null

        assertFailsWith<UserNotFoundException> {
            authService.login(LoginRequest("unknown@example.com", "password123"))
        }
    }

    @Test
    fun `login throws InvalidPasswordException if password is wrong`() = runTest {
        val user = UserModel(ObjectId(), "test@example.com", "hash")

        coEvery { userRepository.findByEmail("test@example.com") } returns user
        every { passwordHashing.verify("wrongPassword", "hash") } returns false

        assertFailsWith<InvalidPasswordException> {
            authService.login(LoginRequest("test@example.com", "wrongPassword"))
        }
    }

    @Test
    fun `login returns token if credentials are correct`() = runTest {
        val userId = ObjectId()
        val user = UserModel(userId, "test@example.com", "hash")

        coEvery { userRepository.findByEmail("test@example.com") } returns user
        every { passwordHashing.verify("password123", "hash") } returns true
        every { jwtConfig.generateToken(userId.toHexString()) } returns "jwt-token"

        val response = authService.login(LoginRequest("test@example.com", "password123"))

        assertTrue(response.token.isNotEmpty())
        assertEquals("jwt-token", response.token)
        assertEquals("Bearer", response.type)
    }

    @Test
    fun `login fails if token generation fails`() = runTest {
        val user = UserModel(ObjectId(), "test@example.com", "hash")

        coEvery { userRepository.findByEmail(any()) } returns user
        every { passwordHashing.verify(any(), any()) } returns true
        every { jwtConfig.generateToken(any()) } throws RuntimeException("JWT error")

        assertFails {
            authService.login(LoginRequest("test@example.com", "password"))
        }
    }
}