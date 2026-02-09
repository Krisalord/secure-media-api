package io.github.krisalord.errors

import com.mongodb.MongoWriteException

sealed class AppException(message: String) : RuntimeException(message)

// General Errors

class BadRequestException(message: String) : AppException(message)
class NotFoundException(message: String) : AppException(message)

// Auth exceptions

class AuthValidationException(message: String) : AppException(message)
class UserAlreadyExistsException(message: String) : AppException(message)
class UserNotFoundException(message: String) : AppException(message)
class InvalidPasswordException(message: String) : AppException(message)

// Media exceptions

class MediaValidationException(message: String) : AppException(message)
class UnauthorizedException(message: String) : AppException(message)

// OpenAi exceptions

class RateLimitExceededException(message: String) : AppException(message)
class AiRequestFailedException(message: String) : AppException(message)

// Database exceptions

class DatabaseException(message: String, e: MongoWriteException) : AppException(message)