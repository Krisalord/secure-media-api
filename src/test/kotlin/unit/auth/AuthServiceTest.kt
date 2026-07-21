//package io.github.krisalord.unit.auth
//
//import io.github.krisalord.auth.*
//import io.github.krisalord.core.security.PasswordHashing
//import io.github.krisalord.core.security.TokenProvider
//import io.mockk.coEvery
//import io.mockk.mockk
//import io.mockk.mockkStatic
//import io.mockk.unmockkAll
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import kotlin.test.assertEquals
//
//class AuthServiceTest {
//
//    private lateinit var authRepository: AuthRepository
//    private lateinit var refreshSessionRepository: RefreshSessionRepository
//    private lateinit var tokenProvider: TokenProvider
//    private lateinit var authService: AuthService
//
//    @BeforeEach
//    fun setup() {
//        // BYPASS THE DATABASE: Intercept dbQuery so Exposed doesn't crash
//        mockkStatic("io.github.krisalord.core.database.DatabaseFactoryKt")
//        coEvery { io.github.krisalord.core.database.dbQuery<Any>(any()) } coAnswers {
//            val block = firstArg<suspend () -> Any>()
//            block.invoke()
//        }
//
//        authRepository = mockk()
//        refreshSessionRepository = mockk()
//        tokenProvider = mockk()
//
//        authService = AuthService(
//            authRepository = authRepository,
//            refreshSessionRepository = refreshSessionRepository,
//            tokenProvider = tokenProvider,
//            reuseDetectionEnabled = true,
//            maxSessionsPerUser = 5
//        )
//    }
//
//    @AfterEach
//    fun tearDown() {
//        unmockkAll() // Clean up static mocks after the test
//    }
//
//    @Test
//    fun `login - should throw UnauthorizedException when password does not match hash`() = runBlocking {
//        val request = LoginRequest("test@example.com", "WrongPassword123!")
//        val userWithDifferentHash = UserModel.create("test@example.com", PasswordHashing.hash("CorrectPassword123!"))
//
//        coEvery { authRepository.findByEmail(any()) } returns userWithDifferentHash
//
//        val exception = assertThrows<UnauthorizedException> {
//            authService.login(request, "Test Agent", "127.0.0.1")
//        }
//
//        assertEquals("Invalid credentials", exception.message)
//    }
//
//    @Test
//    fun `login - should throw UnauthorizedException when user does not exist`() = runBlocking {
//        val request = LoginRequest("ghost@example.com", "Password123!")
//        coEvery { authRepository.findByEmail(any()) } returns null
//
//        val exception = assertThrows<UnauthorizedException> {
//            authService.login(request, "Test Agent", "127.0.0.1")
//        }
//
//        assertEquals("Invalid credentials", exception.message)
//    }
//}