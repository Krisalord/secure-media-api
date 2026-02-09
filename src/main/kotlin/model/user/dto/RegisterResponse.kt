package io.github.krisalord.model.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val id: String,
    val email: String
)