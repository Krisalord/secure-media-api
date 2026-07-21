package io.github.krisalord.auth


import io.github.krisalord.plugins.DatabaseException
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

    fun create(user: UserModel): UserModel {
        try {
            val insertedRow = UsersTable.insert {
                it[id] = UUID.fromString(user.id)
                it[email] = user.email
                it[passwordHash] = user.passwordHash
                it[role] = user.role
            }

            val row = insertedRow.resultedValues?.firstOrNull()
                ?: throw DatabaseException("Failed to insert user: No auto-generated keys returned.")

            return toModel(row)
        } catch (e: ExposedSQLException) {
            if (e.sqlState == "23505") {
                throw UserAlreadyExistsException("This email is already registered")
            }
            throw DatabaseException("Unexpected database error: ${e.message}")
        }
    }

    fun findByEmail(email: String): UserModel? {
        return UsersTable
            .selectAll()
            .where { UsersTable.email eq email }
            .map { toModel(it) }
            .singleOrNull()
    }

    fun findById(userId: String): UserModel? {
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return null

        return UsersTable
            .selectAll()
            .where { UsersTable.id eq userUuid }
            .map { toModel(it) }
            .singleOrNull()
    }
}
