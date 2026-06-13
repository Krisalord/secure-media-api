package io.github.krisalord.media

import io.github.krisalord.plugins.AppException
import io.ktor.http.HttpStatusCode

class InvalidMediaDataException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class MediaNotFoundException(message: String) : AppException(message, HttpStatusCode.NotFound)
class MediaDatabaseException(message: String) : AppException(message, HttpStatusCode.InternalServerError)