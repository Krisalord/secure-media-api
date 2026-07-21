package io.github.krisalord.core.config

import io.github.krisalord.core.security.AccessTokenSettings
import io.github.krisalord.core.security.RefreshTokenSettings

data class AppConfig(
    val auth: AuthConfig,
    val cors: CorsConfig,
    val cookies: CookiesConfig,
    val database: DatabaseSettings,
    val aiConfig: AiConfig,
    val tmdbConfig: TmdbConfig
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

data class AiConfig(
    val geminiApiKey: String
)

data class TmdbConfig(
    val tmdbApiKey: String
)