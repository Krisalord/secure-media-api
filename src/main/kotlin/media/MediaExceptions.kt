package io.github.krisalord.media

import io.github.krisalord.plugins.AppException
import io.ktor.http.HttpStatusCode

class InvalidMediaDataException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class MediaNotFoundException(message: String) : AppException(message, HttpStatusCode.NotFound)