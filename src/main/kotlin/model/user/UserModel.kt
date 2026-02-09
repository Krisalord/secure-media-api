package io.github.krisalord.model.user

import io.github.krisalord.model.user.dto.RegisterRequest
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.AuthValidation
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserModel(
    @BsonId val id: ObjectId,
    val email: String,
    val passwordHash: String
) {
    companion object {
        fun createNewUser(registerRequest: RegisterRequest, hasher: PasswordHashing) : UserModel {
            val sanitizedEmail = Sanitizer.sanitizeEmail(registerRequest.email)

            AuthValidation.validateEmail(sanitizedEmail)
            AuthValidation.validatePassword(registerRequest.passwordBeforeHash)

            return UserModel(
                id = ObjectId(),
                email = sanitizedEmail,
                passwordHash = hasher.hash(registerRequest.passwordBeforeHash)
            )
        }
    }
}