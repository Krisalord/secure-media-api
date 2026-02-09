package io.github.krisalord.routes

import io.github.krisalord.model.user.dto.LoginRequest
import io.github.krisalord.model.user.dto.RegisterRequest
import io.github.krisalord.services.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val requestRegister = call.receive<RegisterRequest>()
        val registerResponse = authService.register(requestRegister)

        call.respond(HttpStatusCode.OK, registerResponse)
    }

    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val loginResponse = authService.login(loginRequest)

        call.respond(HttpStatusCode.OK, loginResponse)
    }
}