package com.bankcore.reporting

import java.math.BigDecimal
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import javax.sql.DataSource

// This file defines the ReportingService which provides the endpoints for generating reports
data class PaymentRecord(
  val id: String,
  val senderId: String,
  val receiverId: String,
  val currency: String,
  val amount: BigDecimal,
  val status: String,
  val occurredAt: Timestamp
)

data class DailyTotal(
  val userId: String,
  val date: LocalDate,
  val sentTotal: BigDecimal,
  val receivedTotal: BigDecimal
)

object ReportingService {
  // 1) Fetch all payments by user (either sent or received) between two users.
  fun getPaymentsForUser(
    ds: DataSource,
    userId: String,
    from: Instant,
    to: Instant
  ): List<PaymentRecord> = ds.connection.use { conn ->
    val sql = """
      SELECT id, sender_id, receiver_id, currency_code, amount, status, occurred_at
      FROM transactions
      WHERE (sender_id = ? OR receiver_id = ?)
        AND occurred_at BETWEEN ? AND ?
      ORDER BY occurred_at
    """.trimIndent()
    conn.prepareStatement(sql).use { stmt ->
      stmt.setString(1, userId)
      stmt.setString(2, userId)
      stmt.setTimestamp(3, Timestamp.from(from))
      stmt.setTimestamp(4, Timestamp.from(to))
      val rs = stmt.executeQuery()
      val results = mutableListOf<PaymentRecord>()
      while (rs.next()) {
        results += PaymentRecord(
          id         = rs.getString("id"),
          senderId   = rs.getString("sender_id"),
          receiverId = rs.getString("receiver_id"),
          currency   = rs.getString("currency_code"),
          amount     = rs.getBigDecimal("amount"),
          status     = rs.getString("status"),
          occurredAt = rs.getTimestamp("occurred_at")
        )
      }
      results
    }
  }

  /** 2) Compute daily sent/received totals per user for each date in the window. */
  fun getDailyTotals(
    ds: DataSource,
    userId: String,
    from: LocalDate,
    to: LocalDate
  ): List<DailyTotal> = ds.connection.use { conn ->
    val sql = """
      SELECT
        date(occurred_at AT TIME ZONE 'UTC') AS day,
        SUM(CASE WHEN sender_id = ? THEN amount ELSE 0 END) AS sent_total,
        SUM(CASE WHEN receiver_id = ? THEN amount ELSE 0 END) AS recv_total
      FROM transactions
      WHERE occurred_at >= ? AND occurred_at < ?
      GROUP BY day
      ORDER BY day
    """.trimIndent()
    conn.prepareStatement(sql).use { stmt ->
      stmt.setString(1, userId)
      stmt.setString(2, userId)
      stmt.setTimestamp(3, Timestamp.from(from.atStartOfDay().toInstant(ZoneOffset.UTC)))
      stmt.setTimestamp(4, Timestamp.from(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
      val rs = stmt.executeQuery()
      val results = mutableListOf<DailyTotal>()
      while (rs.next()) {
        results += DailyTotal(
          userId      = userId,
          date        = rs.getDate("day").toLocalDate(),
          sentTotal   = rs.getBigDecimal("sent_total"),
          receivedTotal = rs.getBigDecimal("recv_total")
        )
      }
      results
    }
  }
}