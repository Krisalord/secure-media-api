package io.github.krisalord.auth

import io.github.krisalord.auth.token.AccessTokenService
import io.github.krisalord.auth.PasswordHashing
import io.github.krisalord.auth.session.RefreshSessionRepository
import io.github.krisalord.auth.token.RefreshTokenService
import java.time.Instant

class AuthService(
    private val authRepository: AuthRepository,
    private val accessTokenService: AccessTokenService,
    private val refreshTokenService: RefreshTokenService,
    private val refreshSessionRepository: RefreshSessionRepository,
    private val reuseDetectionEnabled: Boolean
) {
    suspend fun register(request: RegisterRequest) {
        AuthRequestValidator.validateCredentials(request.email, request.password)
        val sanitizedEmail = request.email.trim().lowercase()

        val passwordHash = PasswordHashing.hash(request.password)
        val newUser = UserModel.create(sanitizedEmail, passwordHash)

        authRepository.create(newUser)
    }

    suspend fun login(
        request: LoginRequest,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> {
        val sanitizedEmail = request.email.trim().lowercase()
        AuthRequestValidator.validateCredentials(sanitizedEmail, request.password)

        val user = authRepository.findByEmail(sanitizedEmail)
            ?: throw UnauthorizedException("Invalid credentials")

        val valid = PasswordHashing.verify(request.password, user.passwordHash)

        if (!valid) {
            throw UnauthorizedException("Invalid credentials")
        }

        return issueTokens(user, userAgent, ipAddress)
    }

    suspend fun refresh(
        refreshToken: String,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> {
        val refreshTokenHash = refreshTokenService.hashRefreshToken(refreshToken)

        val session = refreshSessionRepository.findByTokenHash(refreshTokenHash)
            ?: throw UnauthorizedException("Invalid refresh token")

        if (session.expiresAt.isBefore(Instant.now()))
            throw UnauthorizedException("Session expired")

        val revoked = refreshSessionRepository.revokeActiveById(session.id)
        if (!revoked) {
            if (reuseDetectionEnabled) {
                refreshSessionRepository.revokeAllByUserId(session.userId)
            }
            throw UnauthorizedException("Token reuse detected")
        }

        val user = authRepository.findById(session.userId)
            ?: throw UnauthorizedException("User not found")

        return issueTokens(user, userAgent, ipAddress)
    }

    suspend fun logout(refreshToken: String) {
        val refreshTokenHash = refreshTokenService.hashRefreshToken(refreshToken)

        val session = refreshSessionRepository.findByTokenHash(refreshTokenHash)

        if (session != null) {
            refreshSessionRepository.revokeActiveById(session.id)
        }
    }

    suspend fun logoutAll(userId: String) {
        refreshSessionRepository.revokeAllByUserId(userId)
    }

    private suspend fun issueTokens(
        user: UserModel,
        userAgent: String?,
        ipAddress: String?
    ): Pair<AuthTokenResponse, String> {
        val refreshToken = refreshTokenService.generateRefreshToken()

        val refreshSession = refreshTokenService.buildRefreshSession(
            user.id,
            refreshToken,
            userAgent,
            ipAddress
        )

        refreshSessionRepository.create(refreshSession)

        val accessToken = accessTokenService.generateAccessToken(user)

        return Pair(AuthTokenResponse(accessToken), refreshToken)
    }
}