package io.github.krisalord.repositories

import com.mongodb.client.MongoCollection
import io.github.krisalord.model.user.UserModel
import org.litote.kmongo.findOne
import org.litote.kmongo.eq

class UserRepository (
    private val userCollection: MongoCollection<UserModel>
) {
    fun registerUser(
        user: UserModel
    ): UserModel {
        userCollection.insertOne(user)
        return user
    }

    fun findByEmail(
        email: String
    ): UserModel? {
        return userCollection.findOne(UserModel::email eq email)
    }
}