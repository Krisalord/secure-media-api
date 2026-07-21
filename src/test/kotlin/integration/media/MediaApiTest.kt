package io.github.krisalord.integration.media

import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.getAuthToken
import io.github.krisalord.media.MediaResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MediaApiTest : BaseIntegrationTest() {

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
    fun `get - should return users specific watch history including poster urls`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("history_user@example.com")

        client.post("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title": "Inception", "mediaType": "MOVIE", "rating": 5}""")
        }

        val response = client.get("/api/v1/media") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val history = response.body<List<MediaResponse>>()
        assertTrue(history.isNotEmpty())

        val inception = history.find { it.title == "Inception" }
        assertNotNull(inception)
        assertEquals("MOVIE", inception.mediaType)
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
    }

    @Test
    fun `security - should return 401 Unauthorized when Bearer token is missing`() = runSecureTestApplication { client ->
        val response = client.get("/api/v1/media")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}