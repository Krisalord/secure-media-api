package io.github.krisalord.security

import io.github.krisalord.errors.RateLimitExceededException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class AiRateLimiter(
    private val maxRequests: Int,
    private val windowSeconds: Long
) {
    private val requests = ConcurrentHashMap<String, MutableList<Long>>()

    fun check(key: String) {
        val now = Instant.now().epochSecond
        val timestamps = requests.getOrPut(key) { mutableListOf() }

        timestamps.removeIf { it < now - windowSeconds }

        if (timestamps.size >= maxRequests) {
            throw RateLimitExceededException("Too many requests, try again later")
        }

        timestamps.add(now)
    }
}