package io.github.krisalord.integration.favorite_actors

import io.github.krisalord.favorite_actors.FavoriteActorResponse
import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.getAuthToken
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
import kotlin.test.assertTrue

class FavoriteActorApiTest : BaseIntegrationTest() {

    @Test
    fun `post - should log favorite actor and return 201 Created`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("actor_create@example.com")

        val response = client.post("/api/v1/favorite-actors") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Denzel Washington"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `get - should return user's favorite actors`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("actor_get@example.com")

        client.post("/api/v1/favorite-actors") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Viola Davis"}""")
        }

        val response = client.get("/api/v1/favorite-actors") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val actors = response.body<List<FavoriteActorResponse>>()
        assertEquals(1, actors.size)
        assertEquals("Viola Davis", actors.first().name)
    }

    @Test
    fun `delete - should remove actor and return 204 NoContent`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("actor_delete@example.com")

        client.post("/api/v1/favorite-actors") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name": "To Be Deleted"}""")
        }

        val actors = client.get("/api/v1/favorite-actors") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body<List<FavoriteActorResponse>>()

        val deleteResponse = client.delete("/api/v1/favorite-actors/${actors.first().id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
    }
}