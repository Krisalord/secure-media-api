package io.github.krisalord.auth.token

data class RefreshTokenSettings(
    val validityDays: Long,
    val reuseDetectionEnabled: Boolean = true,
    val maxSessionsPerUser: Int = 5,
    val tokenHashPepper: String
)

data class AccessTokenSettings(
    val secret: String,
    val issuer: String,
    val audience: String,
    val validityMs: Long
)