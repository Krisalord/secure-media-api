package io.github.krisalord.auth

import io.github.krisalord.plugins.LOGIN_RATE_LIMIT
import io.github.krisalord.plugins.REFRESH_RATE_LIMIT
import io.github.krisalord.plugins.REGISTER_RATE_LIMIT
import io.github.krisalord.plugins.USER_RATE_LIMIT
import io.github.krisalord.shared.requireUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    authService: AuthService,
    webCookieSecure: Boolean,
    webCookieSameSite: String
) {
    route("/api") {
        route("/v1") {
            route("/auth") {
                rateLimit(RateLimitName(REGISTER_RATE_LIMIT)) {
                    post("/register") {
                        val request = call.receive<RegisterRequest>()
                        authService.register(request)

                        call.respond(HttpStatusCode.Created)
                    }
                }

                rateLimit(RateLimitName(LOGIN_RATE_LIMIT)) {
                    post("/login") {
                        val request = call.receive<LoginRequest>()
                        val userAgent = call.request.headers["User-Agent"]
                        val ipAddress = call.request.origin.remoteHost

                        val (response, refreshToken) = authService.login(request, userAgent, ipAddress)

                        call.response.cookies.append(AuthCookies.refreshToken(refreshToken, webCookieSecure, webCookieSameSite))

                        call.respond(HttpStatusCode.OK, response)
                    }
                }

                rateLimit(RateLimitName(REFRESH_RATE_LIMIT)) {
                    post("/refresh") {
                        val oldRefreshToken = call.request.cookies["refresh_token"]
                            ?: throw UnauthorizedException("Missing refresh token")

                        val userAgent = call.request.headers["User-Agent"]
                        val ipAddress = call.request.origin.remoteHost

                        val (response, refreshToken) = authService.refresh(oldRefreshToken, userAgent, ipAddress)

                        call.response.cookies.append(AuthCookies.refreshToken(refreshToken, webCookieSecure, webCookieSameSite))

                        call.respond(HttpStatusCode.OK, response)
                    }
                }

                post("/logout") {
                    val refreshToken = call.request.cookies["refresh_token"]

                    if (refreshToken != null) {
                        authService.logout(refreshToken)
                    }

                    call.response.cookies.append(
                        AuthCookies.clearRefreshToken(
                            webCookieSecure,
                            webCookieSameSite
                        )
                    )

                    call.respond(HttpStatusCode.NoContent)
                }

                authenticate("jwt-auth") {
                    rateLimit(RateLimitName(USER_RATE_LIMIT)) {
                        post("/logout-all") {
                            val userId = call.requireUserId()

                            authService.logoutAll(userId)

                            call.response.cookies.append(
                                AuthCookies.clearRefreshToken(
                                    webCookieSecure,
                                    webCookieSameSite
                                )
                            )

                            call.respond(HttpStatusCode.NoContent)
                        }
                    }
                }
            }
        }
    }
}