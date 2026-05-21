package io.github.krisalord.plugins

import io.github.krisalord.auth.AuthRepository
import io.github.krisalord.auth.AuthService
import io.github.krisalord.auth.UserModel
import io.github.krisalord.auth.token.AccessTokenService
import io.github.krisalord.auth.token.RefreshTokenHashing
import io.github.krisalord.auth.session.RefreshSessionModel
import io.github.krisalord.auth.session.RefreshSessionRepository
import io.github.krisalord.auth.token.RefreshTokenService
import io.github.krisalord.config.AppConfig
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineDatabase

class Dependencies(
    val authService: AuthService,
    val accessTokenService: AccessTokenService
)

fun Application.buildDependencies(
    config: AppConfig,
    database: CoroutineDatabase
): Dependencies {
    val userCollection = database.getCollection<UserModel>("users")
    val refreshSessionCollection = database.getCollection<RefreshSessionModel>("refresh_sessions")

    val authRepository = AuthRepository(userCollection)
    val refreshSessionRepository = RefreshSessionRepository(refreshSessionCollection)

    runBlocking {
        authRepository.ensureIndexes()
        refreshSessionRepository.ensureIndexes()
    }

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
        refreshSessionRepository = refreshSessionRepository
    )

    return Dependencies(
        authService = authService,
        accessTokenService = accessTokenService
    )
}