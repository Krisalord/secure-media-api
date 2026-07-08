package io.github.krisalord.unit

import io.github.krisalord.auth.AuthValidator
import io.github.krisalord.auth.AuthValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class AuthValidatorTest {

    @Test
    fun `validateCredentials - should pass with valid email and strong password`() {
        assertDoesNotThrow {
            AuthValidator.validateCredentials("valid_user@example.com", "SuperSecure123!")
        }
    }

    @Test
    fun `validateCredentials - should throw when email is completely blank`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("   ", "SuperSecure123!")
        }
        assertEquals("Email can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email lacks an @ symbol`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("invalidemail.com", "SuperSecure123!")
        }
        assertEquals("Email format is not valid", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password is blank`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("valid@example.com", "   ")
        }
        assertEquals("Password can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password is under 8 characters`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("valid@example.com", "short")
        }
        assertEquals("Password must be at least 8 characters", exception.message)
    }
}