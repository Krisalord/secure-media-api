package io.github.krisalord.unit.core.security

import io.github.krisalord.core.security.PasswordHashing
import org.junit.jupiter.api.Test
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
}