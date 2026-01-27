package io.github.krisalord

import io.github.krisalord.errors.AuthValidationException
import io.github.krisalord.validation.AuthValidation
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class AuthValidationTest {
    @Test
    fun `validateEmail accepts valid email`() {
        AuthValidation.validateEmail("test@example.com")
    }

    @Test
    fun `validateEmail throws for email without at symbol`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validateEmail("testexample.com")
        }
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `validateEmail throws for email shorter than 6 chars`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validateEmail("a@b.c")
        }
        assertEquals("Invalid email format", exception.message)
    }

    @Test
    fun `validatePassword accepts valid password`() {
        AuthValidation.validatePassword("Password1")
    }

    @Test
    fun `validatePassword throws if less than 8 chars`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("Abc123")
        }
        assertEquals("Password must be at least 8 characters long", exception.message)
    }

    @Test
    fun `validatePassword throws if no number`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("Password")
        }
        assertEquals("Password must contain at least one number", exception.message)
    }

    @Test
    fun `validatePassword throws if no letter`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("12345678")
        }
        assertEquals("Password must contain at least one letter", exception.message)
    }
}
