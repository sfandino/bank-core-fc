package com.bankcore.payments

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object HikariProvider {
  private val ds: HikariDataSource by lazy {
    HikariConfig().apply {
      jdbcUrl = "jdbc:postgresql://localhost:5432/bankcore"
      username = "postgres"
      password = "postgres"
      maximumPoolSize = 5
      isAutoCommit = false
    }.let { config ->
      HikariDataSource(config)
    }
  }

  fun dataSource(): DataSource = ds
}