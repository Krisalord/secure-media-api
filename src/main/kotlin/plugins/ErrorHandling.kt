package io.github.krisalord.plugins

import io.github.krisalord.shared.AppException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureErrorHandling() {
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