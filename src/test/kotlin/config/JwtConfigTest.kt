package config

import com.auth0.jwt.exceptions.JWTVerificationException
import io.github.krisalord.config.JwtConfig
import io.github.krisalord.config.JwtSettings
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JwtConfigTest {
    private val settings = JwtSettings(
        secret = "test-secret",
        issuer = "test-issuer",
        validityInMs = 1000L
    )
    private val jwtConfig = JwtConfig(settings)

    @Test
    fun `generateToken should produce a valid JWT containing userId`() {
        val userId = "user123"
        val token = jwtConfig.generateToken(userId)

        val decoded = jwtConfig.verifier().verify(token)
        assertEquals(settings.issuer, decoded.issuer)
        assertEquals(userId, decoded.getClaim("userId").asString())
    }

    @Test
    fun `verify should throw exception for invalid token`() {
        val invalidToken = "invalid.token.value"
        assertFailsWith<JWTVerificationException> {
            jwtConfig.verifier().verify(invalidToken)
        }
    }

    @Test
    fun `token should expire after validity period`() {
        val userId = "user456"
        val token = jwtConfig.generateToken(userId)

        Thread.sleep(1200)

        assertFailsWith<JWTVerificationException> {
            jwtConfig.verifier().verify(token)
        }
    }
}