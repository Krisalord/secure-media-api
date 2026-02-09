package io.github.krisalord.config

import io.ktor.server.application.ApplicationEnvironment
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoConfig {
    lateinit var client: CoroutineClient
    lateinit var database: CoroutineDatabase

    fun initialize(environment: ApplicationEnvironment) {
        val uri = environment.config.property("ktor.mongo.uri").getString()
        client = KMongo.createClient(uri).coroutine

        val dbName = environment.config.property("ktor.mongo.database").getString()
        database = client.getDatabase(dbName)
    }
}
