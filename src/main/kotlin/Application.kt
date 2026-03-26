package io.github.krisalord

import io.github.krisalord.config.JwtConfig
import io.github.krisalord.config.MongoConfig
import io.github.krisalord.config.loadJwtSettings
import io.github.krisalord.errors.installErrorHandler
import io.github.krisalord.repositories.MediaRepository
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.routes.authRoutes
import io.github.krisalord.routes.mediaRoutes
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.services.AuthService
import io.github.krisalord.services.MediaService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
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
    }
}

fun Application.installPlugins(jwtConfig: JwtConfig) {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
    }

    install(CORS) {
        allowHost("localhost:5173")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
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


    return Dependencies(authService, mediaService)
}