package io.github.krisalord.core.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHashing {
    private const val WORK_FACTOR = 12
    fun hash(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR))
    }

    fun verify(rawPassword: String, passwordHash: String): Boolean {
        return BCrypt.checkpw(rawPassword, passwordHash)
    }
}