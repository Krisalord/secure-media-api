package io.github.krisalord.services

import io.github.krisalord.errors.AiRequestFailedException
import io.github.krisalord.models.openai.OpenAiMessage
import io.github.krisalord.models.openai.OpenAiRequest
import io.github.krisalord.models.openai.OpenAiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


class AiClient(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun createSuggestion(text: String): String {
        try {
            val requestBody = OpenAiRequest(
                model = "gpt-4o-mini",
                messages = listOf(OpenAiMessage("user", text))
            )

            val response: OpenAiResponse = client.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            return response.choices.first().message.content
        } catch (e: Exception) {
            throw AiRequestFailedException("AI request failed")
        }
    }
}