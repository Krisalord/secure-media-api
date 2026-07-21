package io.github.krisalord.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.krisalord.auth.RefreshSessionModel
import io.github.krisalord.auth.UserModel
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TokenProvider(
    private val accessTokenSettings: AccessTokenSettings,
    private val refreshTokenSettings: RefreshTokenSettings
) {
    private val algorithm = Algorithm.HMAC256(accessTokenSettings.secret)
    private val secureRandom = SecureRandom()

    fun generateAccessToken(user: UserModel): String {
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer(accessTokenSettings.issuer)
            .withAudience(accessTokenSettings.audience)
            .withSubject(user.id)
            .withClaim("role", user.role)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenSettings.validityMs))
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(accessTokenSettings.issuer)
            .withAudience(accessTokenSettings.audience)
            .build()
    }

    fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashRefreshToken(rawRefreshToken: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(
            refreshTokenSettings.tokenHashPepper.toByteArray(StandardCharsets.UTF_8),
            "HmacSHA256"
        )
        mac.init(key)
        val digest = mac.doFinal(rawRefreshToken.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun buildRefreshSession(
        userId: String,
        rawRefreshToken: String,
        userAgent: String?,
        ipAddress: String?
    ): RefreshSessionModel {
        return RefreshSessionModel(
            id = "",
            userId = userId,
            refreshTokenHash = hashRefreshToken(rawRefreshToken),
            expiresAt = Instant.now().plus(refreshTokenSettings.validityDays, ChronoUnit.DAYS),
            createdAt = Instant.now(),
            isRevoked = false,
            userAgent = userAgent,
            ipAddress = ipAddress
        )
    }
}