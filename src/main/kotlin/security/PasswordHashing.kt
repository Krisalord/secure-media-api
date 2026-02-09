package io.github.krisalord.security

import org.mindrot.jbcrypt.BCrypt

class PasswordHashing {
    fun hash(passwordBeforeHashing: String): String {
        return BCrypt.hashpw(passwordBeforeHashing, BCrypt.gensalt())
    }

    fun verify(passwordBeforeHashing: String, passwordHash: String): Boolean {
        return BCrypt.checkpw(passwordBeforeHashing, passwordHash)
    }
}