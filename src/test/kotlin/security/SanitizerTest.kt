package security

import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.krisalord.security.Sanitizer


class SanitizerTest {
    @Test
    fun `sanitizeText trims whitespace`() {
        val input = "   hello world   "
        val output = Sanitizer.sanitizeText(input)
        assertEquals("hello world", output)
    }

    @Test
    fun `sanitizeText removes HTML tags`() {
        val input = "<b>Hello</b> <i>World</i>"
        val output = Sanitizer.sanitizeText(input)
        assertEquals("Hello World", output)
    }

    @Test
    fun `sanitizeText removes null characters`() {
        val input = "Hello\u0000World"
        val output = Sanitizer.sanitizeText(input)
        assertEquals("HelloWorld", output)
    }

    @Test
    fun `sanitizeText preserves normal text`() {
        val input = "Clean text"
        val output = Sanitizer.sanitizeText(input)
        assertEquals("Clean text", output)
    }

    @Test
    fun `sanitizeText combines trimming, HTML removal, and null char removal`() {
        val input = "  <p>Hello\u0000 <b>World</b> </p> "
        val output = Sanitizer.sanitizeText(input)
        assertEquals("Hello World", output)
    }

    @Test
    fun `sanitizeEmail trims whitespace`() {
        val input = "   USER@Example.com  "
        val output = Sanitizer.sanitizeEmail(input)
        assertEquals("user@example.com", output)
    }

    @Test
    fun `sanitizeEmail lowercases the email`() {
        val input = "TeSt@DOMAIN.CoM"
        val output = Sanitizer.sanitizeEmail(input)
        assertEquals("test@domain.com", output)
    }

    @Test
    fun `sanitizeEmail preserves valid email characters`() {
        val input = "user.name+tag@example-domain.com"
        val output = Sanitizer.sanitizeEmail(input)
        assertEquals("user.name+tag@example-domain.com", output)
    }
}