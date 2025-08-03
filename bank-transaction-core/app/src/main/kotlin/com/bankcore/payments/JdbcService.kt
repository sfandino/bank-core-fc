package com.bankcore.payments

import mu.KotlinLogging
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}
private val suspiciousThreshold = BigDecimal("10000")

object JdbcService {
  /**
   * Insert a transaction event.
   * Returns true if inserted (or id existed → skip),
   * false on validation error.
   */
  fun insertEvent(ds: DataSource, evt: TransactionEvent): Boolean {
    ds.connection.use { conn ->
      val id = UUID.randomUUID().toString()
      try {
        // parse JSON fields
        val sender   = evt.senderId
        val receiver = evt.receiverId
        val amount   = evt.amount
        val currency = evt.currency
        val status   = evt.status
        val flag_suspicious = amount > suspiciousThreshold
        val occurred = Timestamp.from(Instant.parse(evt.timestamp))
        val created  = Timestamp.from(Instant.now())

        // basic validation
        require(status in listOf("pending","completed","failed"))
        require(amount >= BigDecimal.ZERO)

        if (flag_suspicious) {
                logger.warn { "Suspicious transaction $id over threshold: $amount" }
        }

        // insert
        val sql = """
          INSERT INTO transactions
            (id, sender_id, receiver_id, currency_code, amount, status, flag_suspicious, occurred_at, created_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
          ON CONFLICT (id) DO NOTHING
        """
        conn.prepareStatement(sql).use { stmt ->
          stmt.setString(1, id)
          stmt.setString(2, sender)
          stmt.setString(3, receiver)
          stmt.setString(4, currency)
          stmt.setBigDecimal(5, amount)
          stmt.setString(6, status)
          stmt.setBoolean(7, flag_suspicious)
          stmt.setTimestamp(8, occurred)
          stmt.setTimestamp(9, created)
          val updated = stmt.executeUpdate()
          conn.commit()
          if (updated == 1) {
            logger.info { "Stream inserted event $id from JSON ${evt.transactionId}" }
            return true
          } else {
            logger.warn { "Skipped duplicate event JSON ${evt.transactionId}" }
            return true
          }
        }
      } catch (e: Exception) {
        conn.rollback()
        logger.error(e) { "Failed inserting JSON ${evt.transactionId}" }
        return false
      }
    }
  }
}