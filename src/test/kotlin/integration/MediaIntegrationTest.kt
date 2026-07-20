package io.github.krisalord.integration

import io.github.krisalord.auth.AuthTokenResponse
import io.github.krisalord.media.MediaResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MediaIntegrationTest : BaseIntegrationTest() {

    private suspend fun HttpClient.getAuthToken(email: String): String {
        this.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "$email", "password": "SecurePassword123!"}""")
        }
        val loginResponse = this.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "$email", "password": "SecurePassword123!"}""")
        }
        return loginResponse.body<AuthTokenResponse>().accessToken
    }

    @Test
    fun `create - should log media entry and return 201 Created on valid payload`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("create_success@example.com")

        val response = client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "The Matrix", "mediaType": "MOVIE", "rating": 5}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `create - should fail with 400 BadRequest when mediaType is invalid`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("create_bad_type@example.com")

        val response = client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "Naruto", "mediaType": "ANIME", "rating": 5}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `create - should fail with 400 BadRequest when rating is out of bounds`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("create_bad_rating@example.com")

        val response = client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "The Room", "mediaType": "MOVIE", "rating": 10}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `create - should fail with 400 BadRequest when title is blank`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("create_blank_title@example.com")

        val response = client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "   ", "mediaType": "TV_SHOW", "rating": 3}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `get - should return users specific watch history including poster urls`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("history_user@example.com")

        client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "Inception", "mediaType": "MOVIE", "rating": 5}""")
        }
        client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "Breaking Bad", "mediaType": "TV_SHOW", "rating": 5}""")
        }

        val response = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val history = response.body<List<MediaResponse>>()
        assertEquals(2, history.size)

        val inception = history.find { it.title == "Inception" }
        assertNotNull(inception)
        assertEquals("MOVIE", inception.mediaType)
        assertEquals(5, inception.rating)
        assertNotNull(inception.id)
        assertNotNull(inception.watchedAt)

        assertTrue(
            inception.posterUrl == null || inception.posterUrl!!.startsWith("https://image.tmdb.org"),
            "Poster URL should either be null (due to dummy API key failure) or a valid TMDB image URL."
        )
    }

    @Test
    fun `get - should return empty list if user has no media logs`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("empty_history@example.com")

        val response = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val history = response.body<List<MediaResponse>>()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `delete - should remove media and return 204 NoContent`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("delete_success@example.com")

        client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "To Be Deleted", "mediaType": "MOVIE", "rating": 1}""")
        }

        val history = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<MediaResponse>>()

        val mediaId = history.first().id

        val deleteResponse = client.delete("/api/v1/media/$mediaId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val emptyHistory = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<MediaResponse>>()

        assertTrue(emptyHistory.isEmpty())
    }

    @Test
    fun `delete - should return 404 NotFound if media ID does not exist`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("delete_not_found@example.com")
        val fakeUuid = "123e4567-e89b-12d3-a456-426614174000"

        val response = client.delete("/api/v1/media/$fakeUuid") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `delete - security isolation should block deleting another users media`() = runSecureTestApplication { client ->
        val tokenA = client.getAuthToken("user_a@example.com")
        client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "User A Movie", "mediaType": "MOVIE", "rating": 4}""")
        }
        val mediaIdA = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }.body<List<MediaResponse>>().first().id

        val tokenB = client.getAuthToken("user_b@example.com")
        val maliciousDeleteResponse = client.delete("/api/v1/media/$mediaIdA") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }

        assertEquals(HttpStatusCode.NotFound, maliciousDeleteResponse.status)
    }

    @Test
    fun `security - should return 401 Unauthorized when Bearer token is missing`() = runSecureTestApplication { client ->
        val response = client.get("/api/v1/media")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `security - should return 401 Unauthorized when Bearer token is invalid`() = runSecureTestApplication { client ->
        val response = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer totally-fake-and-invalid-jwt-token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `rate limiting - should block excessive media endpoint requests`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("ratelimit_media@example.com")
        var lastStatus: HttpStatusCode = HttpStatusCode.OK

        for (i in 1..65) {
            val response = client.get("/api/v1/media") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (i > 60) {
                lastStatus = response.status
            }
        }

        assertEquals(HttpStatusCode.TooManyRequests, lastStatus)
    }
}