package io.github.krisalord.repositories

import com.mongodb.client.model.Filters
import io.github.krisalord.model.media.MediaModel
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection

class MediaRepository(
    private val mediaCollection: CoroutineCollection<MediaModel>,
) {
    suspend fun addMedia(mediaModel: MediaModel): MediaModel {
        mediaCollection.insertOne(mediaModel)
        return mediaModel
    }

    suspend fun getAllMediaByUserId(userId: String): List<MediaModel> {
        return mediaCollection.find(
            Filters.eq("userId", userId)
        ).toList()
    }

    suspend fun getMediaByMediaId(mediaId: ObjectId, userId: String): MediaModel? {
        return mediaCollection.find(
            Filters.and(
                Filters.eq("_id", mediaId),
                Filters.eq("userId", userId)
            )
        ).first()
    }

    suspend fun updateMedia(mediaModel: MediaModel): Boolean {
        val result = mediaCollection.replaceOne(
            Filters.and(
                Filters.eq("_id", mediaModel.id),
                Filters.eq("userId", mediaModel.userId)
            ),
            mediaModel
        )
        return result.matchedCount == 1L
    }

    suspend fun deleteMedia(id: ObjectId, userId: String): Boolean {
        val result = mediaCollection.deleteOne(
            Filters.and(
                Filters.eq("_id", id),
                Filters.eq("userId", userId)
            )
        )
        return result.deletedCount == 1L
    }
}