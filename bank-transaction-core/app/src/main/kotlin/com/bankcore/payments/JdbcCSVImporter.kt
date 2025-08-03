package com.bankcore.payments

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}
private val suspiciousThreshold = BigDecimal("10000")

object JdbcCSVImporter {
  @JvmStatic
  fun main(args: Array<String>) {
    // 1) HikariCP setup
    val config = HikariConfig().apply {
      jdbcUrl = "jdbc:postgresql://localhost:5432/bankcore"
      username = "postgres"
      password = "postgres"
      maximumPoolSize = 10
    }
    val ds = HikariDataSource(config)

    // 2) Read CSV - In an optimal scenario, this could be fetching data from S3/Cloud Storage with dynamic paths (dates etc..)
    val path = args.getOrNull(0) ?: "src/main/resources/data/transactions.csv"
    logger.info { "Starting import from $path" }
    val parser = CSVParser(
      FileReader(path),
      CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim()
    )

    ds.connection.use { conn ->
      conn.autoCommit = false
      val insertSql = """
        INSERT INTO transactions
          (id, sender_id, receiver_id, currency_code, amount, status, flag_suspicious, occurred_at, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO NOTHING
      """.trimIndent()
      conn.prepareStatement(insertSql).use { stmt ->
        for (rec in parser) {
          try {

            val id = rec["transaction_id"]
            val sender = rec["sender_id"]
            val receiver = rec["receiver_id"]
            val amount = BigDecimal(rec["amount"])
            val currency = rec["currency"]
            val status = rec["status"]
            val flag_suspicious = amount > suspiciousThreshold
            val occurred = Timestamp.from(Instant.parse(rec["timestamp"]))
            val created = Timestamp.from(Instant.now())
            
            if (flag_suspicious) {
                logger.warn { "Suspicious transaction $id over threshold: $amount" }
            }

            // Basic validation
            require(status in listOf("pending","completed","failed"))
            require(amount >= BigDecimal.ZERO)

            // Bind params
            stmt.setObject(1, id)
            stmt.setObject(2, sender)
            stmt.setObject(3, receiver)
            stmt.setString(4, currency)
            stmt.setBigDecimal(5, amount)
            stmt.setString(6, status)
            stmt.setBoolean(7, flag_suspicious)
            stmt.setTimestamp(8, occurred)
            stmt.setTimestamp(9, created)

            stmt.addBatch()

          } catch (e: Exception) {
            logger.error(e) { "Skipping invalid record #${rec.recordNumber}" }
          }
        }
        val counts = stmt.executeBatch()
        conn.commit()
        logger.info { "Imported ${counts.count { it >= 0 }} records." }
      }
    }
    logger.info { "Import complete." }
    ds.close()
  }
}