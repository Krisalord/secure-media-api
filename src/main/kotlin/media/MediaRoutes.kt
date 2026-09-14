package io.github.krisalord.media


import io.github.krisalord.plugins.AUTHENTICATED_RATE_LIMIT
import io.github.krisalord.plugins.requireUserId
import io.github.krisalord.plugins.requireUuidParam
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mediaRoutes(mediaService: MediaService) {
    authenticate("jwt-auth") {
        rateLimit(RateLimitName(AUTHENTICATED_RATE_LIMIT)) {
            route("/api/v1/media") {
                post {
                    val userId = call.requireUserId()
                    val request = call.receive<CreateMediaRequest>()
                    mediaService.logMedia(userId, request)
                    call.respond(HttpStatusCode.Created)
                }

                get {
                    val userId = call.requireUserId()
                    val history = mediaService.getWatchHistory(userId)
                    val responseHistory = history.map { it.toResponse() }
                    call.respond(HttpStatusCode.OK, responseHistory)
                }

                delete("/{id}") {
                    val userId = call.requireUserId()
                    val mediaId = call.requireUuidParam("id")

                    mediaService.removeMedia(mediaId, userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}