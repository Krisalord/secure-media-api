package io.github.krisalord.favorite_actors

import io.github.krisalord.plugins.FAVORITE_ACTOR_RATE_LIMIT
import io.github.krisalord.plugins.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.favoriteActorRoutes(favoriteActorService: FavoriteActorService) {
    authenticate("jwt-auth") {
        rateLimit(RateLimitName(FAVORITE_ACTOR_RATE_LIMIT)) {
            route("/api/v1/favorite-actors") {
                post {
                    val userId = call.requireUserId()
                    val request = call.receive<CreateFavoriteActorRequest>()
                    favoriteActorService.logFavoriteActor(userId, request)
                    call.respond(HttpStatusCode.Created)
                }

                get {
                    val userId = call.requireUserId()
                    val favoriteActorList = favoriteActorService.getFavoriteActorList(userId)
                    val responseFavoriteActorList = favoriteActorList.map { it.toResponse() }
                    call.respond(HttpStatusCode.OK, responseFavoriteActorList)
                }

                delete("/{id}") {
                    val userId = call.requireUserId()
                    val favoriteActorId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                    favoriteActorService.removeFavoriteActor(favoriteActorId, userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}