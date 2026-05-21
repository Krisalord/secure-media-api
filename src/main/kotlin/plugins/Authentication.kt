package io.github.krisalord.plugins

import io.github.krisalord.auth.token.AccessTokenService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureAuthentication(
    accessTokenService: AccessTokenService,
) {
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(accessTokenService.verifier())
            validate { credential ->
                val userId = credential.payload.subject
                    ?: return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}