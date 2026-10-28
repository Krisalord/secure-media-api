package io.github.krisalord.unit.core.security

import io.github.krisalord.core.security.PasswordHashing
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHashingTest {

    @Test
    fun `hash - should generate a unique secure string that does not match plain text`() {
        val password = "SuperSecretPassword123!"
        val hash = PasswordHashing.hash(password)

        assertNotEquals(password, hash)
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"))
    }

    @Test
    fun `verify - should return true when plain text matches hash`() {
        val password = "MySecurePassword$$"
        val hash = PasswordHashing.hash(password)
        val isValid = PasswordHashing.verify(password, hash)

        assertTrue(isValid)
    }

    @Test
    fun `verify - should return false when plain text does not match hash`() {
        val password = "CorrectPassword"
        val wrongPassword = "IncorrectPassword"
        val hash = PasswordHashing.hash(password)
        val isValid = PasswordHashing.verify(wrongPassword, hash)

        assertFalse(isValid)
    }

    @Test
    fun `hash - salting should ensure same password produces completely different hashes`() {
        val password = "identical_password"
        val hashOne = PasswordHashing.hash(password)
        val hashTwo = PasswordHashing.hash(password)

        assertNotEquals(hashOne, hashTwo)
    }

    @Test
    fun `hash - should handle empty password`() {
        val hash = PasswordHashing.hash("")
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"))
        assertTrue(PasswordHashing.verify("", hash))
        assertFalse(PasswordHashing.verify("not-empty", hash))
    }

    @Test
    fun `hash - should handle special characters in password`() {
        val password = "P@\$\$w0rd!#%^&*()_+-=[]{}|;:',.<>?/~`"
        val hash = PasswordHashing.hash(password)

        assertTrue(PasswordHashing.verify(password, hash))
    }

    @Test
    fun `hash - should handle very long password`() {
        // BCrypt truncates passwords at 72 bytes, so two different long passwords
        // that share the first 72 bytes will produce the same hash
        val password = "a".repeat(1000)
        val hash = PasswordHashing.hash(password)

        assertTrue(PasswordHashing.verify(password, hash))
    }

    @Test
    fun `hash - should handle Unicode characters in password`() {
        val password = "пароль🔐密码"
        val hash = PasswordHashing.hash(password)

        assertTrue(PasswordHashing.verify(password, hash))
    }

    @Test
    fun `verify - should throw exception for empty hash`() {
        assertThrows<Exception> {
            PasswordHashing.verify("somePassword", "")
        }
    }

    @Test
    fun `verify - should throw exception for invalid hash format`() {
        assertThrows<Exception> {
            PasswordHashing.verify("somePassword", "not-a-valid-hash")
        }
    }
}