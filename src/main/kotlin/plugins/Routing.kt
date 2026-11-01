package io.github.krisalord.plugins

import io.github.krisalord.auth.authRoutes
import io.github.krisalord.media.mediaRoutes
import io.github.krisalord.recommendation.recommendationRoutes
import io.github.krisalord.core.config.AppConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    dependencies: Dependencies,
    config: AppConfig
) {
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, "Server is UP")
        }
        swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")
        rateLimit(RateLimitName(GLOBAL_RATE_LIMIT)) {
            authRoutes(
                authService = dependencies.authService,
                webCookieSecure = config.cookies.secure,
                webCookieSameSite = config.cookies.sameSite,
                validityDays = config.auth.refreshToken.validityDays
            )

            mediaRoutes(mediaService = dependencies.mediaService)

            recommendationRoutes(recommendationService = dependencies.recommendationService)
        }
    }
}