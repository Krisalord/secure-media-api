package io.github.krisalord.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        allowedFrontendHosts().forEach { host ->
            allowHost(
                host,
                schemes = listOf("http", "https")
            )
        }
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        allowHost("localhost:5173")
        allowHost("secure-media-frontend.onrender.com")
    }
}

private fun allowedFrontendHosts(): List<String> =
    System.getenv("CORS_ALLOWED_HOSTS")
        ?.split(",")
        ?.map { it.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
        }
        ?.filter { it.isNotBlank() }
        ?: listOf(
            "localhost:3000",
        )