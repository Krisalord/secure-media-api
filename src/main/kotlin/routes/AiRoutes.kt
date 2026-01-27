package io.github.krisalord.routes

import io.github.krisalord.services.AiService
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.aiRoutes(aiService: AiService?) {
    if (aiService == null) return

    authenticate("auth-jwt") {
        post("ai/suggest") {
            val userId = call.requireUserId()
            val suggestion = aiService.createSuggestion(userId)
            call.respond(mapOf("suggestion" to suggestion))
        }
    }

}