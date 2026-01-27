package io.github.krisalord.model.user

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserModel(
    @BsonId val id: ObjectId,
    val email: String,
    val passwordHash: String
)