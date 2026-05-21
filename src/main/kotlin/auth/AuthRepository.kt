package io.github.krisalord.auth

import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.github.krisalord.shared.DatabaseException
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class AuthRepository(
    private val users: CoroutineCollection<UserModel>
) {
    suspend fun ensureIndexes() {
        users.createIndex(
            Indexes.ascending("email"),
            IndexOptions()
                .unique(true)
                .name("user_email_unique")
        )
    }
    suspend fun create(user: UserModel): UserModel {
        try {
            users.insertOne(user)
            return user
        } catch (e: MongoWriteException) {
            if (e.error.code == 11000) {
                throw UserAlreadyExistsException("This email is already registered")
            }
            throw DatabaseException("Unexpected database error")
        }
    }

    suspend fun findByEmail(email: String): UserModel? =
        users.findOne(UserModel::email eq email)

    suspend fun findById(userId: String): UserModel? {
        if (!ObjectId.isValid(userId))
            return null

        return users.findOneById(ObjectId(userId))
    }
}
