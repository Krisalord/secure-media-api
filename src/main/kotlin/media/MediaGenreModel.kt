package io.github.krisalord.media

enum class Genre {
    ACTION,
    COMEDY,
    DRAMA,
    SCI_FI,
    HORROR,
    DOCUMENTARY;

    companion object {
        fun fromString(value: String): Genre? {
            return entries.find { it.name == value.uppercase().replace("-", "_") }
        }
    }
}