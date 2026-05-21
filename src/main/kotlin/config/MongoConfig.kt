package io.github.krisalord.config

import io.ktor.server.config.ApplicationConfig
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoConfig(config: ApplicationConfig) {

    val database: CoroutineDatabase

    init {
        val mongo = config.config("ktor.mongo")

        val uri = mongo.property("uri").getString()
        val dbName = mongo.property("database").getString()

        val client = KMongo.createClient(uri).coroutine
        database = client.getDatabase(dbName)
    }
}