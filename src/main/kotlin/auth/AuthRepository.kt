package io.github.krisalord.auth


import io.github.krisalord.shared.DatabaseException
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AuthRepository {
    private fun toModel(row: ResultRow): UserModel = UserModel(
        id = row[UsersTable.id].value.toString(),
        email = row[UsersTable.email],
        passwordHash = row[UsersTable.passwordHash],
        role = row[UsersTable.role],
        createdAt = row[UsersTable.createdAt],
        updatedAt = row[UsersTable.updatedAt]
    )

    suspend fun create(user: UserModel): UserModel = newSuspendedTransaction {
        try {
            val insertedRow = UsersTable.insert {
                if (user.id.isNotEmpty())
                    it[id] = UUID.fromString(user.id)
                it[email] = user.email
                it[passwordHash] = user.passwordHash
                it[role] = user.role
            }
            toModel(insertedRow.resultedValues?.first()!!)
        } catch (e: ExposedSQLException) {
            if (e.sqlState == "23505") {
                throw UserAlreadyExistsException("This email is already registered")
            }
            throw DatabaseException("Unexpected database error: ${e.message}")
        }
    }

    suspend fun findByEmail(email: String): UserModel? = newSuspendedTransaction {
        UsersTable
            .selectAll()
            .where { UsersTable.email eq email }
            .map { toModel(it) }
            .singleOrNull()
    }

    suspend fun findById(userId: String): UserModel? = newSuspendedTransaction {
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return@newSuspendedTransaction null

        UsersTable
            .selectAll()
            .where { UsersTable.id eq userUuid }
            .map { toModel(it) }
            .singleOrNull()
    }
}
