package io.github.krisalord.integration.recommendation

import io.github.krisalord.integration.core.BaseIntegrationTest
import io.github.krisalord.integration.core.getAuthToken
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

class RecommendationApiTest : BaseIntegrationTest() {

    @Test
    fun `post - should fail with 400 BadRequest when promptType is invalid`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("recc_bad_prompt@example.com")

        val response = client.post("/api/v1/recommendations") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"promptType": "NON_EXISTENT_PROMPT"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `post - should fail with 400 BadRequest when CUSTOM prompt lacks input`() = runSecureTestApplication { client ->
        val token = client.getAuthToken("recc_bad_custom@example.com")

        val response = client.post("/api/v1/recommendations") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"promptType": "CUSTOM", "customInput": "   "}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `security - should return 401 Unauthorized when Bearer token is missing`() = runSecureTestApplication { client ->
        val response = client.post("/api/v1/recommendations") {
            contentType(ContentType.Application.Json)
            setBody("""{"promptType": "DEFAULT"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}