package io.github.krisalord.media

import com.github.benmanes.caffeine.cache.Cache
import io.github.krisalord.core.database.dbQuery
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class MediaService(
    private val mediaRepository: MediaRepository,
    private val httpClient: io.ktor.client.HttpClient,
    private val tmdbApiKey: String,
    private val posterUrlCache: Cache<String, String>
) {
    suspend fun logMedia(userId: UUID, request: CreateMediaRequest): WatchedMediaModel {
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

    suspend fun getWatchHistory(userId: UUID): List<WatchedMediaModel> = dbQuery {
        mediaRepository.findAllByUserId(userId)
    }

    suspend fun removeMedia(id: UUID, userId: UUID): Boolean = dbQuery {
        val deleted = mediaRepository.deleteByIdAndUserId(id, userId)
        if (!deleted) throw MediaNotFoundException("Media log entry not found or access denied.")
        true
    }

    private suspend fun fetchPosterUrl(title: String, type: String): String? {
        val cacheKey = "$title|$type"
        val cached = posterUrlCache.getIfPresent(cacheKey)
        if (cached != null) return cached

        val result = if (tmdbApiKey.isBlank()) {
            null
        } else {
            try {
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
                            "https://image.tmdb.org/t/p/w500$posterPath"
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        result?.let { posterUrlCache.put(cacheKey, it) }
        return result
    }
}