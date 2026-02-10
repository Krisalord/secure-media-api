package io.github.krisalord.models.media

import kotlinx.serialization.Serializable

@Serializable
enum class WatchStatus {
    PLANNED,
    WATCHING,
    COMPLETED,
    DROPPED
}