package io.github.krisalord.core.exceptions

import io.github.krisalord.plugins.AppException
import io.ktor.http.HttpStatusCode

class DatabaseException(message: String) : AppException(message, HttpStatusCode.BadRequest)