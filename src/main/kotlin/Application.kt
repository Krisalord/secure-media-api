package io.github.krisalord

import io.github.krisalord.core.database.DatabaseFactory
import io.github.krisalord.core.config.loadAppConfig
import io.github.krisalord.plugins.*
import io.ktor.server.application.*

fun Application.module() {
    val config = environment.config.loadAppConfig()

    DatabaseFactory.init(config.database)

    val dependencies = buildDependencies(config)

    configureSerialization()
    configureErrorHandling()
    configureAuthentication(dependencies.tokenProvider)
    configureRateLimiting()
    configureRouting(dependencies, config)
    configureCors()
}

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}