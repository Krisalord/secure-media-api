package io.github.krisalord.auth

import org.mindrot.jbcrypt.BCrypt

object PasswordHashing {
    fun hash(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
    }

    fun verify(rawPassword: String, passwordHash: String): Boolean {
        return BCrypt.checkpw(rawPassword, passwordHash)
    }
}