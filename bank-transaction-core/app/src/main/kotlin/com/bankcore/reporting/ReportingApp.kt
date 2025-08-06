package com.bankcore.reporting

import mu.KotlinLogging
import com.bankcore.payments.HikariProvider
import java.time.Instant
import java.time.LocalDate
import java.util.*

private val logger = KotlinLogging.logger {}

// This files defines the reporting application
fun main(args: Array<String>) {
  if (args.isEmpty()) {
    println("Usage:")
    println("  report payments <user-id> <from-iso> <to-iso>")
    println("  report daily-totals <user-id> <from-date> <to-date>")
    println("  report all-users-balance <report-date>")
    return
  }

  val ds = HikariProvider.dataSource()
  when (args[0]) {
    "payments" -> {
      val userId = args[1]
      val from   = Instant.parse(args[2])
      val to     = Instant.parse(args[3])
      val list = ReportingService.getPaymentsForUser(ds, userId, from, to)
      list.forEach { println(it) }
    }
    "daily-totals" -> {
      val userId = args[1]
      val from   = LocalDate.parse(args[2])
      val to     = LocalDate.parse(args[3])
      val list = ReportingService.getDailyTotals(ds, userId, from, to)
      list.forEach { println(it) }
    }
    "all-users-balance" -> {
      val date = LocalDate.parse(args[1])
      val list = ReportingService.getUserBalances(ds, date)
      list.forEach { println(it) }
    }
    else -> {
      println("Unknown report type: ${args[0]}")
    }
  }
}