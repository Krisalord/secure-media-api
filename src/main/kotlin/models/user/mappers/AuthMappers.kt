package io.github.krisalord.models.user.mappers

import io.github.krisalord.models.user.UserModel
import io.github.krisalord.models.user.dto.LoginResponse
import io.github.krisalord.models.user.dto.RegisterResponse

fun UserModel.toRegisterResponse() = RegisterResponse(
    id = id.toHexString(),
    email = email
)

fun UserModel.toLoginResponse(token: String) = LoginResponse(
    token = token
)