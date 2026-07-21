package io.github.krisalord.integration.core

import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun runDbTest(block: () -> Unit) {
    transaction {
        try {
            block()
        } finally {
            rollback()
        }
    }
}