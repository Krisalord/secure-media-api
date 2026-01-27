package io.github.krisalord.security

import org.mindrot.jbcrypt.BCrypt

class PasswordHashing {
    fun hash(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
    }

    fun verify(rawPassword: String, hashed: String): Boolean {
        return BCrypt.checkpw(rawPassword, hashed)
    }
}