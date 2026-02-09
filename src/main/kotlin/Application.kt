package io.github.krisalord

import io.github.krisalord.config.JwtConfig
import io.github.krisalord.config.MongoConfig
import io.github.krisalord.config.loadJwtSettings
import io.github.krisalord.errors.installErrorHandler
import io.github.krisalord.model.media.MediaModel
import io.github.krisalord.model.user.UserModel
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.routes.aiRoutes
import io.github.krisalord.routes.authRoutes
import io.github.krisalord.routes.mediaRoutes
import io.github.krisalord.security.AiRateLimiter
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.services.AiClient
import io.github.krisalord.services.AiService
import io.github.krisalord.services.AuthService
import io.github.krisalord.services.MediaService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun Application.module() {
    initInfrastructure()

    val jwtSettings = loadJwtSettings()
    val jwtConfig = JwtConfig(jwtSettings)

    installPlugins(jwtConfig)

    val dependencies = buildDependencies(jwtConfig)
    registerRoutes(dependencies)
}

fun Application.initInfrastructure() {
    MongoConfig.initialize(environment)
}

fun Application.registerRoutes(dependencies: Dependencies) {
    routing {
        authRoutes(dependencies.authService)
        mediaRoutes(dependencies.mediaService)
        aiRoutes(dependencies.aiService)
    }
}

fun Application.installPlugins(jwtConfig: JwtConfig) {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtConfig.verifier())
            validate { cred ->
                cred.payload.getClaim("userId")
                    .asString()
                    ?.let { JWTPrincipal(cred.payload) }
            }
        }
    }

    installErrorHandler()
}

class Dependencies(
    val authService: AuthService,
    val mediaService: MediaService,
    val aiService: AiService? = null
)

fun Application.buildDependencies(jwtConfig: JwtConfig): Dependencies {
    val database = MongoConfig.database

    // User
    val userRepository = UserRepository(
        database.getCollection("users")
    )
    val passwordHashing = PasswordHashing()
    val authService = AuthService(userRepository, passwordHashing, jwtConfig = jwtConfig)

    // Media
    val mediaRepository = MediaRepository(
        database.getCollection("media")
    )
    val mediaService = MediaService(mediaRepository)

    // Ai Service
    val openAiKey = environment.config.propertyOrNull("ktor.openai.apiKey")?.getString()
    val aiService = if (!openAiKey.isNullOrBlank()) {
        val aiClient = AiClient(openAiKey)
        val aiRateLimiter = AiRateLimiter(maxRequests = 5, windowSeconds = 60)
        AiService(mediaRepository, aiClient, aiRateLimiter)
    } else {
        null
    }

    return Dependencies(authService, mediaService, aiService)
}