package io.github.krisalord.config

import io.ktor.server.application.Application
import io.ktor.server.config.property

data class JwtSettings(
    val secret: String,
    val issuer: String,
    val validityInMs: Long
)

fun Application.loadJwtSettings(): JwtSettings {
    val config = environment.config

    fun require(path: String) =
        config.propertyOrNull(path)?.getString()
            ?: error("Missing JWT config: $path")

    return JwtSettings(
        secret = require("ktor.jwt.secret"),
        issuer = require("ktor.jwt.issuer"),
        validityInMs = require("ktor.jwt.validityMs").toLong()
    )
}