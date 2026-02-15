package security

import io.github.krisalord.security.PasswordHashing
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHashingTest {
    private val passwordHashing = PasswordHashing()

    @Test
    fun `hash produces a non-empty hash`() {
        val rawPassword = "Passwrd1"
        val hash = passwordHashing.hash(rawPassword)
        assertTrue(hash.isNotEmpty(), "Hash shouldn't be empty")
        assertNotEquals(rawPassword, hash, "Hash should not match raw password")
    }

    @Test
    fun `verify succeeds for correct password`() {
        val rawPassword = "Passwrd1"
        val hash = passwordHashing.hash(rawPassword)

        val result = passwordHashing.verify(rawPassword, hash)
        assertTrue(result, "Password verification should succeed if password is correct")
    }

    @Test
    fun `verify fails for incorrect password`() {
        val rawPassword = "Passwrd1"
        val wrongPassword = "Passwrd2"
        val hash = passwordHashing.hash(rawPassword)

        val result = passwordHashing.verify(wrongPassword, hash)
        assertFalse(result, "Password verification should fail if password is not correct")
    }
}