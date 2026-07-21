package io.github.krisalord.integration.core

import io.github.krisalord.auth.AuthTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

suspend fun HttpClient.getAuthToken(email: String): String {
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

fun HttpResponse.getRefreshTokenCookie(): String? {
    return headers.getAll(HttpHeaders.SetCookie)
        ?.find { it.startsWith("refresh_token=") }
        ?.substringAfter("refresh_token=")
        ?.substringBefore(";")
}