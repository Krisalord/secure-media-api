package io.github.krisalord.integration

import io.github.krisalord.auth.AuthTokenResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthIntegrationTest : BaseIntegrationTest() {

    private fun io.ktor.client.statement.HttpResponse.getRefreshTokenCookie(): String? {
        return headers.getAll(HttpHeaders.SetCookie)
            ?.find { it.startsWith("refresh_token=") }
            ?.substringAfter("refresh_token=")
            ?.substringBefore(";")
    }

    @Test
    fun `register - should create user and return 201 Created on valid credentials`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "fresh_user@example.com", "password": "SecurePassword123!"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `register - should fail with 400 BadRequest when email is malformed`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "bad-email.com", "password": "SecurePassword123!"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register - should fail with 400 BadRequest when password is too weak`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "weak_password@example.com", "password": "123"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login - should return access token and set cookie on valid credentials`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "login_test@example.com", "password": "Password123!"}""")
        }

        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "login_test@example.com", "password": "Password123!"}""")
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val body = loginResponse.body<AuthTokenResponse>()
        assertNotNull(body.accessToken)
        assertTrue(body.accessToken.isNotEmpty())

        val cookie = loginResponse.getRefreshTokenCookie()
        assertNotNull(cookie)
        assertTrue(cookie.isNotEmpty())
    }

    @Test
    fun `login - should fail with 401 Unauthorized when password does not match`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "wrong_pass@example.com", "password": "CorrectPassword123!"}""")
        }

        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "wrong_pass@example.com", "password": "IncorrectPassword!!!"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
    }

    @Test
    fun `refresh - should issue new tokens when a valid refresh cookie is provided`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "refresh_flow@example.com", "password": "Password123!"}""")
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "refresh_flow@example.com", "password": "Password123!"}""")
        }
        val firstRefreshToken = loginResponse.getRefreshTokenCookie()!!

        val refreshResponse = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$firstRefreshToken")
        }

        assertEquals(HttpStatusCode.OK, refreshResponse.status)

        val body = refreshResponse.body<AuthTokenResponse>()
        assertNotNull(body.accessToken)

        val secondRefreshToken = refreshResponse.getRefreshTokenCookie()
        assertNotNull(secondRefreshToken)
        assertTrue(firstRefreshToken != secondRefreshToken)
    }

    @Test
    fun `refresh - should return 401 Unauthorized if refresh cookie is missing`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/auth/refresh")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `logout - should clear refresh cookie and revoke session`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_test@example.com", "password": "Password123!"}""")
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_test@example.com", "password": "Password123!"}""")
        }
        val tokenCookie = loginResponse.getRefreshTokenCookie()!!

        val logoutResponse = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenCookie")
        }

        assertEquals(HttpStatusCode.NoContent, logoutResponse.status)

        val setCookieHeader = logoutResponse.headers.getAll(HttpHeaders.SetCookie)?.find { it.contains("refresh_token=") }
        assertNotNull(setCookieHeader)
        assertTrue(setCookieHeader.contains("Max-Age=0") || setCookieHeader.contains("refresh_token=;"))

        val subsequentRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenCookie")
        }
        assertEquals(HttpStatusCode.Unauthorized, subsequentRefresh.status)
    }

    @Test
    fun `security - token reuse detection should revoke entire session family tree`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "victim@example.com", "password": "Password123!"}""")
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "victim@example.com", "password": "Password123!"}""")
        }
        val tokenV1 = loginResponse.getRefreshTokenCookie()!!

        val legitimateRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV1")
        }
        val tokenV2Legit = legitimateRefresh.getRefreshTokenCookie()!!

        val attackerRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV1")
        }
        assertEquals(HttpStatusCode.Unauthorized, attackerRefresh.status)

        val compromisedUserRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV2Legit")
        }
        assertEquals(HttpStatusCode.Unauthorized, compromisedUserRefresh.status)
    }

    @Test
    fun `register - should return error status when email is already registered`() = runSecureTestApplication { client ->
        val payload = """{"email": "duplicate@example.com", "password": "Password123!"}"""

        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        val duplicateResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        assertTrue(duplicateResponse.status.value in 400..499)
    }

    @Test
    fun `logout-all - should revoke all active sessions for authenticated user`() = runSecureTestApplication { client ->
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }

        val loginDevice1 = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }
        val tokenDevice1 = loginDevice1.getRefreshTokenCookie()!!
        val accessToken = loginDevice1.body<AuthTokenResponse>().accessToken

        val loginDevice2 = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }
        val tokenDevice2 = loginDevice2.getRefreshTokenCookie()!!

        val logoutAllResponse = client.post("/api/v1/auth/logout-all") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.NoContent, logoutAllResponse.status)

        val refreshDevice1 = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenDevice1")
        }
        val refreshDevice2 = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenDevice2")
        }

        assertEquals(HttpStatusCode.Unauthorized, refreshDevice1.status)
        assertEquals(HttpStatusCode.Unauthorized, refreshDevice2.status)
    }
}