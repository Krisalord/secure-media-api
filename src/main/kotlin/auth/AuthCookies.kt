package io.github.krisalord.auth

import io.ktor.http.Cookie

object AuthCookies {
    fun refreshToken(
        value: String,
        secure: Boolean,
        sameSite: String
    ): Cookie {
        return Cookie(
            name = "refresh_token",
            value = value,
            httpOnly = true,
            secure = secure,
            path = "/",
            maxAge = 60 * 60 * 24 * 30,
            extensions = mapOf(
                "SameSite" to sameSite
            )
        )
    }

    fun clearRefreshToken(
        secure: Boolean,
        sameSite: String
    ): Cookie {
        return Cookie(
            name = "refresh_token",
            value = "",
            httpOnly = true,
            secure = secure,
            path = "/",
            maxAge = 0,
            extensions = mapOf(
                "SameSite" to sameSite
            )
        )
    }
}