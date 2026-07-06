package io.github.krisalord.config

import io.github.krisalord.auth.token.AccessTokenSettings
import io.github.krisalord.auth.token.RefreshTokenSettings
import io.ktor.server.config.ApplicationConfig

fun ApplicationConfig.loadAppConfig(): AppConfig {
    val jwt = config("ktor.jwt")
    val refresh = config("ktor.refreshToken")
    val cors = config("ktor.cors")
    val cookies = config("ktor.cookies")
    val db = config("ktor.database")
    val ai = config("ktor.ai")

    return AppConfig(
        auth = AuthConfig(
            accessToken = AccessTokenSettings(
                secret = jwt.property("secret").getString(),
                issuer = jwt.property("issuer").getString(),
                audience = jwt.property("audience").getString(),
                validityMs = jwt.property("accessValidityMs").getString().toLong()
            ),
            refreshToken = RefreshTokenSettings(
                validityDays = refresh.property("validityDays").getString().toLong(),
                reuseDetectionEnabled = refresh.property("reuseDetectionEnabled").getString().toBoolean(),
                maxSessionsPerUser = refresh.property("maxSessionsPerUser").getString().toInt(),
                tokenHashPepper = refresh.property("tokenHashPepper").getString()
            )
        ),

        cors = CorsConfig(
            allowedFrontendHosts = cors.propertyOrNull("allowedHosts")
                ?.getList()
                ?: listOf("localhost:3000", "127.0.0.1:3000", "localhost:5173", "127.0.0.1:5173")
        ),

        cookies = CookiesConfig(
            secure = cookies.property("secure").getString().toBooleanStrictOrNull() ?: false,
            sameSite = cookies.property("sameSite").getString()
        ),

        database = DatabaseSettings(
            driverClassName = db.property("driverClassName").getString(),
            jdbcUrl = db.property("jdbcUrl").getString(),
            username = db.property("username").getString(),
            password = db.property("password").getString(),
            maximumPoolSize = db.property("maximumPoolSize").getString().toInt()
        ),

        aiConfig = AiConfig(
            geminiApiKey = ai.property("geminiApiKey").getString()
        )
    )
}