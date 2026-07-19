package io.github.krisalord

import io.github.krisalord.config.DatabaseFactory
import io.github.krisalord.config.loadAppConfig
import io.github.krisalord.plugins.*
import io.ktor.server.application.*

fun Application.module() {
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

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}