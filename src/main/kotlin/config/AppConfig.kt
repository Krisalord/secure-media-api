package io.github.krisalord.config

import io.github.krisalord.auth.token.AccessTokenSettings
import io.github.krisalord.auth.token.RefreshTokenSettings

data class AppConfig(
    val auth: AuthConfig,
    val cors: CorsConfig,
    val cookies: CookiesConfig,
    val mongo: MongoSettings
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

data class MongoSettings(
    val uri: String,
    val database: String
)