package io.github.krisalord.unit.auth

import io.github.krisalord.auth.*
import io.github.krisalord.core.security.PasswordHashing
import io.github.krisalord.core.security.TokenProvider
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import kotlin.test.*

class AuthServiceTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var refreshSessionRepository: RefreshSessionRepository
    private lateinit var tokenProvider: TokenProvider
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        // BYPASS THE DATABASE: Intercept dbQuery so Exposed doesn't crash
        mockkStatic("io.github.krisalord.core.database.DatabaseFactoryKt")
        coEvery { io.github.krisalord.core.database.dbQuery<Any?>(any()) } coAnswers {
            // REMOVED 'suspend' here!
            val block = firstArg<() -> Any?>()
            block.invoke()
        }

        authRepository = mockk()
        refreshSessionRepository = mockk()
        tokenProvider = mockk()

        authService = AuthService(
            authRepository = authRepository,
            refreshSessionRepository = refreshSessionRepository,
            tokenProvider = tokenProvider,
            reuseDetectionEnabled = true,
            maxSessionsPerUser = 5
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll() // Clean up static mocks after the test
    }

    // --- Login Tests ---

    @Test
    fun `login - should throw UnauthorizedException when password does not match hash`() = runBlocking {
        val request = LoginRequest("test@example.com", "WrongPassword123!")
        val userWithDifferentHash = UserModel.create("test@example.com", PasswordHashing.hash("CorrectPassword123!"))

        coEvery { authRepository.findByEmail(any()) } returns userWithDifferentHash

        val exception = assertThrows<UnauthorizedException> {
            authService.login(request, "Test Agent", "127.0.0.1")
        }

        assertEquals("Invalid credentials", exception.message)
    }

    @Test
    fun `login - should throw UnauthorizedException when user does not exist`() = runBlocking {
        val request = LoginRequest("ghost@example.com", "Password123!")
        coEvery { authRepository.findByEmail(any()) } returns null

        val exception = assertThrows<UnauthorizedException> {
            authService.login(request, "Test Agent", "127.0.0.1")
        }

        assertEquals("Invalid credentials", exception.message)
    }

    @Test
    fun `login - should return tokens on valid credentials`() = runBlocking {
        val password = "CorrectPassword123!"
        val request = LoginRequest("test@example.com", password)
        val user = UserModel.create("test@example.com", PasswordHashing.hash(password))

        val refreshToken = "generated-refresh-token"
        val accessToken = "generated-access-token"
        val refreshSession = RefreshSessionModel(
            id = null,
            userId = user.id,
            refreshTokenHash = "hashed-refresh",
            expiresAt = Instant.now().plusSeconds(604800),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = "Test Agent",
            ipAddress = "127.0.0.1"
        )

        coEvery { authRepository.findByEmail("test@example.com") } returns user
        coEvery { tokenProvider.generateRefreshToken() } returns refreshToken
        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-refresh"
        coEvery { tokenProvider.buildRefreshSession(any(), any(), any(), any()) } returns refreshSession
        coEvery { refreshSessionRepository.create(any()) } returns refreshSession
        coEvery { tokenProvider.generateAccessToken(user) } returns accessToken
        coEvery { refreshSessionRepository.enforceSessionLimit(any(), any()) } returns Unit

        val (response, returnedRefreshToken) = authService.login(request, "Test Agent", "127.0.0.1")

        assertNotNull(response.accessToken)
        assertEquals(refreshToken, returnedRefreshToken)
        coVerify(exactly = 1) { authRepository.findByEmail("test@example.com") }
        coVerify(exactly = 1) { refreshSessionRepository.create(any()) }
    }

    @Test
    fun `login - should trim and lowercase email before lookup`() = runBlocking {
        val password = "CorrectPassword123!"
        val request = LoginRequest("  Test@Example.COM  ", password)
        val user = UserModel.create("test@example.com", PasswordHashing.hash(password))

        val refreshToken = "refresh-token"
        val accessToken = "access-token"
        val refreshSession = RefreshSessionModel(
            id = null,
            userId = user.id,
            refreshTokenHash = "hashed",
            expiresAt = Instant.now().plusSeconds(604800),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        coEvery { authRepository.findByEmail("test@example.com") } returns user
        coEvery { tokenProvider.generateRefreshToken() } returns refreshToken
        coEvery { tokenProvider.hashRefreshToken(any()) } returns "hashed"
        coEvery { tokenProvider.buildRefreshSession(any(), any(), any(), any()) } returns refreshSession
        coEvery { refreshSessionRepository.create(any()) } returns refreshSession
        coEvery { tokenProvider.generateAccessToken(user) } returns accessToken
        coEvery { refreshSessionRepository.enforceSessionLimit(any(), any()) } returns Unit

        val result = authService.login(request, null, null)

        assertNotNull(result.first.accessToken)
        coVerify(exactly = 1) { authRepository.findByEmail("test@example.com") }
    }

    // --- Register Tests ---

    @Test
    fun `register - should create user with sanitized email and hashed password`() = runBlocking {
        val request = RegisterRequest("  NewUser@Example.COM  ", "SecurePassword123!")
        val expectedEmail = "newuser@example.com"

        coEvery { authRepository.create(any()) } answers {
            val model = firstArg<UserModel>()
            assertEquals(expectedEmail, model.email)
            assertNotEquals("SecurePassword123!", model.passwordHash)
            model
        }

        authService.register(request)

        coVerify(exactly = 1) { authRepository.create(any()) }
    }

    @Test
    fun `register - should throw AuthValidationException for blank email`() = runBlocking {
        val request = RegisterRequest("   ", "SecurePassword123!")

        val exception = assertThrows<AuthValidationException> {
            authService.register(request)
        }

        assertEquals("Email can not be blank", exception.message)
    }

    @Test
    fun `register - should throw AuthValidationException for short password`() = runBlocking {
        val request = RegisterRequest("test@example.com", "short")

        val exception = assertThrows<AuthValidationException> {
            authService.register(request)
        }

        assertEquals("Password must be at least 8 characters", exception.message)
    }

    // --- Refresh Tests ---

    @Test
    fun `refresh - should return new tokens for valid refresh token`() = runBlocking {
        val refreshToken = "valid-refresh-token"
        val newRefreshToken = "new-refresh-token"
        val accessToken = "new-access-token"
        val userId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()

        val session = RefreshSessionModel(
            id = sessionId,
            userId = userId,
            refreshTokenHash = "hashed-refresh",
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        val user = UserModel.create("test@example.com", "hashed-password")
        val newRefreshSession = RefreshSessionModel(
            id = null,
            userId = userId,
            refreshTokenHash = "hashed-new",
            expiresAt = Instant.now().plusSeconds(604800),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = "New Agent",
            ipAddress = "192.168.1.1"
        )

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-refresh"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-refresh") } returns session
        coEvery { refreshSessionRepository.revokeActiveById(sessionId) } returns true
        coEvery { authRepository.findById(userId) } returns user
        coEvery { tokenProvider.generateRefreshToken() } returns newRefreshToken
        coEvery { tokenProvider.hashRefreshToken(newRefreshToken) } returns "hashed-new"
        coEvery { tokenProvider.buildRefreshSession(any(), any(), any(), any()) } returns newRefreshSession
        coEvery { refreshSessionRepository.create(any()) } returns newRefreshSession
        coEvery { tokenProvider.generateAccessToken(user) } returns accessToken
        coEvery { refreshSessionRepository.enforceSessionLimit(any(), any()) } returns Unit

        val (response, returnedRefreshToken) = authService.refresh(refreshToken, "New Agent", "192.168.1.1")

        assertEquals(accessToken, response.accessToken)
        assertEquals(newRefreshToken, returnedRefreshToken)
        coVerify(exactly = 1) { refreshSessionRepository.revokeActiveById(sessionId) }
    }

    @Test
    fun `refresh - should throw UnauthorizedException for invalid refresh token`() = runBlocking {
        val refreshToken = "invalid-token"

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-invalid"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-invalid") } returns null

        val exception = assertThrows<UnauthorizedException> {
            authService.refresh(refreshToken, null, null)
        }

        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refresh - should throw UnauthorizedException for expired session`() = runBlocking {
        val refreshToken = "expired-token"
        val sessionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val expiredSession = RefreshSessionModel(
            id = sessionId,
            userId = userId,
            refreshTokenHash = "hashed-expired",
            expiresAt = Instant.now().minusSeconds(3600),
            createdAt = Instant.now().minusSeconds(7200),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-expired"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-expired") } returns expiredSession

        val exception = assertThrows<UnauthorizedException> {
            authService.refresh(refreshToken, null, null)
        }

        assertEquals("Session expired", exception.message)
    }

    @Test
    fun `refresh - should detect token reuse and revoke all sessions`() = runBlocking {
        val refreshToken = "reused-token"
        val sessionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val session = RefreshSessionModel(
            id = sessionId,
            userId = userId,
            refreshTokenHash = "hashed-reused",
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-reused"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-reused") } returns session
        coEvery { refreshSessionRepository.revokeActiveById(sessionId) } returns false
        coEvery { refreshSessionRepository.revokeAllByUserId(userId) } returns Unit

        val exception = assertThrows<UnauthorizedException> {
            authService.refresh(refreshToken, null, null)
        }

        assertEquals("Token reuse detected", exception.message)
        coVerify(exactly = 1) { refreshSessionRepository.revokeAllByUserId(userId) }
    }

    @Test
    fun `refresh - should skip reuse detection when disabled`() = runBlocking {
        val authServiceNoDetection = AuthService(
            authRepository = authRepository,
            refreshSessionRepository = refreshSessionRepository,
            tokenProvider = tokenProvider,
            reuseDetectionEnabled = false,
            maxSessionsPerUser = 5
        )

        val refreshToken = "reused-token"
        val sessionId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val session = RefreshSessionModel(
            id = sessionId,
            userId = userId,
            refreshTokenHash = "hashed-reused",
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-reused"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-reused") } returns session
        coEvery { refreshSessionRepository.revokeActiveById(sessionId) } returns false

        val exception = assertThrows<UnauthorizedException> {
            authServiceNoDetection.refresh(refreshToken, null, null)
        }

        // Should still throw because revokeActiveById returned false, but should NOT revoke all sessions
        coVerify(exactly = 0) { refreshSessionRepository.revokeAllByUserId(any()) }
    }

    // --- Logout Tests ---

    @Test
    fun `logout - should revoke session when refresh token exists`() = runBlocking {
        val refreshToken = "logout-token"
        val sessionId = UUID.randomUUID()

        val session = RefreshSessionModel(
            id = sessionId,
            userId = UUID.randomUUID(),
            refreshTokenHash = "hashed-logout",
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = null,
            ipAddress = null
        )

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-logout"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-logout") } returns session
        coEvery { refreshSessionRepository.revokeActiveById(sessionId) } returns true

        authService.logout(refreshToken)

        coVerify(exactly = 1) { refreshSessionRepository.revokeActiveById(sessionId) }
    }

    @Test
    fun `logout - should handle missing refresh token gracefully`() = runBlocking {
        val refreshToken = "missing-token"

        coEvery { tokenProvider.hashRefreshToken(refreshToken) } returns "hashed-missing"
        coEvery { refreshSessionRepository.findByTokenHash("hashed-missing") } returns null

        // Should not throw
        authService.logout(refreshToken)

        coVerify(exactly = 0) { refreshSessionRepository.revokeActiveById(any()) }
    }

    // --- Logout All Tests ---

    @Test
    fun `logoutAll - should revoke all sessions for a user`() = runBlocking {
        val userId = UUID.randomUUID()

        coEvery { refreshSessionRepository.revokeAllByUserId(userId) } returns Unit

        authService.logoutAll(userId)

        coVerify(exactly = 1) { refreshSessionRepository.revokeAllByUserId(userId) }
    }
}