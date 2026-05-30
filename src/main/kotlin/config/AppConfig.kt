package io.github.krisalord.config

import io.github.krisalord.auth.token.AccessTokenSettings
import io.github.krisalord.auth.token.RefreshTokenSettings

data class AppConfig(
    val auth: AuthConfig,
    val cors: CorsConfig,
    val cookies: CookiesConfig,
    val database: DatabaseSettings
)

data class AuthConfig(
    val accessToken: AccessTokenSettings,
    val refreshToken: RefreshTokenSettings
)

data class CookiesConfig(
    val secure: Boolean,
    val sameSite: String
)

data class CorsConfig(
    val allowedFrontendHosts: List<String>
)

data class DatabaseSettings(
    val driverClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int
)