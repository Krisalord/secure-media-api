package io.github.krisalord.errors

import com.mongodb.MongoWriteException
import io.ktor.http.*

sealed class AppException(message: String, val statusCode: HttpStatusCode) : RuntimeException(message)

// General Errors
class BadRequestException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class NotFoundException(message: String) : AppException(message, HttpStatusCode.NotFound)

// Auth exceptions
class AuthValidationException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class UserAlreadyExistsException(message: String) : AppException(message, HttpStatusCode.Conflict)
class UserNotFoundException(message: String) : AppException(message, HttpStatusCode.NotFound)
class InvalidPasswordException(message: String) : AppException(message, HttpStatusCode.Unauthorized)

// Media exceptions
class MediaValidationException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class UnauthorizedException(message: String) : AppException(message, HttpStatusCode.Unauthorized)

// Database exceptions
class DatabaseException(message: String, e: MongoWriteException? = null) :
    AppException(message, HttpStatusCode.InternalServerError)