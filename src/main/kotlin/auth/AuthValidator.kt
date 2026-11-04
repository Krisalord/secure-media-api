package io.github.krisalord.auth

import java.util.regex.Pattern

object AuthValidator {

    private val emailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    private const val EMAIL_MIN_LENGTH = 5
    private const val EMAIL_MAX_LENGTH = 254
    private const val PASSWORD_MIN_LENGTH = 8
    private const val PASSWORD_MAX_LENGTH = 72

    fun validateCredentials(email: String, password: String) {
        validateEmail(email)
        validatePassword(password)
    }

    private fun validateEmail(email: String) {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank()) {
            throw AuthValidationException("Email can not be blank")
        }
        if (normalized.length < EMAIL_MIN_LENGTH) {
            throw AuthValidationException("Email must be at least $EMAIL_MIN_LENGTH characters")
        }
        if (normalized.length > EMAIL_MAX_LENGTH) {
            throw AuthValidationException("Email must not exceed $EMAIL_MAX_LENGTH characters")
        }
        if (!emailPattern.matcher(normalized).matches()) {
            throw AuthValidationException("Email format is not valid")
        }
    }

    private fun validatePassword(password: String) {
        if (password.isBlank()) {
            throw AuthValidationException("Password can not be blank")
        }
        if (password.length < PASSWORD_MIN_LENGTH) {
            throw AuthValidationException("Password must be at least $PASSWORD_MIN_LENGTH characters")
        }
        if (password.length > PASSWORD_MAX_LENGTH) {
            throw AuthValidationException("Password must not exceed $PASSWORD_MAX_LENGTH characters")
        }
        if (!password.any { it.isUpperCase() }) {
            throw AuthValidationException("Password must contain at least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            throw AuthValidationException("Password must contain at least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            throw AuthValidationException("Password must contain at least one digit")
        }
        if (!password.any { it in "@$!%*?&#^_-" }) {
            throw AuthValidationException("Password must contain at least one special character")
        }
    }
}