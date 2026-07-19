package io.github.krisalord.favorite_actors

import io.github.krisalord.plugins.AppException
import io.ktor.http.HttpStatusCode

class InvalidFavoriteActorDataException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class FavoriteActorNotFoundException(message: String) : AppException(message, HttpStatusCode.NotFound)