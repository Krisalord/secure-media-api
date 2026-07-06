package io.github.krisalord.recommendation

import io.github.krisalord.plugins.AppException
import io.ktor.http.HttpStatusCode

class InvalidRecommendationRequestException(message: String) : AppException(message, HttpStatusCode.BadRequest)
class AiProviderException(message: String) : AppException(message, HttpStatusCode.BadGateway)