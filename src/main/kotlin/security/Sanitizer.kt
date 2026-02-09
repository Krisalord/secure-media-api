package io.github.krisalord.security

object Sanitizer {
    fun sanitizeText(input: String): String {
        return input
            .replace(Regex("<.*?>"), "")
            .replace("\u0000", "")
            .trim()
    }

    fun sanitizeEmail(input: String): String {
        return input.trim().lowercase()
    }
}
