package io.github.krisalord.services

import io.github.krisalord.models.media.dto.SearchMediaResponse
import java.util.concurrent.ConcurrentHashMap

object MediaCache {
    private val allMediaOfUserCache = ConcurrentHashMap<String, List<SearchMediaResponse>>()

    fun getAllMediaOfUser(userId: String): List<SearchMediaResponse>? = allMediaOfUserCache[userId]

    fun setAllMediaOfUser(userId: String, media: List<SearchMediaResponse>) {
        allMediaOfUserCache[userId] = media
    }

    fun invalidateAllMediaOfUser(userId: String) {
        allMediaOfUserCache.remove(userId)
    }
}