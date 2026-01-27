package io.github.krisalord.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import io.ktor.server.application.ApplicationEnvironment
import org.litote.kmongo.KMongo

object MongoConfig {
    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    fun initialize(environment: ApplicationEnvironment) {
        val uri = environment.config.property("ktor.mongo.uri").getString()
        client = KMongo.createClient(uri)

        val dbName = environment.config.property("ktor.mongo.database").getString()
        database = client.getDatabase(dbName)
    }
}