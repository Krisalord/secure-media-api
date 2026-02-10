package io.github.krisalord.routes

import io.github.krisalord.models.user.dto.LoginRequest
import io.github.krisalord.models.user.dto.RegisterRequest
import io.github.krisalord.services.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val request = call.receive<RegisterRequest>()
        val response = authService.register(request)

        call.respond(HttpStatusCode.OK, response)
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        val response = authService.login(request)

        call.respond(HttpStatusCode.OK, response)
    }
}