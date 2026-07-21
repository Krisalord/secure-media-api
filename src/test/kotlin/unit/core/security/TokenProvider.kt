package io.github.krisalord.unit.core.security

import io.github.krisalord.auth.UserModel
import io.github.krisalord.core.security.AccessTokenSettings
import io.github.krisalord.core.security.RefreshTokenSettings
import io.github.krisalord.core.security.TokenProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        assertEquals(dummyUser.id, decodedJwt.subject)
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
}