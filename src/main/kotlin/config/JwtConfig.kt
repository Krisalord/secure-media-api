package io.github.krisalord.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtConfig(
    private val settings: JwtSettings
) {
    private val algorithm = Algorithm.HMAC256(settings.secret)

    fun generateToken(userId: String): String =
        JWT.create()
            .withIssuer(settings.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + settings.validityInMs))
            .sign(algorithm)

    fun verifier() =
        JWT.require(algorithm)
            .withIssuer(settings.issuer)
            .build()
}