package com.bankcore.payments

import org.flywaydb.core.Flyway

fun main() {
    val flyway = Flyway.configure()
        .dataSource(
            "jdbc:postgresql://localhost:5432/bankcore",
            "postgres",
            "postgres"
        )
        .locations("classpath:db/migration")
        .load()

    val result = flyway.migrate()
    println("Got it finnaly applied - migration: ${result.migrationsExecuted}")
}