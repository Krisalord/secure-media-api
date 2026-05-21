package io.github.krisalord

import io.github.krisalord.config.MongoConfig
import io.github.krisalord.config.loadAppConfig
import io.github.krisalord.plugins.buildDependencies
import io.github.krisalord.plugins.configureAuthentication
import io.github.krisalord.plugins.configureCors
import io.github.krisalord.plugins.configureRouting
import io.github.krisalord.plugins.configureSerialization
import io.github.krisalord.plugins.configureErrorHandling
import io.github.krisalord.plugins.configureRateLimiting
import io.ktor.server.application.*

fun Application.module() {
    val config = environment.config.loadAppConfig()

    val mongoConfig = MongoConfig(environment.config)
    val database = mongoConfig.database

    val dependencies = buildDependencies(config, database)

    configureSerialization()
    configureErrorHandling()
    configureAuthentication(dependencies.accessTokenService)
    configureRateLimiting()
    configureRouting(dependencies, config)
    configureCors()
}