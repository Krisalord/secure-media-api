package io.github.krisalord.plugins

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.AuthService
import io.github.krisalord.auth.RefreshSessionRepository
import io.github.krisalord.core.config.AppConfig
import io.github.krisalord.core.security.TokenProvider
import io.github.krisalord.media.MediaRepository
import io.github.krisalord.media.MediaService
import io.github.krisalord.recommendation.RecommendationService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import java.util.concurrent.TimeUnit

class Dependencies(
    val authService: AuthService,
    val tokenProvider: TokenProvider,
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

    val posterUrlCache = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(24, TimeUnit.HOURS)
        .build<String, String>()

    val authRepository = AuthRepository()
    val refreshSessionRepository = RefreshSessionRepository()
    val mediaRepository = MediaRepository()

    val tokenProvider = TokenProvider(
        accessTokenSettings = config.auth.accessToken,
        refreshTokenSettings = config.auth.refreshToken
    )

    val authService = AuthService(
        authRepository = authRepository,
        refreshSessionRepository = refreshSessionRepository,
        tokenProvider = tokenProvider,
        reuseDetectionEnabled = config.auth.refreshToken.reuseDetectionEnabled,
        maxSessionsPerUser = config.auth.refreshToken.maxSessionsPerUser
    )

    val mediaService = MediaService(
        mediaRepository = mediaRepository,
        httpClient = httpClient,
        tmdbApiKey = config.tmdbConfig.tmdbApiKey,
        posterUrlCache = posterUrlCache
    )


    val recommendationHttpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            retryIf { _, response ->
                response.status == HttpStatusCode.TooManyRequests ||
                    response.status == HttpStatusCode.ServiceUnavailable ||
                    response.status == HttpStatusCode.GatewayTimeout
            }
            exponentialDelay()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 5_000
        }
    }

    val recommendationService = RecommendationService(
        mediaRepository = mediaRepository,
        httpClient = recommendationHttpClient,
        geminiApiKey = config.aiConfig.geminiApiKey
    )

    return Dependencies(
        authService = authService,
        tokenProvider = tokenProvider,
        mediaService = mediaService,
        recommendationService = recommendationService
    )
}