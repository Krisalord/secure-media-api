package io.github.krisalord.auth.token

import io.github.krisalord.auth.token.RefreshTokenHashing
import io.github.krisalord.auth.session.RefreshSessionModel
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

class RefreshTokenService(
    private val refreshTokenHashing: RefreshTokenHashing,
    private val refreshTokenSettings: RefreshTokenSettings
) {
    private val secureRandom = SecureRandom()
    fun generateRefreshToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashRefreshToken(rawRefreshToken: String): String {
        return refreshTokenHashing.hmacSha256(rawRefreshToken)
    }

    fun refreshTokenExpiresAt(): Instant {
        return Instant.now().plus(
            refreshTokenSettings.validityDays,
            ChronoUnit.DAYS
        )
    }

    fun buildRefreshSession(
        userId: String,
        rawRefreshToken: String,
        userAgent: String?,
        ipAddress: String?
    ): RefreshSessionModel {
        return RefreshSessionModel(
            userId = userId,
            refreshTokenHash = hashRefreshToken(rawRefreshToken),
            expiresAt = refreshTokenExpiresAt(),
            userAgent = userAgent,
            ipAddress = ipAddress
        )
    }
}