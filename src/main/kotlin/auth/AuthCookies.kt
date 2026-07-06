package io.github.krisalord.auth

import io.ktor.http.Cookie

object AuthCookies {
    fun refreshToken(
        value: String,
        secure: Boolean,
        sameSite: String,
        validityDays: Long
    ): Cookie {
        return Cookie(
            name = "refresh_token",
            value = value,
            httpOnly = true,
            secure = secure,
            path = "/",
            maxAge = (validityDays * 24 * 60 * 60).toInt(),
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