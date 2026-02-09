package io.github.krisalord.model.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val type: String = "Bearer"
)