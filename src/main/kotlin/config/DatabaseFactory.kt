package io.github.krisalord.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.krisalord.auth.UsersTable
import io.github.krisalord.auth.session.UserSessionTable
import io.github.krisalord.favorite_actors.FavoriteActorTable
import io.github.krisalord.media.MediaTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun init(settings: DatabaseSettings) {
        val config = HikariConfig().apply {
            driverClassName = settings.driverClassName
            jdbcUrl = settings.jdbcUrl
            username = settings.username
            password = settings.password
            maximumPoolSize = settings.maximumPoolSize

            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        var dataSource: HikariDataSource? = null
        var retries = 5

        while (retries > 0) {
            try {
                dataSource = HikariDataSource(config)
                dataSource.connection.use { }
                break
            } catch (e: Exception) {
                retries--
                logger.warn("Database container initializing. Retrying in 3s... ($retries remaining)")
                dataSource?.close()
                Thread.sleep(3000)
            }
        }

        if (dataSource == null) {
            throw IllegalStateException("Failed to connect to PostgreSQL container. Verify your docker environment.")
        }

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(UsersTable, UserSessionTable, MediaTable, FavoriteActorTable)
        }

        logger.info("PostgreSQL fully initialized and connected cleanly!")
    }
}