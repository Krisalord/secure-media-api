package io.github.krisalord.shared

import io.ktor.http.*

open class AppException(message: String, val statusCode: HttpStatusCode)
    : RuntimeException(message)

class BadRequestException(message: String)
    : AppException(message, HttpStatusCode.BadRequest)

class DatabaseException(message: String)
    : AppException(message, HttpStatusCode.InternalServerError)