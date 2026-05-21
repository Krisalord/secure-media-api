package io.github.krisalord.plugins

import io.github.krisalord.auth.authRoutes
import io.github.krisalord.config.AppConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*

fun Application.configureRouting(
    dependencies: Dependencies,
    config: AppConfig
) {
    routing {
        rateLimit(RateLimitName(GLOBAL_RATE_LIMIT)) {
            authRoutes(
                authService = dependencies.authService,
                webCookieSecure = config.cookies.secure,
                webCookieSameSite = config.cookies.sameSite
            )
        }
    }
}