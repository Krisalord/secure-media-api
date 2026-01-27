package io.github.krisalord.model.user

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val rawPassword: String
)
@Serializable
data class RegisterResponse(
    val id: String,
    val email: String
)
@Serializable
data class LoginRequest(
    val email: String,
    val rawPassword: String
)
@Serializable
data class LoginResponse(
    val token: String,
    val type: String = "Bearer"
)