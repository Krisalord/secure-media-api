package io.github.krisalord.model.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwordBeforeHash: String
)