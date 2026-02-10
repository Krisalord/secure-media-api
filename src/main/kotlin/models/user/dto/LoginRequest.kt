package io.github.krisalord.models.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwordBeforeHash: String
)