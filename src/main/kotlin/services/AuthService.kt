package io.github.krisalord.services

import io.github.krisalord.config.JwtConfig
import io.github.krisalord.errors.DatabaseException
import io.github.krisalord.errors.InvalidPasswordException
import io.github.krisalord.errors.UserAlreadyExistsException
import io.github.krisalord.errors.UserNotFoundException
import io.github.krisalord.models.user.UserModel
import io.github.krisalord.models.user.dto.LoginRequest
import io.github.krisalord.models.user.dto.LoginResponse
import io.github.krisalord.models.user.dto.RegisterRequest
import io.github.krisalord.models.user.dto.RegisterResponse
import io.github.krisalord.models.user.mappers.toLoginResponse
import io.github.krisalord.models.user.mappers.toRegisterResponse
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.input.AuthValidation

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHashing: PasswordHashing,
    private val jwtConfig: JwtConfig
) {
    suspend fun register(registerRequest: RegisterRequest): RegisterResponse {
        val user = UserModel.createNewUser(registerRequest, passwordHashing)

        try {
            userRepository.registerUser(user)
        } catch (e: com.mongodb.MongoWriteException) {
            if (e.error.code == 11000) throw UserAlreadyExistsException("This email is already registered")
            else throw DatabaseException("Unexpected database error", e)
        }

        return user.toRegisterResponse()
    }

    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        val sanitizedEmail = Sanitizer.sanitizeEmail(loginRequest.email)
        AuthValidation.validateEmail(sanitizedEmail)

        val user = userRepository.findByEmail(sanitizedEmail)
            ?: throw UserNotFoundException("User not found")

        if (!passwordHashing.verify(loginRequest.passwordBeforeHash, user.passwordHash)) {
            throw InvalidPasswordException("Password is incorrect")
        }

        val token = jwtConfig.generateToken(user.id.toHexString())

        return user.toLoginResponse(token)
    }
}