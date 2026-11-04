package io.github.krisalord.unit.auth

import io.github.krisalord.auth.AuthValidator
import io.github.krisalord.auth.AuthValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class AuthValidatorTest {

    private val validPassword = "SecurePass1!"

    // --- Valid emails ---

    @Test
    fun `validateCredentials - should pass with simple valid email`() {
        assertDoesNotThrow {
            AuthValidator.validateCredentials("user@example.com", validPassword)
        }
    }

    @Test
    fun `validateCredentials - should pass with complex valid email including plus tag and subdomain`() {
        assertDoesNotThrow {
            AuthValidator.validateCredentials("first.last+tag@domain.co.uk", validPassword)
        }
    }

    @Test
    fun `validateCredentials - should pass with trimmed and lowercased email`() {
        assertDoesNotThrow {
            AuthValidator.validateCredentials("  User@Example.COM  ", validPassword)
        }
    }

    // --- Invalid emails ---

    @Test
    fun `validateCredentials - should throw when email is blank`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("   ", validPassword)
        }
        assertEquals("Email can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email is empty string`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("", validPassword)
        }
        assertEquals("Email can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email is too short`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("a@bc", validPassword)
        }
        assertEquals("Email must be at least 5 characters", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email exceeds 254 characters`() {
        val longEmail = "a".repeat(245) + "@example.com"
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials(longEmail, validPassword)
        }
        assertEquals("Email must not exceed 254 characters", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email lacks domain part`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@", validPassword)
        }
        assertEquals("Email format is not valid", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email lacks local part`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("@domain.com", validPassword)
        }
        assertEquals("Email format is not valid", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email lacks TLD`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@domain", validPassword)
        }
        assertEquals("Email format is not valid", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email contains spaces`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user name@example.com", validPassword)
        }
        assertEquals("Email format is not valid", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email has only @ symbol`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("@", validPassword)
        }
        assertEquals("Email must be at least 5 characters", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when email has multiple @ symbols`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@domain@extra.com", validPassword)
        }
        assertEquals("Email format is not valid", exception.message)
    }

    // --- Password complexity ---

    @Test
    fun `validateCredentials - should pass with valid email and strong password`() {
        assertDoesNotThrow {
            AuthValidator.validateCredentials("user@example.com", "SuperSecure123!")
        }
    }

    @Test
    fun `validateCredentials - should throw when password is blank`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "")
        }
        assertEquals("Password can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password is whitespace only`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "        ")
        }
        assertEquals("Password can not be blank", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password is 7 characters`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "Short1!")
        }
        assertEquals("Password must be at least 8 characters", exception.message)
    }

    @Test
    fun `validateCredentials - should pass when password is exactly 72 characters`() {
        val password72 = "A" + "a".repeat(68) + "1!a"
        assertDoesNotThrow {
            AuthValidator.validateCredentials("user@example.com", password72)
        }
    }

    @Test
    fun `validateCredentials - should throw when password is 73 characters`() {
        val password73 = "A" + "a".repeat(69) + "1!a"
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", password73)
        }
        assertEquals("Password must not exceed 72 characters", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password missing uppercase`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "securepass1!")
        }
        assertEquals("Password must contain at least one uppercase letter", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password missing lowercase`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "SECUREPASS1!")
        }
        assertEquals("Password must contain at least one lowercase letter", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password missing digit`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "SecurePass!")
        }
        assertEquals("Password must contain at least one digit", exception.message)
    }

    @Test
    fun `validateCredentials - should throw when password missing special character`() {
        val exception = assertThrows<AuthValidationException> {
            AuthValidator.validateCredentials("user@example.com", "SecurePass1")
        }
        assertEquals("Password must contain at least one special character", exception.message)
    }
}