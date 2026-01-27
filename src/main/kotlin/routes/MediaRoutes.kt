package io.github.krisalord.routes

import io.github.krisalord.errors.NotFoundException
import io.github.krisalord.model.media.MediaRequest
import io.github.krisalord.model.media.toResponse
import io.github.krisalord.services.MediaService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mediaRoutes(mediaService: MediaService) {
    authenticate("auth-jwt") {
        post("/media") {
            try{
                val userId = call.requireUserId()

                val request = call.receive<MediaRequest>()
                val media = mediaService.createMedia(userId, request)

                call.respond(HttpStatusCode.Created, media.toResponse())
            }catch(e: Exception){
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        get("/media") {
            val userId = call.requireUserId()

            val media = mediaService.getMediaByUserId(userId)
            call.respond(media.map { it.toResponse() })
        }
        get("/media/{id}") {
            val userId = call.requireUserId()
            val mediaId = call.requirePathParam("id")

            val media = mediaService.getMediaByMediaId(userId, mediaId)
                ?: throw NotFoundException("Media not found")

            call.respond(media.toResponse())
        }
        put("/media/{id}") {
            val userId = call.requireUserId()
            val mediaId = call.requirePathParam("id")

            val request = call.receive<MediaRequest>()
            mediaService.updateMedia(userId, mediaId, request)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/media/{id}") {
            val userId = call.requireUserId()
            val mediaId = call.requirePathParam("id")

            mediaService.deleteMedia(userId, mediaId)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}