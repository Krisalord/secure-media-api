package io.github.krisalord.models.user

import io.github.krisalord.models.user.dto.RegisterRequest
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.security.Sanitizer
import io.github.krisalord.validation.input.AuthValidation
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserModel(
    @BsonId val id: ObjectId,
    val email: String,
    val passwordHash: String
) {
    companion object {
        fun createNewUser(request: RegisterRequest, hasher: PasswordHashing) : UserModel {
            val sanitizedEmail = Sanitizer.sanitizeEmail(request.email)

            AuthValidation.validateEmail(sanitizedEmail)
            AuthValidation.validatePassword(request.passwordBeforeHash)

            return UserModel(
                id = ObjectId(),
                email = sanitizedEmail,
                passwordHash = hasher.hash(request.passwordBeforeHash)
            )
        }
    }
}