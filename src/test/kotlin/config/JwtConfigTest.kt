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
        validityInMs = 60000
    )
    private val jwtConfig = JwtConfig(settings)

    @Test
    fun `generateToken should produce a valid JWT containing userId`() {
        val userId = "user123"
        val token = jwtConfig.generateToken(userId)

        val decoded = jwtConfig.verifier()
            .verify(token)

        Thread.sleep(1200)

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
        val shortSettings = JwtSettings(
            secret = "test-secret",
            issuer = "test-issuer",
            validityInMs = 500
        )

        val shortJwtConfig = JwtConfig(shortSettings)

        val token = shortJwtConfig.generateToken("user456")

        Thread.sleep(1000)

        assertFailsWith<JWTVerificationException> {
            shortJwtConfig.verifier().verify(token)
        }
    }
}