package io.github.krisalord.plugins

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.AuthService
import io.github.krisalord.auth.session.RefreshSessionRepository
import io.github.krisalord.auth.token.AccessTokenService
import io.github.krisalord.auth.token.RefreshTokenHashing
import io.github.krisalord.auth.token.RefreshTokenService
import io.github.krisalord.config.AppConfig
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.MediaService
import io.github.krisalord.recommendation.RecommendationService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*

class Dependencies(
    val authService: AuthService,
    val accessTokenService: AccessTokenService,
    val mediaService: MediaService,
    val recommendationService: RecommendationService
)

fun Application.buildDependencies(
    config: AppConfig,
): Dependencies {
    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
    }

    val authRepository = AuthRepository()
    val refreshSessionRepository = RefreshSessionRepository()
    val mediaRepository = MediaRepository()

    val refreshTokenHashing = RefreshTokenHashing(config.auth.refreshToken.tokenHashPepper)
    val accessTokenService = AccessTokenService(config.auth.accessToken)

    val refreshTokenService = RefreshTokenService(
        refreshTokenHashing = refreshTokenHashing,
        refreshTokenSettings = config.auth.refreshToken
    )

    val authService = AuthService(
        authRepository = authRepository,
        accessTokenService = accessTokenService,
        refreshTokenService = refreshTokenService,
        refreshSessionRepository = refreshSessionRepository,
        reuseDetectionEnabled = config.auth.refreshToken.reuseDetectionEnabled
    )

    val mediaService = MediaService(mediaRepository = mediaRepository)

    val recommendationService = RecommendationService(
        mediaRepository = mediaRepository,
        httpClient = httpClient,
        geminiApiKey = config.aiConfig.geminiApiKey
    )

    return Dependencies(
        authService = authService,
        accessTokenService = accessTokenService,
        mediaService = mediaService,
        recommendationService = recommendationService
    )
}