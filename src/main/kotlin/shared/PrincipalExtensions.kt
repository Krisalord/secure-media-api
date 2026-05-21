package io.github.krisalord.shared

import io.github.krisalord.auth.UnauthorizedException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal

fun ApplicationCall.requireUserId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Unauthorized")

    return principal.payload.subject
        ?: throw UnauthorizedException("Unauthorized")
}