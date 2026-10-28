package io.github.krisalord.unit.auth

import io.github.krisalord.auth.AuthCookies
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthCookiesTest {

    @Test
    fun `refreshToken - should create cookie with correct properties`() {
        val cookie = AuthCookies.refreshToken(
            value = "test-refresh-token",
            secure = true,
            sameSite = "Strict",
            validityDays = 7
        )

        assertEquals("refresh_token", cookie.name)
        assertEquals("test-refresh-token", cookie.value)
        assertTrue(cookie.httpOnly)
        assertTrue(cookie.secure)
        assertEquals("/", cookie.path)
        assertEquals(7 * 24 * 60 * 60, cookie.maxAge)
        assertEquals("Strict", cookie.extensions?.get("SameSite"))
    }

    @Test
    fun `refreshToken - should set maxAge correctly for 30 days`() {
        val cookie = AuthCookies.refreshToken(
            value = "test-token",
            secure = false,
            sameSite = "Lax",
            validityDays = 30
        )

        assertEquals(30 * 24 * 60 * 60, cookie.maxAge)
    }

    @Test
    fun `clearRefreshToken - should create cookie with maxAge 0`() {
        val cookie = AuthCookies.clearRefreshToken(
            secure = true,
            sameSite = "Strict"
        )

        assertEquals("refresh_token", cookie.name)
        assertEquals("", cookie.value)
        assertTrue(cookie.httpOnly)
        assertTrue(cookie.secure)
        assertEquals("/", cookie.path)
        assertEquals(0, cookie.maxAge)
        assertEquals("Strict", cookie.extensions?.get("SameSite"))
    }

    @Test
    fun `clearRefreshToken - should allow insecure cookies for localhost`() {
        val cookie = AuthCookies.clearRefreshToken(
            secure = false,
            sameSite = "Lax"
        )

        assertEquals(false, cookie.secure)
        assertEquals("Lax", cookie.extensions?.get("SameSite"))
    }
}
