package io.github.krisalord.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.krisalord.core.config.DatabaseSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import javax.sql.DataSource

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var isInitialized = false

    lateinit var dbDispatcher: CoroutineDispatcher
        private set

    fun init(settings: DatabaseSettings) {
        if (isInitialized) {
            logger.info("Database already initialized, skipping reconnect.")
            return
        }

        dbDispatcher = Executors.newFixedThreadPool(settings.maximumPoolSize).asCoroutineDispatcher()

        val dataSource = connectWithRetry(settings)
        runMigrations(dataSource)
        Database.connect(dataSource)

        logger.info("PostgreSQL migrated via Flyway and connected cleanly!")
        isInitialized = true
    }

    private fun connectWithRetry(settings: DatabaseSettings, maxRetries: Int = 5): HikariDataSource {
        val config = buildHikariConfig(settings)
        var retries = maxRetries

        while (retries > 0) {
            var candidate: HikariDataSource? = null
            try {
                candidate = HikariDataSource(config)
                candidate.connection.use { }
                return candidate
            } catch (e: Exception) {
                retries--
                logger.warn("Database container initializing. Retrying in 3s... ($retries remaining)")
                candidate?.close()
                Thread.sleep(3000)
            }
        }

        throw IllegalStateException("Failed to connect to PostgreSQL container. Verify your docker environment.")
    }

    private fun buildHikariConfig(settings: DatabaseSettings) = HikariConfig().apply {
        driverClassName = settings.driverClassName
        jdbcUrl = settings.jdbcUrl
        username = settings.username
        password = settings.password
        maximumPoolSize = settings.maximumPoolSize

        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    }

    private fun runMigrations(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}

suspend fun <T> dbQuery(block: () -> T): T =
    withContext(DatabaseFactory.dbDispatcher) {
        transaction { block() }
    }