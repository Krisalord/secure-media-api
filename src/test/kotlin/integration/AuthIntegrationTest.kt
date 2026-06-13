package io.github.krisalord.auth

import io.github.krisalord.integration.BaseIntegrationTest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthIntegrationTest : BaseIntegrationTest() {

    // Helper to extract the Http-Only Refresh Token Cookie safely from responses
    private fun io.ktor.client.statement.HttpResponse.getRefreshTokenCookie(): String? {
        return headers.getAll(HttpHeaders.SetCookie)
            ?.find { it.startsWith("refresh_token=") }
            ?.substringAfter("refresh_token=")
            ?.substringBefore(";")
    }

    // =========================================================================
    // 1. REGISTRATION FLOWS
    // =========================================================================

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
        // Assuming AuthRequestValidator throws a validation exception mapped to 400 Bad Request
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

    // =========================================================================
    // 2. LOGIN FLOWS
    // =========================================================================

    @Test
    fun `login - should return access token and set cookie on valid credentials`() = runSecureTestApplication { client ->
        // Setup: Pre-register a test user
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "login_test@example.com", "password": "Password123!"}""")
        }

        // Act: Attempt Login
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "login_test@example.com", "password": "Password123!"}""")
        }

        // Assert
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        // Assert JSON body has access token
        val body = loginResponse.body<AuthTokenResponse>()
        assertNotNull(body.accessToken)
        assertTrue(body.accessToken.isNotEmpty())

        // Assert HTTP-Only cookie presence
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

    // =========================================================================
    // 3. REFRESH TOKEN FLOWS
    // =========================================================================

    @Test
    fun `refresh - should issue new tokens when a valid refresh cookie is provided`() = runSecureTestApplication { client ->
        // Setup: Register and Login to fetch a valid cookie
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "refresh_flow@example.com", "password": "Password123!"}""")
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "refresh_flow@example.com", "password": "Password123!"}""")
        }
        val firstRefreshToken = loginResponse.getRefreshTokenCookie()!!

        // Act: Hit the refresh endpoint with the cookie
        val refreshResponse = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$firstRefreshToken")
        }

        // Assert
        assertEquals(HttpStatusCode.OK, refreshResponse.status)

        val body = refreshResponse.body<AuthTokenResponse>()
        assertNotNull(body.accessToken)

        val secondRefreshToken = refreshResponse.getRefreshTokenCookie()
        assertNotNull(secondRefreshToken)
        // Ensure a sliding session strategy rotates the token value
        assertTrue(firstRefreshToken != secondRefreshToken)
    }

    @Test
    fun `refresh - should return 401 Unauthorized if refresh cookie is missing`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/auth/refresh")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // =========================================================================
    // 4. LOGOUT FLOWS
    // =========================================================================

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

        // Act
        val logoutResponse = client.post("/api/v1/auth/logout") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenCookie")
        }

        assertEquals(HttpStatusCode.NoContent, logoutResponse.status)

        // Check if cookie clearing instruction was passed back to browser
        val setCookieHeader = logoutResponse.headers.getAll(HttpHeaders.SetCookie)?.find { it.contains("refresh_token=") }
        assertNotNull(setCookieHeader)
        assertTrue(setCookieHeader.contains("Max-Age=0") || setCookieHeader.contains("refresh_token=;"))

        // Try using that refresh token again; it should be rejected because it was revoked
        val subsequentRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenCookie")
        }
        assertEquals(HttpStatusCode.Unauthorized, subsequentRefresh.status)
    }

    // =========================================================================
    // 5. SECURITY ATTACK VECTOR: REUSE DETECTION
    // =========================================================================

    @Test
    fun `security - token reuse detection should revoke entire session family tree`() = runSecureTestApplication { client ->
        // Step A: Setup account and login
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "victim@example.com", "password": "Password123!"}""")
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "victim@example.com", "password": "Password123!"}""")
        }
        val tokenV1 = loginResponse.getRefreshTokenCookie()!!

        // Step B: Legitimate browser exchanges Token V1 for Token V2
        val legitimateRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV1")
        }
        val tokenV2Legit = legitimateRefresh.getRefreshTokenCookie()!!

        // Step C: ATTACKER steals or reuses the spent Token V1
        val attackerRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV1")
        }
        // Your AuthService throws UnauthorizedException("Token reuse detected") on this event
        assertEquals(HttpStatusCode.Unauthorized, attackerRefresh.status)

        // Step D: Verify that the innocent legitimate user is now forced logged out
        // because Token V2 was part of the breached family tree and was completely wiped!
        val compromisedUserRefresh = client.post("/api/v1/auth/refresh") {
            header(HttpHeaders.Cookie, "refresh_token=$tokenV2Legit")
        }
        assertEquals(HttpStatusCode.Unauthorized, compromisedUserRefresh.status)
    }

    // =========================================================================
    // 6. PROTECTED ROUTE & EXCEPTION HANDLING
    // =========================================================================

    @Test
    fun `register - should return error status when email is already registered`() = runSecureTestApplication { client ->
        val payload = """{"email": "duplicate@example.com", "password": "Password123!"}"""

        // First registration call succeeds
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        // Second registration call with identical email
        val duplicateResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        // Verifies your StatusPages error plugin captures UserAlreadyExistsException gracefully
        assertTrue(duplicateResponse.status.value in 400..499)
    }

    @Test
    fun `logout-all - should revoke all active sessions for authenticated user`() = runSecureTestApplication { client ->
        // Step A: Setup account
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }

        // Step B: Simulate user logging in from two different devices (fetching two valid refresh cookies)
        val loginDevice1 = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }
        val tokenDevice1 = loginDevice1.getRefreshTokenCookie()!!
        val accessToken = loginDevice1.body<AuthTokenResponse>().accessToken // Grab the bearer token

        val loginDevice2 = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "logout_all@example.com", "password": "Password123!"}""")
        }
        val tokenDevice2 = loginDevice2.getRefreshTokenCookie()!!

        // Step C: Hit the protected /logout-all endpoint using the Access Token
        val logoutAllResponse = client.post("/api/v1/auth/logout-all") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.NoContent, logoutAllResponse.status)

        // Step D: Verify BOTH devices are now completely kicked out when they try to refresh
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