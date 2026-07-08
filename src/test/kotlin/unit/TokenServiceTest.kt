package io.github.krisalord.unit

import io.github.krisalord.auth.UserModel
import io.github.krisalord.auth.token.AccessTokenService
import io.github.krisalord.auth.token.AccessTokenSettings
import io.github.krisalord.auth.token.RefreshTokenHashing
import io.github.krisalord.auth.token.RefreshTokenService
import io.github.krisalord.auth.token.RefreshTokenSettings
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TokenServiceTest {
    @Test
    fun `RefreshTokenHashing - HMAC SHA-256 should be deterministic with the same pepper`() {
        val hasher = RefreshTokenHashing("my-secret-pepper")
        val rawToken = "random-token-string"

        val hash1 = hasher.hmacSha256(rawToken)
        val hash2 = hasher.hmacSha256(rawToken)

        assertEquals(hash1, hash2)

        val hasherWithDifferentPepper = RefreshTokenHashing("different-pepper")
        val hash3 = hasherWithDifferentPepper.hmacSha256(rawToken)

        assertNotEquals(hash1, hash3)
    }

    @Test
    fun `RefreshTokenService - should generate unique base64 URL safe tokens`() {
        val settings = RefreshTokenSettings(7, true, 5, "pepper")
        val service = RefreshTokenService(RefreshTokenHashing("pepper"), settings)

        val token1 = service.generateRefreshToken()
        val token2 = service.generateRefreshToken()

        assertNotEquals(token1, token2)

        assertTrue(token1.matches(Regex("^[A-Za-z0-9-_]+$")))
    }

    @Test
    fun `AccessTokenService - should generate valid JWT with correct user claims`() {
        val settings = AccessTokenSettings("jwt-secret", "issuer", "audience", 360000)
        val service = AccessTokenService(settings)

        val user = UserModel("uuid-123", "test@test.com", "hash", "USER", Instant.now(), Instant.now())
        val token = service.generateAccessToken(user)

        assertNotNull(token)

        val decoded = service.verifier().verify(token)

        assertEquals("uuid-123", decoded.subject)
        assertEquals("USER", decoded.getClaim("role").asString())
        assertEquals("issuer", decoded.issuer)
        assertEquals("audience", decoded.audience.first())
    }
}