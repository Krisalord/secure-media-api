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
import java.util.UUID

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

fun ApplicationCall.requireUserId(): UUID {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Unauthorized")

    val subject = principal.payload.subject
        ?: throw UnauthorizedException("Unauthorized")

    return try {
        UUID.fromString(subject)
    } catch (e: IllegalArgumentException) {
        throw UnauthorizedException("Unauthorized")
    }
}

// TODO
// move to CallExtensions?

fun ApplicationCall.requireUuidParam(name: String): UUID {
    val raw = parameters[name]
        ?: throw BadRequestException("Missing path parameter: '$name'")

    return try {
        UUID.fromString(raw)
    } catch (e: IllegalArgumentException) {
        throw BadRequestException("Invalid UUID format for parameter: '$name'")
    }
}