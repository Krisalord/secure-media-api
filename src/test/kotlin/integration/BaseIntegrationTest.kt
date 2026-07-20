package io.github.krisalord.integration

import io.github.krisalord.module
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.testcontainers.containers.PostgreSQLContainer

abstract class BaseIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("auth_test_db")
            withUsername("test_user")
            withPassword("test_password")
        }.also {
            it.start()
        }
    }

    fun runSecureTestApplication(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.database.driverClassName" to "org.postgresql.Driver",
                "ktor.database.jdbcUrl" to postgres.jdbcUrl,
                "ktor.database.username" to postgres.username,
                "ktor.database.password" to postgres.password,
                "ktor.database.maximumPoolSize" to "5",

                "ktor.jwt.secret" to "test-jwt-signing-secret-key-that-must-be-long-enough-for-hmac",
                "ktor.jwt.issuer" to "io.github.krisalord",
                "ktor.jwt.audience" to "secure-media-api",
                "ktor.jwt.accessValidityMs" to "120000",

                "ktor.refreshToken.validityDays" to "7",
                "ktor.refreshToken.reuseDetectionEnabled" to "true",
                "ktor.refreshToken.maxSessionsPerUser" to "5",
                "ktor.refreshToken.tokenHashPepper" to "test-pepper-signature",

                "ktor.cors.allowedFrontendHosts" to "localhost:3000",
                "ktor.cookies.secure" to "false",
                "ktor.cookies.sameSite" to "Strict",

                "ktor.ai.geminiApiKey" to "test-dummy-gemini-api-key",

                "ktor.tmdb.tmdbApiKey" to "test-dummy-tmdb-api-key"
            )
        }

        application {
            module()
        }

        val testClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        block(testClient)
    }
}