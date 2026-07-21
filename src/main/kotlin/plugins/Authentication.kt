package io.github.krisalord.plugins

import io.github.krisalord.auth.UnauthorizedException
import io.github.krisalord.core.security.TokenProvider
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal

fun Application.configureAuthentication(
    tokenProvider: TokenProvider,
) {
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(tokenProvider.verifier())
            validate { credential ->
                credential.payload.subject
                    ?: return@validate null
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun ApplicationCall.requireUserId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Unauthorized")

    return principal.payload.subject
        ?: throw UnauthorizedException("Unauthorized")
}