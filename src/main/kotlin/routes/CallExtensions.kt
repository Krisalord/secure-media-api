package io.github.krisalord.routes

import io.github.krisalord.errors.AuthValidationException
import io.github.krisalord.errors.MediaValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.requireUserId(): String =
    principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asString()
        ?: throw AuthValidationException("Invalid or missing JWT token")

fun ApplicationCall.requirePathParam(name: String): String =
    parameters[name]
        ?: throw MediaValidationException("Missing parameter: $name")