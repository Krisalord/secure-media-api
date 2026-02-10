package io.github.krisalord.models.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val id: String,
    val email: String
)