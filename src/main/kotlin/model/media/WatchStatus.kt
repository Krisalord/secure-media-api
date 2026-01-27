package io.github.krisalord.model.media

import kotlinx.serialization.Serializable

@Serializable
enum class WatchStatus {
    PLANNED,
    WATCHING,
    COMPLETED,
    DROPPED
}