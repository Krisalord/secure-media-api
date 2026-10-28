package io.github.krisalord.unit.core.security

import io.github.krisalord.auth.UserModel
import io.github.krisalord.core.security.AccessTokenSettings
import io.github.krisalord.core.security.RefreshTokenSettings
import io.github.krisalord.core.security.TokenProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Base64
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenProviderTest {

    private lateinit var tokenProvider: TokenProvider
    private lateinit var dummyUser: UserModel

    @BeforeEach
    fun setup() {
        val accessSettings = AccessTokenSettings(
            secret = "super-secret-test-key",
            issuer = "test-issuer",
            audience = "test-audience",
            validityMs = 60000
        )
        val refreshSettings = RefreshTokenSettings(
            validityDays = 7,
            reuseDetectionEnabled = true,
            maxSessionsPerUser = 5,
            tokenHashPepper = "test-pepper"
        )

        tokenProvider = TokenProvider(accessSettings, refreshSettings)
        dummyUser = UserModel.create("test@example.com", "hashed_pass")
    }

    @Test
    fun `generateAccessToken - should create a valid decodable JWT`() {
        val token = tokenProvider.generateAccessToken(dummyUser)

        assertNotNull(token)
        assertEquals(token.split(".").size, 3)
    }

    @Test
    fun `verifier - should successfully verify a valid generated token`() {
        val token = tokenProvider.generateAccessToken(dummyUser)
        val decodedJwt = tokenProvider.verifier().verify(token)

        assertEquals("test-issuer", decodedJwt.issuer)
        assertEquals(dummyUser.id.toString(), decodedJwt.subject)
        assertEquals("USER", decodedJwt.getClaim("role").asString())
    }

    @Test
    fun `generateRefreshToken - should create a secure URL-safe base64 string`() {
        val token = tokenProvider.generateRefreshToken()

        assertNotNull(token)
        Assertions.assertFalse(token.contains("="))
        val decodedBytes = Base64.getUrlDecoder().decode(token)
        assertEquals(32, decodedBytes.size)
    }

    @Test
    fun `hashRefreshToken - should produce consistent HMAC SHA-256 hashes`() {
        val rawToken = "my-raw-refresh-token"

        val hash1 = tokenProvider.hashRefreshToken(rawToken)
        val hash2 = tokenProvider.hashRefreshToken(rawToken)

        assertEquals(hash1, hash2)
        assertEquals(hash1.length, 64)
    }

    @Test
    fun `hashRefreshToken - different tokens should produce different hashes`() {
        val hash1 = tokenProvider.hashRefreshToken("token-one")
        val hash2 = tokenProvider.hashRefreshToken("token-two")

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `generateRefreshToken - should produce unique tokens on each call`() {
        val token1 = tokenProvider.generateRefreshToken()
        val token2 = tokenProvider.generateRefreshToken()

        assertNotEquals(token1, token2)
    }

    @Test
    fun `buildRefreshSession - should create a valid session model`() {
        val userId = UUID.randomUUID()
        val rawToken = "raw-refresh-token"
        val userAgent = "TestBrowser/1.0"
        val ipAddress = "192.168.1.1"

        val session = tokenProvider.buildRefreshSession(userId, rawToken, userAgent, ipAddress)

        assertNull(session.id)
        assertEquals(userId, session.userId)
        assertEquals(tokenProvider.hashRefreshToken(rawToken), session.refreshTokenHash)
        assertFalse(session.isRevoked)
        assertEquals(userAgent, session.userAgent)
        assertEquals(ipAddress, session.ipAddress)
        assertTrue(session.expiresAt.isAfter(Instant.now()))
    }

    @Test
    fun `buildRefreshSession - should handle null userAgent and ipAddress`() {
        val userId = UUID.randomUUID()
        val rawToken = "raw-refresh-token"

        val session = tokenProvider.buildRefreshSession(userId, rawToken, null, null)

        assertNotNull(session)
        assertNull(session.userAgent)
        assertNull(session.ipAddress)
    }

    @Test
    fun `verifier - should verify token contains correct audience`() {
        val token = tokenProvider.generateAccessToken(dummyUser)
        val decodedJwt = tokenProvider.verifier().verify(token)

        assertTrue(decodedJwt.audience.contains("test-audience"))
    }
}