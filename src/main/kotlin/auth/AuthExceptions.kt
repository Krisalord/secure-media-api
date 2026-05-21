package io.github.krisalord.auth

import io.github.krisalord.shared.AppException
import io.ktor.http.*

class AuthValidationException(message: String) : AppException(message, HttpStatusCode.BadRequest)

class UserAlreadyExistsException(message: String) : AppException(message, HttpStatusCode.Conflict)

class UnauthorizedException(message: String) : AppException(message, HttpStatusCode.Unauthorized)