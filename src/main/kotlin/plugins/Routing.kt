package io.github.krisalord.plugins

import io.github.krisalord.auth.authRoutes
import io.github.krisalord.media.mediaRoutes
import io.github.krisalord.recommendation.recommendationRoutes
import io.github.krisalord.config.AppConfig
import io.github.krisalord.favorite_actors.favoriteActorRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting(
    dependencies: Dependencies,
    config: AppConfig
) {
    routing {
        get("/debug-ping") {
            call.respondText("Routing is definitely active!")
        }
        rateLimit(RateLimitName(GLOBAL_RATE_LIMIT)) {
            authRoutes(
                authService = dependencies.authService,
                webCookieSecure = config.cookies.secure,
                webCookieSameSite = config.cookies.sameSite,
                validityDays = config.auth.refreshToken.validityDays
            )

            mediaRoutes(mediaService = dependencies.mediaService)

            recommendationRoutes(recommendationService = dependencies.recommendationService)

            favoriteActorRoutes(favoriteActorService = dependencies.favoriteActorService)
        }
    }
}