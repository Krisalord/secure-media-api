package io.github.krisalord.auth.token

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.krisalord.auth.UserModel
import java.util.Date
import java.util.UUID

class AccessTokenService (
    private val accessTokenSettings: AccessTokenSettings
) {

    private val algorithm =
        Algorithm.HMAC256(accessTokenSettings.secret)

    fun generateAccessToken(user: UserModel): String {
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer(accessTokenSettings.issuer)
            .withAudience(accessTokenSettings.audience)
            .withSubject(user.id.toHexString())
            .withClaim("role", user.role)
            .withIssuedAt(Date())
            .withExpiresAt(
                Date(
                    System.currentTimeMillis() +
                            accessTokenSettings.validityMs
                )
            )
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(accessTokenSettings.issuer)
            .withAudience(accessTokenSettings.audience)
            .build()
    }
}