package io.github.krisalord.routes

import io.github.krisalord.model.media.dto.CreateMediaRequest
import io.github.krisalord.model.media.dto.UpdateMediaRequest
import io.github.krisalord.services.MediaService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mediaRoutes(mediaService: MediaService) {
    authenticate("auth-jwt") {
        post("/media") {
            val userId = call.requireUserId()
            val request = call.receive<CreateMediaRequest>()
            val response = mediaService.createMedia(userId, request)

            call.respond(HttpStatusCode.Created, response)
        }

        get("/media") {
            val userId = call.requireUserId()
            val response = mediaService.getMediaByUserId(userId)

            call.respond(response)
        }

        get("/media/{id}") {
            val userId = call.requireUserId()
            val mediaId = call.requirePathParam("id")
            val response = mediaService.getMediaByMediaId(userId, mediaId)

            call.respond(response)
        }

        put("/media/{id}") {
            val userId = call.requireUserId()
            val mediaId = call.requirePathParam("id")
            val request = call.receive<UpdateMediaRequest>()

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