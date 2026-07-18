package io.github.krisalord

import io.github.krisalord.config.DatabaseFactory
import io.github.krisalord.config.loadAppConfig
import io.github.krisalord.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing




fun Application.module() {
    println(">>> SYSTEM: Ktor Module is successfully executing! <<<")


    routing {
        get("/") {
            call.respondText("The root module is working!")
        }
    }

    val config = environment.config.loadAppConfig()

    DatabaseFactory.init(config.database)

    val dependencies = buildDependencies(config)

    configureSerialization()
    configureErrorHandling()
    configureAuthentication(dependencies.accessTokenService)
    configureRateLimiting()
    configureRouting(dependencies, config)
    configureCors()
}