package io.github.krisalord.security

object Sanitizer {
    fun sanitizeText(input: String): String {
        return input
            .trim()
            .replace(Regex("<.*?>"), "")
            .replace("\u0000", "")
    }

    fun sanitizeEmail(input: String): String {
        return input.trim().lowercase()
    }
}
