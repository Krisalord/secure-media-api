package io.github.krisalord.model.user.mappers

import io.github.krisalord.model.user.UserModel
import io.github.krisalord.model.user.dto.LoginResponse
import io.github.krisalord.model.user.dto.RegisterResponse

fun UserModel.toRegisterResponse() = RegisterResponse(
    id = id.toHexString(),
    email = email
)

fun UserModel.toLoginResponse(token: String) = LoginResponse(
    token = token
)