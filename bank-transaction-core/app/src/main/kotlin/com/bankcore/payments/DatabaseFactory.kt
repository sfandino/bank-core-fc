package com.bankcore.payments

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
  fun init() {
    val config = HikariConfig().apply {
      jdbcUrl             = "jdbc:postgresql://localhost:5432/bankcore"
      driverClassName     = "org.postgresql.Driver"
      username            = "postgres"
      password            = "postgres"
      maximumPoolSize     = 5
      isAutoCommit        = false
      transactionIsolation= "TRANSACTION_REPEATABLE_READ"
    }
    val ds = HikariDataSource(config)
    Database.connect(ds)
  }
}