package io.github.krisalord.auth

import io.github.krisalord.core.database.dbQuery
import io.github.krisalord.core.security.PasswordHashing
import io.github.krisalord.core.security.TokenProvider
import java.time.Instant

class AuthService(
    private val authRepository: AuthRepository,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val tokenProvider: TokenProvider,
    private val reuseDetectionEnabled: Boolean,
    private val maxSessionsPerUser: Int
) {
    suspend fun register(request: RegisterRequest) = dbQuery {
        AuthValidator.validateCredentials(request.email, request.password)
        val sanitizedEmail = request.email.trim().lowercase()

        val passwordHash = PasswordHashing.hash(request.password)
        val newUser = UserModel.create(sanitizedEmail, passwordHash)

        authRepository.create(newUser)
    }

    suspend fun login(
        request: LoginRequest,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> = dbQuery {
        val sanitizedEmail = request.email.trim().lowercase()
        AuthValidator.validateCredentials(sanitizedEmail, request.password)

        val user = authRepository.findByEmail(sanitizedEmail)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!PasswordHashing.verify(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        issueTokens(user, userAgent, ipAddress)
    }

    suspend fun refresh(
        refreshToken: String,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> {
        var tokenReuseDetected = false

        val newTokens = dbQuery {
            val refreshTokenHash = tokenProvider.hashRefreshToken(refreshToken)

            val session = refreshSessionRepository.findByTokenHash(refreshTokenHash)
                ?: throw UnauthorizedException("Invalid refresh token")

            if (session.expiresAt.isBefore(Instant.now())) {
                throw UnauthorizedException("Session expired")
            }

            val revoked = refreshSessionRepository.revokeActiveById(session.id)
            if (!revoked) {
                if (reuseDetectionEnabled) {
                    refreshSessionRepository.revokeAllByUserId(session.userId)
                }
                tokenReuseDetected = true
                return@dbQuery null
            }

            val user = authRepository.findById(session.userId)
                ?: throw UnauthorizedException("User not found")

            issueTokens(user, userAgent, ipAddress)
        }

        if (tokenReuseDetected) {
            throw UnauthorizedException("Token reuse detected")
        }

        return newTokens ?: throw UnauthorizedException("Invalid refresh token")
    }

    suspend fun logout(refreshToken: String) = dbQuery {
        val refreshTokenHash = tokenProvider.hashRefreshToken(refreshToken)
        val session = refreshSessionRepository.findByTokenHash(refreshTokenHash)

        if (session != null) {
            refreshSessionRepository.revokeActiveById(session.id)
        }
    }

    suspend fun logoutAll(userId: String) = dbQuery {
        refreshSessionRepository.revokeAllByUserId(userId)
    }

    private fun issueTokens(
        user: UserModel,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> {
        refreshSessionRepository.enforceSessionLimit(user.id, maxSessionsPerUser)

        val refreshToken = tokenProvider.generateRefreshToken()

        val refreshSession = tokenProvider.buildRefreshSession(
            user.id,
            refreshToken,
            userAgent,
            ipAddress
        )

        refreshSessionRepository.create(refreshSession)
        val accessToken = tokenProvider.generateAccessToken(user)

        return Pair(AuthTokenResponse(accessToken), refreshToken)
    }
}