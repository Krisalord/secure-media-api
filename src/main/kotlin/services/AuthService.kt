package io.github.krisalord.services

import io.github.krisalord.config.JwtConfig
import io.github.krisalord.errors.InvalidPasswordException
import io.github.krisalord.errors.UserAlreadyExistsException
import io.github.krisalord.errors.UserNotFoundException
import io.github.krisalord.model.user.UserModel
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.AuthValidation
import org.bson.types.ObjectId

class AuthService (
    private val userRepository: UserRepository,
    private val passwordHashing: PasswordHashing,
    private val jwtConfig: JwtConfig
) {
    fun register(
        email: String,
        rawPassword: String
    ): UserModel {
        val cleanEmail = Sanitizer.sanitizeEmail(email)

        AuthValidation.validateEmail(cleanEmail)
        AuthValidation.validatePassword(rawPassword)

        if (userRepository.findByEmail(email) != null) {
            throw UserAlreadyExistsException("This email is already registered")
        }

        return userRepository.registerUser(
            UserModel(
                id = ObjectId(),
                email = cleanEmail,
                passwordHash = passwordHashing.hash(rawPassword)
            )
        )
    }

    fun login(
        email: String,
        rawPassword: String
    ): String {
        val cleanEmail = Sanitizer.sanitizeEmail(email)

        AuthValidation.validateEmail(cleanEmail)

        val user = userRepository.findByEmail(cleanEmail)
            ?: throw UserNotFoundException("User not found")

        if (!passwordHashing.verify(rawPassword, user.passwordHash)) {
            throw InvalidPasswordException("Password is incorrect")
        }

        return jwtConfig.generateToken(user.id.toHexString())
    }
}