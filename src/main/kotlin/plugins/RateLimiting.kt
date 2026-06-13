package io.github.krisalord.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.minutes

const val GLOBAL_RATE_LIMIT = "global-rate-limit"
const val REGISTER_RATE_LIMIT = "register-rate-limit"
const val LOGIN_RATE_LIMIT = "login-rate-limit"
const val REFRESH_RATE_LIMIT = "refresh-rate-limit"
const val USER_RATE_LIMIT = "user-rate-limit"
const val MEDIA_RATE_LIMIT = "media-rate-limit"

fun Application.configureRateLimiting() {
    install(RateLimit) {
        register(RateLimitName(GLOBAL_RATE_LIMIT)) {
            requestKey {
                "global"
            }
            rateLimiter(limit = 10000, refillPeriod = 1.minutes)
        }

        register(RateLimitName(REGISTER_RATE_LIMIT)) {
            requestKey { call ->
                call.clientIp()
            }
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
        }

        register(RateLimitName(LOGIN_RATE_LIMIT)) {
            requestKey { call ->
                call.clientIp()
            }
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
        }

        register(RateLimitName(REFRESH_RATE_LIMIT)) {
            requestKey { call ->
                call.clientIp()
            }
            rateLimiter(limit = 30, refillPeriod = 1.minutes)
        }

        register(RateLimitName(USER_RATE_LIMIT)) {
            requestKey { call ->
                call.principalUserIdOrIp()
            }
            rateLimiter(limit = 120, refillPeriod = 1.minutes)
        }

        register(RateLimitName(MEDIA_RATE_LIMIT)) {
            requestKey { call ->
                call.principalUserIdOrIp()
            }
            rateLimiter(limit = 60, refillPeriod = 1.minutes)
        }
    }
}

private fun ApplicationCall.clientIp(): String {
    return request.origin.remoteHost
}

private fun ApplicationCall.principalUserIdOrIp(): String {
    val userId = authentication.principal<JWTPrincipal>()
        ?.payload
        ?.subject

    return userId ?: clientIp()
}