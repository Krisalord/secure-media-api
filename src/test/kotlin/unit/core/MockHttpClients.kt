package io.github.krisalord.unit.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

object MockHttpClients {
    fun createTmdbMockClient(): HttpClient {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/3/search/movie" -> respond(
                    content = """{"results": [{"poster_path": "/matrix_fake_poster.jpg"}]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                "/3/search/tv" -> respond(
                    content = """{"results": []}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        return HttpClient(mockEngine)
    }

    fun createGeminiMockClient(): HttpClient {
        val mockEngine = MockEngine { request ->
            respondError(HttpStatusCode.NotImplemented)
        }
        return HttpClient(mockEngine)
    }
}