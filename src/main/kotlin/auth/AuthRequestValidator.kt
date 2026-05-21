package io.github.krisalord.auth

object AuthRequestValidator {
    fun validateCredentials(email: String, password: String) {
        validateEmail(email)
        validatePassword(password)
    }

    private fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw AuthValidationException("Email can not be blank")
        }
        if (!email.contains("@")) {
            throw AuthValidationException("Email format is not valid")
        }
    }

    private fun validatePassword(password: String) {
        if (password.isBlank()) {
            throw AuthValidationException("Password can not be blank")
        }
        if (password.length < 8) {
            throw AuthValidationException("Password must be at least 8 characters")
        }
    }
}