package io.github.krisalord.errors

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.installErrorHandler() {
    install(StatusPages) {
        exception<AppException> { call, e ->
            call.application.log.error("Handled exception", e)
            call.respond(
                e.statusCode,
                mapOf("error" to (e.message ?: "Unknown error"))
            )
        }

        exception<Throwable> { call, e ->
            call.application.log.error("Unhandled exception", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}