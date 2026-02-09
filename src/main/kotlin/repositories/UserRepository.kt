package io.github.krisalord.repositories

import io.github.krisalord.model.user.UserModel
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class UserRepository (
    private val userCollection: CoroutineCollection<UserModel>
) {
    suspend fun registerUser(user: UserModel): UserModel {
        userCollection.insertOne(user)
        return user
    }

    suspend fun findByEmail(email: String): UserModel? {
        return userCollection.findOne(UserModel::email eq email)
    }
}