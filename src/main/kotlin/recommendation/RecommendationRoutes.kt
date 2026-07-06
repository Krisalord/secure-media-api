package io.github.krisalord.recommendation

import io.github.krisalord.plugins.RECOMMENDATION_RATE_LIMIT
import io.github.krisalord.plugins.requireUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.recommendationRoutes(recommendationService: RecommendationService) {
    authenticate("jwt-auth") {
        rateLimit(RateLimitName(RECOMMENDATION_RATE_LIMIT)) {
            route("/api/v1/recommendations") {
                post {
                    val userId = call.requireUserId()
                    val request = call.receive<RecommendationRequest>()
                    val response = recommendationService.getRecommendations(userId, request)
                    call.respond(HttpStatusCode.OK, response)
                }
            }
        }
    }
}