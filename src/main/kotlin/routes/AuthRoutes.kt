package io.github.krisalord.routes

import io.github.krisalord.model.user.LoginRequest
import io.github.krisalord.model.user.LoginResponse
import io.github.krisalord.model.user.RegisterRequest
import io.github.krisalord.model.user.RegisterResponse
import io.github.krisalord.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val request = call.receive<RegisterRequest>()
        val user = authService.register(request.email, request.rawPassword)

        call.respond(
            HttpStatusCode.Created,
            RegisterResponse(
                id = user.id.toHexString(),
                email = user.email
            )
        )
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        val token = authService.login(request.email, request.rawPassword)

        call.respond(LoginResponse(token))
    }
}