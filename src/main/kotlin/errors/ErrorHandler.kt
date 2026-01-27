package io.github.krisalord.errors

import io.ktor.server.plugins.statuspages.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*

fun Application.installErrorHandler() {
    install(StatusPages) {
        exception<Throwable> { call, _ ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }

        // Auth Errors

        exception<AuthValidationException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<UserAlreadyExistsException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<UserNotFoundException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<InvalidPasswordException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        // Media Errors

        exception<MediaValidationException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<BadRequestException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<NotFoundException> { call, e ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to e.message)
            )
        }

        exception<UnauthorizedException> { call, e ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to e.message)
            )
        }

        // OpenAi Errors
        exception<RateLimitExceededException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }

        exception<AiRequestFailedException> { call, e ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }
}