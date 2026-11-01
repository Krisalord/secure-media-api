package io.github.krisalord.plugins

import io.github.krisalord.core.config.AppConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors(config: AppConfig) {
    install(CORS) {
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        val defaultDevHosts = listOf("localhost:5173", "localhost:3000")
        val configuredHosts = config.cors.allowedFrontendHosts

        (defaultDevHosts + configuredHosts).forEach { rawHost ->
            val cleanHost = rawHost
                .removePrefix("http://")
                .removePrefix("https://")
                .trimEnd('/')
                .trim()

            if (cleanHost.isNotBlank()) {
                allowHost(cleanHost, schemes = listOf("http", "https"))
            }
        }
    }
}