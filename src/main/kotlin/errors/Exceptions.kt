package io.github.krisalord.errors

sealed class AppException(message: String) : RuntimeException(message)

// General Errors

class BadRequestException(message: String) : AppException(message)
class NotFoundException(message: String) : AppException(message)

// Auth Errors

class AuthValidationException(message: String) : AppException(message)
class UserAlreadyExistsException(message: String) : AppException(message)
class UserNotFoundException(message: String) : AppException(message)
class InvalidPasswordException(message: String) : AppException(message)

// Media Errors

class MediaValidationException(message: String) : AppException(message)
class UnauthorizedException(message: String) : AppException(message)

// OpenAi Errors

class RateLimitExceededException(message: String) : AppException(message)
class AiRequestFailedException(message: String) : AppException(message)