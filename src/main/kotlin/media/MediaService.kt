package io.github.krisalord.media

import io.github.krisalord.core.database.dbQuery
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MediaService(
    private val mediaRepository: MediaRepository,
    private val httpClient: io.ktor.client.HttpClient,
    private val tmdbApiKey: String
) {
    suspend fun logMedia(userId: String, request: CreateMediaRequest): WatchedMediaModel {
        MediaValidator.validateLogRequest(request)
        val sanitizedTitle = request.title.trim().replace(Regex("\\s+"), " ")

        val fetchedPosterUrl = fetchPosterUrl(sanitizedTitle, request.mediaType)

        val mediaToCreate = WatchedMediaModel.create(
            userId = userId,
            sanitizedTitle = sanitizedTitle,
            rawMediaType = request.mediaType,
            rating = request.rating,
            posterUrl = fetchedPosterUrl
        )

        return dbQuery {
            mediaRepository.create(mediaToCreate)
        }
    }

    suspend fun getWatchHistory(userId: String): List<WatchedMediaModel> = dbQuery {
        mediaRepository.findAllByUserId(userId)
    }

    suspend fun removeMedia(id: String, userId: String): Boolean = dbQuery {
        val deleted = mediaRepository.deleteByIdAndUserId(id, userId)
        if (!deleted) throw MediaNotFoundException("Media log entry not found or access denied.")
        true
    }

    private suspend fun fetchPosterUrl(title: String, type: String): String? {
        if (tmdbApiKey.isBlank()) return null
        return try {
            val searchType = if (type == "MOVIE") "movie" else "tv"
            val response = httpClient.get("https://api.themoviedb.org/3/search/$searchType") {
                url {
                    parameters.append("api_key", tmdbApiKey)
                    parameters.append("query", title)
                }
            }

            if (response.status.isSuccess()) {
                val jsonBody = response.bodyAsText()
                val json = Json { ignoreUnknownKeys = true }
                val jsonObject = json.parseToJsonElement(jsonBody).jsonObject
                val results = jsonObject["results"]?.jsonArray

                if (!results.isNullOrEmpty()) {
                    val firstResult = results[0].jsonObject
                    val posterPath = firstResult["poster_path"]?.jsonPrimitive?.contentOrNull
                    if (posterPath != null) {
                        return "https://image.tmdb.org/t/p/w500$posterPath"
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}