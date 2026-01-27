package io.github.krisalord.model.media

import kotlinx.serialization.Serializable

@Serializable
enum class Genre {
    ACTION,
    DRAMA,
    COMEDY,
    THRILLER,
    HORROR,
    SCI_FI,
    FANTASY,
    ROMANCE,
    DOCUMENTARY,
    ANIMATION
}