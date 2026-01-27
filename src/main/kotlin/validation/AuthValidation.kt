package io.github.krisalord.validation

import io.github.krisalord.errors.AuthValidationException

object AuthValidation {
    fun validateEmail(email: String) {
        if (!email.contains("@") || email.length < 6) {
            throw AuthValidationException("Invalid email format")
        }
    }
    fun validatePassword(rawPassword: String) {
        if (rawPassword.length < 8) {
            throw AuthValidationException("Password must be at least 8 characters long")
        }
        if (!rawPassword.any {it.isDigit()}) {
            throw AuthValidationException("Password must contain at least one number")
        }
        if (!rawPassword.any {it.isLetter()}) {
            throw AuthValidationException("Password must contain at least one letter")
        }
    }
}