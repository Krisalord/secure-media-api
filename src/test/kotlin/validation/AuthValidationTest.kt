package validation

import io.github.krisalord.errors.AuthValidationException
import io.github.krisalord.validation.input.AuthValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `validateEmail throws for email too short`() {
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
    fun `validatePassword throws if less than 8 characters`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("Abc123")
        }

        assertEquals(
            "Password must be at least 8 characters long",
            exception.message
        )
    }

    @Test
    fun `validatePassword throws if no number`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("Password")
        }

        assertEquals(
            "Password must contain at least one number",
            exception.message
        )
    }

    @Test
    fun `validatePassword throws if no letter`() {
        val exception = assertFailsWith<AuthValidationException> {
            AuthValidation.validatePassword("12345678")
        }

        assertEquals(
            "Password must contain at least one letter",
            exception.message
        )
    }
}